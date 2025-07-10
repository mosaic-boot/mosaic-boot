/**
 * Copyright 2025 JC-Lab (mosaicboot.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mosaicboot.payment.service

import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.result.UserErrorMessageException
import io.mosaicboot.payment.db.dto.PaymentSubscriptionInput
import io.mosaicboot.payment.db.dto.PaymentSubscriptionLogInput
import io.mosaicboot.payment.db.entity.PaymentSubscription
import io.mosaicboot.payment.db.entity.PaymentSubscriptionRenew
import io.mosaicboot.payment.db.entity.PaymentSubscriptionRenewStatus
import io.mosaicboot.payment.db.entity.SubscriptionStatus
import io.mosaicboot.payment.db.repository.PaymentSubscriptionRenewRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentBillingRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentCouponRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentGoodsRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentSubscriptionLogRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentSubscriptionRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentTransactionRepositoryBase
import io.mosaicboot.payment.dto.SubmitBillingPayment
import io.mosaicboot.payment.dto.SubscriptionUpdateType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PaymentSubscriptionService(
    private val paymentService: PaymentService,
    private val pgRouter: PgRouter,
    private val paymentBillingRepository: PaymentBillingRepositoryBase<*>,
    private val paymentCouponRepository: PaymentCouponRepositoryBase<*>,
    private val paymentGoodsRepository: PaymentGoodsRepositoryBase<*>,
    private val paymentTransactionRepository: PaymentTransactionRepositoryBase<*>,
    private val paymentSubscriptionRepository: PaymentSubscriptionRepositoryBase<*>,
    private val paymentSubscriptionLogRepository: PaymentSubscriptionLogRepositoryBase<*>,
    private val paymentSubscriptionRenewRepository: PaymentSubscriptionRenewRepositoryBase<*>,
) {
    @Transactional
    fun startSubscription(
        authentication: MosaicAuthenticatedToken,
        traceId: String,
        goodsId: String,
        optionId: String?,
        couponId: String?,
        firstAmount: BigDecimal,
        billingCycle: Int = 30,
        isUpdate: SubscriptionUpdateType? = null,
        feedback: String? = null,
    ): PaymentSubscription {
        // 1. 금액 검증은 caller 에서 해야 함.

        // 2. goods, option 검증
        val (goods, option) = paymentService.getGoods(goodsId, optionId)

        // 3. billing 확인
        val billing = paymentBillingRepository.findPrimaryByUserId(authentication.userId)
            ?: throw UserErrorMessageException(
                HttpStatus.NOT_FOUND,
                "Billing method not found"
            )

        // 4. 이전 subscription 확인
        val lastSubscription = paymentSubscriptionRepository.findLatestByUserIdAndGoodsId(
            userId = authentication.userId,
            goodsId = goodsId,
        )
        val existingSubscription = lastSubscription
            ?.takeIf { isSubscriptionAvailable(it) }

        val now = Instant.now()

        val updatedSubscription = if (existingSubscription != null) {
            // 구독 갱신 또는 변경
            if (isUpdate == null) {
                throw UserErrorMessageException(
                    HttpStatus.CONFLICT,
                    "Active subscription already exists for this goods"
                )
            }

            val fromOptionId = existingSubscription.optionId
            when (isUpdate) {
                SubscriptionUpdateType.NOW -> { // 즉시 변경 (업그레이드)
                    existingSubscription.optionId = optionId
                    existingSubscription.status = SubscriptionStatus.ACTIVE
                    existingSubscription.validTo = now.plus(billingCycle.toLong(), ChronoUnit.DAYS)
                }
                SubscriptionUpdateType.NEXT -> { // 다음 결제 주기에 변경 (다운그레이드)
                    existingSubscription.scheduledOptionId = optionId
                    existingSubscription.status = SubscriptionStatus.PENDING_CHANGE
                }
            }
            existingSubscription.updatedAt = now
            val subscription = paymentSubscriptionRepository.saveEntity(existingSubscription)
            paymentSubscriptionLogRepository.save(
                PaymentSubscriptionLogInput(
                    userId = authentication.userId,
                    subscriptionId = existingSubscription.id,
                    traceId = traceId,
                    status = existingSubscription.status,
                    fromOptionId = fromOptionId,
                    toOptionId = optionId,
                    reason = feedback
                )
            )
            subscription
        } else {
            // 신규 구독
            val subscriptionInput = PaymentSubscriptionInput(
                createdAt = now,
                traceId = traceId,
                userId = authentication.userId,
                goodsId = goodsId,
                version = 1,
                optionId = optionId,
                usedCouponId = couponId,
                billingId = billing.id,
                status = SubscriptionStatus.ACTIVE,
                billingCycle = billingCycle,
                validFrom = now,
                validTo = now.plus(billingCycle.toLong(), ChronoUnit.DAYS),
                scheduledOptionId = null
            )
            val subscription = paymentSubscriptionRepository.save(subscriptionInput)

            paymentSubscriptionLogRepository.save(
                PaymentSubscriptionLogInput(
                    userId = authentication.userId,
                    subscriptionId = subscription.id,
                    traceId = traceId,
                    status = subscription.status,
                    fromOptionId = null,
                    toOptionId = optionId,
                    reason = "New subscription"
                )
            )

            subscription
        }

        pgRouter.getPg(billing.pg).processBillingPayment(
            authentication.userId,
            traceId,
            billing,
            SubmitBillingPayment(
                goodsId = goods.id,
                goodsName = goods.name + option?.name?.let { ": $it" },
                subscriptionId = updatedSubscription.id,
                usedCouponId = couponId,
                amount = firstAmount,
            )
        ).getOrThrow()

        return updatedSubscription
    }

    @Transactional
    fun cancelSubscription(
        authentication: MosaicAuthenticatedToken,
        traceId: String,
        goodsId: String,
        feedback: String? = null,
    ) {
        val now = Instant.now()

        val subscription = paymentSubscriptionRepository.findCurrentByUserIdAndGoodsId(
            userId = authentication.userId,
            goodsId = goodsId,
        ) ?: throw UserErrorMessageException(
            HttpStatus.NOT_FOUND,
            "no existing subscription"
        )
        subscription.status = SubscriptionStatus.PENDING_CANCEL
        subscription.updatedAt = now
        paymentSubscriptionRepository.saveEntity(subscription)
        paymentSubscriptionLogRepository.save(
            PaymentSubscriptionLogInput(
                userId = authentication.userId,
                subscriptionId = subscription.id,
                traceId = traceId,
                status = subscription.status,
                fromOptionId = subscription.optionId,
                toOptionId = subscription.optionId,
                reason = feedback
            )
        )
    }

    @Transactional
    fun cancelChangeSubscription(
        authentication: MosaicAuthenticatedToken,
        traceId: String,
        goodsId: String,
        feedback: String? = null,
    ) {
        val now = Instant.now()

        val subscription = paymentSubscriptionRepository.findCurrentByUserIdAndGoodsId(
            userId = authentication.userId,
            goodsId = goodsId,
        ) ?: throw UserErrorMessageException(
            HttpStatus.NOT_FOUND,
            "no existing subscription"
        )

        if (subscription.status == SubscriptionStatus.PENDING_CHANGE || subscription.status == SubscriptionStatus.PENDING_CANCEL) {
            val fromOptionId = subscription.optionId
            val scheduledOptionId = subscription.scheduledOptionId
            subscription.updatedAt = now
            subscription.status = SubscriptionStatus.ACTIVE
            subscription.scheduledOptionId = null
            paymentSubscriptionRepository.saveEntity(subscription)
            paymentSubscriptionLogRepository.save(
                PaymentSubscriptionLogInput(
                    userId = authentication.userId,
                    subscriptionId = subscription.id,
                    traceId = traceId,
                    status = subscription.status,
                    fromOptionId = fromOptionId,
                    toOptionId = scheduledOptionId,
                    reason = feedback
                )
            )
        } else {
            throw UserErrorMessageException(
                HttpStatus.BAD_REQUEST,
                "Subscription is not pending any change."
            )
        }
    }

    fun findSubscriptions(
        authentication: MosaicAuthenticatedToken,
        goodsId: String?,
        statuses: List<SubscriptionStatus>?,
        pageRequest: PageRequest,
    ): Page<out PaymentSubscription> {
        return paymentSubscriptionRepository.findSubscriptions(
            authentication.userId,
            goodsId,
            statuses,
            pageRequest,
        )
    }

    fun getCurrentSubscription(
        authentication: MosaicAuthenticatedToken,
        goodsId: String,
    ): PaymentSubscription? {
        val subscription = paymentSubscriptionRepository.findCurrentByUserIdAndGoodsId(
            userId = authentication.userId,
            goodsId = goodsId,
        ) ?: return null
        if (!isSubscriptionAvailable(subscription)) {
            return null
        }
        return subscription
    }

    fun isSubscriptionAvailable(
        subscription: PaymentSubscription,
        now: Instant = Instant.now(),
    ): Boolean {
        return subscription.status != SubscriptionStatus.CANCELED && subscription.validTo.isAfter(now)
    }

    @Transactional
    fun renewSubscription(renewEntity: PaymentSubscriptionRenew) {
        val subscription = paymentSubscriptionRepository.findById(renewEntity.subscriptionId).orElse(null)
            ?: throw UserErrorMessageException(HttpStatus.NOT_FOUND, "Subscription not found")

        val (goods, option) = paymentService.getGoods(subscription.goodsId, subscription.optionId)

        val billing = paymentBillingRepository.findPrimaryByUserId(subscription.userId)
            ?: throw UserErrorMessageException(HttpStatus.NOT_FOUND, "Billing method not found")

        val amount = paymentService.calculateDiscountedAmount2(
            subscription.goodsId,
            subscription.optionId,
            subscription.usedCouponId,
            renewEntity.paymentCount
        )

        try {
            pgRouter.getPg(billing.pg).processBillingPayment(
                subscription.userId,
                subscription.traceId,
                billing,
                SubmitBillingPayment(
                    goodsId = goods.id,
                    goodsName = goods.name + option?.name?.let { ": $it" },
                    subscriptionId = subscription.id,
                    usedCouponId = subscription.usedCouponId,
                    amount = amount,
                )
            ).getOrThrow()

            subscription.paymentCount = renewEntity.paymentCount
            subscription.validTo = subscription.validTo.plus(subscription.billingCycle.toLong(), ChronoUnit.DAYS)
            subscription.updatedAt = Instant.now()
            paymentSubscriptionRepository.saveEntity(subscription)

            renewEntity.status = PaymentSubscriptionRenewStatus.PAID
            renewEntity.updatedAt = Instant.now()
            paymentSubscriptionRenewRepository.saveEntity(renewEntity)
        } catch (e: Exception) {
            renewEntity.status = PaymentSubscriptionRenewStatus.FAILED
            renewEntity.updatedAt = Instant.now()
            paymentSubscriptionRenewRepository.saveEntity(renewEntity)
            throw e
        }
    }
}
