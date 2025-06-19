# Payment Module Implementation Proposal for Goods and Subscription Features

## Overview
This document proposes an implementation for managing goods, subscriptions, and coupons within the `mosaic-boot-payment` module. The goal is to support a distinction between regular and subscription-based goods, allow multiple plans for subscriptions with varying options, manage subscription lifecycle including history, validity periods, automatic cancellation on payment failure, and introduce a coupon system for discounts.

## Requirements
- **Goods**: Must distinguish between regular and subscription-based goods. Subscription goods can have multiple plans, and both types of goods should support options with different pricing. Subscription plans should be distinguishable via options.
- **Subscription**: Must link to subscription-type goods via `goodsId`. APIs should provide plan validity periods. Subscription history must be manageable with current activation status. Validity periods can be managed in the subscription entity, ensuring idempotency through payment information. Subscriptions must auto-cancel on payment failure.
- **Coupons**: Must support a coupon system with fields for code, applicability period (permanent or limited cycles), number of uses, type (percentage or amount discount), and discount value. Must handle concurrency issues and allow checking of remaining coupon uses.

## Proposed Implementation

### 1. Goods Management

#### 1.1 Goods Entity Structure
To support the distinction between regular and subscription goods, introduce a `Goods` interface with a type discriminator and embedded options and plans.

- **Goods Interface** (`Goods.kt`):
  ```kotlin
  interface Goods : UpdatableEntity<String> {
      val name: String
      val type: GoodsType
      val description: String?
      val basePrice: Long
      val options: List<GoodsOption>
      val plans: List<SubscriptionPlan> // Only applicable for SUBSCRIPTION type
  }

  enum class GoodsType {
      REGULAR,
      SUBSCRIPTION
  }
  ```
  - `type` field distinguishes between regular and subscription goods.
  - `options` embeds a list of `GoodsOption` for both regular and subscription goods.
  - `plans` embeds a list of `SubscriptionPlan`, applicable only for subscription goods.

- **Goods Option Embedded Entity** (`GoodsOption.kt`):
  ```kotlin
  interface GoodsOption : UpdatableEntity<String> {
      val name: String
      val additionalPrice: Long
      val description: String?
  }
  ```
  - Represents options for goods, each with an additional price impact.
  - Used for both regular goods (e.g., add-ons) and subscription goods (e.g., differentiating plans).

- **Subscription Plan Embedded Entity** (`SubscriptionPlan.kt`):
  ```kotlin
  interface SubscriptionPlan : UpdatableEntity<String> {
      val name: String
      val billingCycle: BillingCycle
      val cycleDuration: Long
  }

  enum class BillingCycle {
      DAY,
      WEEK,
      MONTH,
      YEAR
  }
  ```
  - Specific to subscription goods, defines billing cycles for plans.

- **Goods Repository** (`GoodsRepository.kt`):
  Extend the existing empty interface to include methods for managing goods, options, and plans.
  ```kotlin
  interface GoodsRepository {
      fun findGoodsById(id: String): Goods?
      fun saveGoods(goods: Goods): Goods
  }
  ```
  - Since options and plans are embedded in `Goods`, repository methods focus on the `Goods` entity as a whole.

### 2. Subscription Management

#### 2.1 Subscription Entity Enhancement
Enhance the existing `PaymentSubscription` interface to include necessary fields for linking to goods and managing validity, with history managed separately.

- **Enhanced PaymentSubscription Interface** (`PaymentSubscription.kt`):
  ```kotlin
  interface PaymentSubscription : UpdatableEntity<String> {
      val pg: String
      val active: Boolean
      val data: Map<String, *>
      val cancelledAt: Instant?
      val goodsId: String
      val planId: String
      val validFrom: Instant
      val validTo: Instant
  }
  ```
  - `goodsId` links to a subscription-type `Goods`.
  - `planId` links to a specific `SubscriptionPlan` within `Goods.plans`.
  - `validFrom` and `validTo` manage the validity period of the subscription.

- **Subscription History Entity** (`SubscriptionHistory.kt`):
  ```kotlin
  interface SubscriptionHistory : UpdatableEntity<String> {
      val subscriptionId: String
      val status: SubscriptionStatus
      val changeDate: Instant
      val details: String?
  }

  enum class SubscriptionStatus {
      ACTIVE,
      PENDING,
      CANCELLED,
      EXPIRED,
      PAYMENT_FAILED
  }
  ```
  - Tracks the history of subscription status changes independently from the `PaymentSubscription` entity.

#### 2.2 Subscription Repository and Service
- **Subscription Repository** (`PaymentSubscriptionRepositoryBase.kt`):
  Extend to include methods for querying validity and history.
  ```kotlin
  interface PaymentSubscriptionRepositoryBase {
      fun findById(id: String): PaymentSubscription?
      fun findActiveByGoodsId(goodsId: String, userId: String): List<PaymentSubscription>
      fun findHistoryBySubscriptionId(subscriptionId: String): List<SubscriptionHistory>
      fun saveSubscription(subscription: PaymentSubscription): PaymentSubscription
      fun saveHistory(history: SubscriptionHistory): SubscriptionHistory
  }
  ```

- **Subscription Service** (Extend `PaymentService.kt`):
  Add logic for managing subscriptions, including validation of plan periods through payment information for idempotency.
  ```kotlin
  fun validateSubscriptionPeriod(subscriptionId: String): Boolean {
      val subscription = subscriptionRepository.findById(subscriptionId)
      val transaction = transactionRepository.findLatestBySubscriptionId(subscriptionId)
      if (subscription == null || transaction == null) return false
      val expectedValidTo = calculateValidToFromTransaction(subscription.planId, transaction)
      return subscription.validTo == expectedValidTo
  }

  fun handlePaymentFailure(subscriptionId: String) {
      val subscription = subscriptionRepository.findById(subscriptionId)
      if (subscription != null && subscription.active) {
          val updatedSubscription = subscription.copy(active = false, cancelledAt = Instant.now())
          subscriptionRepository.saveSubscription(updatedSubscription)
          val history = SubscriptionHistory(
              subscriptionId = subscriptionId,
              status = SubscriptionStatus.PAYMENT_FAILED,
              changeDate = Instant.now(),
              details = "Payment failed on due date"
          )
          subscriptionRepository.saveHistory(history)
      }
  }
  ```
  - `validateSubscriptionPeriod` ensures idempotency by cross-verifying the subscription's validity period with payment transaction data.
  - `handlePaymentFailure` automatically cancels subscriptions on payment failure and logs the event in history.

### 3. Coupon Management

#### 3.1 Coupon Entity Structure
Introduce a `Coupon` entity to manage discount coupons applicable to goods and subscriptions.

- **Coupon Interface** (`Coupon.kt`):
  ```kotlin
  interface Coupon : UpdatableEntity<String> {
      val code: String
      val applicabilityPeriod: Long // 0 for permanent, >=1 for number of billing cycles (e.g., 3 months)
      val totalCount: Long // Total number of times this coupon can be used
      val remainingCount: Long // Remaining number of times this coupon can be used
      val type: CouponType
      val discountValue: Long // Percentage (0-100) or fixed amount based on type
  }

  enum class CouponType {
      PERCENTAGE,
      AMOUNT
  }
  ```
  - `code` is a unique identifier for the coupon.
  - `applicabilityPeriod` defines how long the coupon is valid (0 for permanent, otherwise number of cycles).
  - `totalCount` and `remainingCount` track the number of total and remaining uses for concurrency control.
  - `type` and `discountValue` define whether the discount is a percentage or fixed amount and its value.

- **Coupon Repository** (`CouponRepository.kt`):
  ```kotlin
  interface CouponRepository {
      fun findByCode(code: String): Coupon?
      fun saveCoupon(coupon: Coupon): Coupon
      @Synchronized
      fun decrementRemainingCount(code: String): Boolean // Returns true if successful, false if no remaining uses
      fun getRemainingCount(code: String): Long
  }
  ```
  - `decrementRemainingCount` is synchronized to handle concurrency issues, ensuring that the coupon usage count is updated atomically to prevent over-usage.
  - `getRemainingCount` allows checking the remaining number of uses for a coupon.

- **Coupon Service** (Extend `PaymentService.kt`):
  Add logic for applying coupons and checking availability.
  ```kotlin
  fun applyCoupon(code: String, transactionId: String): Boolean {
      val coupon = couponRepository.findByCode(code)
      if (coupon == null || coupon.remainingCount <= 0) return false
      val success = couponRepository.decrementRemainingCount(code)
      if (success) {
          // Apply discount logic to transaction
          val transaction = transactionRepository.findById(transactionId)
          if (transaction != null) {
              val discount = if (coupon.type == CouponType.PERCENTAGE) {
                  transaction.amount * coupon.discountValue / 100
              } else {
                  coupon.discountValue
              }
              val updatedTransaction = transaction.copy(amount = transaction.amount - discount)
              transactionRepository.save(updatedTransaction)
          }
      }
      return success
  }

  fun checkRemainingCouponCount(code: String): Long {
      return couponRepository.getRemainingCount(code)
  }
  ```
  - `applyCoupon` checks availability and applies the discount, using synchronized methods to prevent concurrency issues.
  - `checkRemainingCouponCount` provides the remaining uses for a given coupon code.

### 4. API for Plan Validity and Coupon Information
- **API Endpoint for Subscription Validity** (Extend `MosaicPaymentController.kt`):
  Provide an endpoint to query subscription plan validity.
  ```kotlin
  @GetMapping("/subscriptions/{subscriptionId}/validity")
  fun getSubscriptionValidity(@PathVariable subscriptionId: String): ResponseEntity<SubscriptionValidityResponse> {
      val subscription = paymentService.getSubscription(subscriptionId)
      return if (subscription != null) {
          ResponseEntity.ok(SubscriptionValidityResponse(
              validFrom = subscription.validFrom,
              validTo = subscription.validTo,
              active = subscription.active
          ))
      } else {
          ResponseEntity.notFound().build()
      }
  }

  data class SubscriptionValidityResponse(
      val validFrom: Instant,
      val validTo: Instant,
      val active: Boolean
  )
  ```

- **API Endpoint for Coupon Information** (Extend `MosaicPaymentController.kt`):
  Provide an endpoint to query coupon details and remaining uses.
  ```kotlin
  @GetMapping("/coupons/{code}/info")
  fun getCouponInfo(@PathVariable code: String): ResponseEntity<CouponInfoResponse> {
      val coupon = paymentService.getCouponByCode(code)
      return if (coupon != null) {
          ResponseEntity.ok(CouponInfoResponse(
              code = coupon.code,
              applicabilityPeriod = coupon.applicabilityPeriod,
              remainingCount = paymentService.checkRemainingCouponCount(code),
              type = coupon.type.toString(),
              discountValue = coupon.discountValue
          ))
      } else {
          ResponseEntity.notFound().build()
      }
  }

  data class CouponInfoResponse(
      val code: String,
      val applicabilityPeriod: Long,
      val remainingCount: Long,
      val type: String,
      val discountValue: Long
  )
  ```

## Integration with Existing Code
- The proposed `Goods` entity integrates with the existing `PaymentSubscription` by linking through `goodsId` and `planId`.
- The `GoodsRepository` is extended from its current empty state to handle CRUD operations for goods, with embedded options and plans.
- Subscription management builds on the existing `PaymentSubscription` interface, adding necessary fields and logic for lifecycle management, with history managed separately.
- The new `Coupon` entity and related services introduce a discount system, integrated with transaction processing to apply discounts and manage concurrency.

## Conclusion
This proposal outlines a comprehensive structure for managing goods, subscriptions, and coupons within the `mosaic-boot-payment` module. It addresses all specified requirements, ensuring distinction between goods types, support for multiple plans and options, subscription linking, validity management, history tracking, automatic cancellation on payment failure, and a coupon system with concurrency control. The design is extensible and integrates with the existing minimal structure in the payment module by embedding `GoodsOption` and `SubscriptionPlan` within the `Goods` interface without direct association between them, separating `SubscriptionHistory` from `PaymentSubscription`, and tracking remaining coupon uses for easier database management.
