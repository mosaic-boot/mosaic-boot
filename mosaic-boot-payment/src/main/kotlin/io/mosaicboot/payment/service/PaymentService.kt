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
import io.mosaicboot.core.result.SimpleSuccess
import io.mosaicboot.core.result.UserErrorMessageException
import io.mosaicboot.payment.controller.dto.AddCardTypeKrRequest
import io.mosaicboot.payment.db.entity.CouponType
import io.mosaicboot.payment.db.entity.PaymentBilling
import io.mosaicboot.payment.db.repository.PaymentBillingRepositoryBase
import org.springframework.stereotype.Service

import io.mosaicboot.payment.db.entity.PaymentCoupon
import io.mosaicboot.payment.db.repository.PaymentCouponRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentGoodsRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentTransactionRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentSubscriptionRepositoryBase
import io.mosaicboot.payment.dto.CouponResult
import io.mosaicboot.payment.dto.SubmitBillingPayment
import io.mosaicboot.payment.db.dto.PaymentSubscriptionInput
import io.mosaicboot.payment.db.entity.GoodsOption
import io.mosaicboot.payment.db.entity.PaymentGoods
import io.mosaicboot.payment.db.entity.PaymentSubscription
import io.mosaicboot.payment.dto.SubscriptionUpdateType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrNull

@Service
class PaymentService(
    private val pgRouter: PgRouter,
    private val paymentBillingRepository: PaymentBillingRepositoryBase<*>,
    private val paymentCouponRepository: PaymentCouponRepositoryBase<*>,
    private val paymentGoodsRepository: PaymentGoodsRepositoryBase<*>,
    private val paymentTransactionRepository: PaymentTransactionRepositoryBase<*>,
    private val paymentSubscriptionRepository: PaymentSubscriptionRepositoryBase<*>,
) {
    fun billingAddCard(
        authentication: MosaicAuthenticatedToken,
        traceId: String,
        request: AddCardTypeKrRequest,
    ): Result<PaymentBilling> {
        return pgRouter.getDefault().addBillingCard(authentication, traceId, request)
    }

    fun getCardList(userId: String): List<PaymentBilling> {
        return paymentBillingRepository.findAllByUserId(userId)
            .filter { !it.deleted }
    }

    fun deletePaymentMethod(authentication: MosaicAuthenticatedToken, traceId: String, billingId: String): Result<SimpleSuccess> {
        val billing = paymentBillingRepository.findAllByUserIdAndId(authentication.userId, billingId)
            ?: return Result.failure(UserErrorMessageException(
                HttpStatus.NOT_FOUND,
                "Card not found"
            ))

        return pgRouter.getPg(billing.pg)
            .removeBillingMethod(authentication, traceId, billing)
            .onSuccess {
                billing.deleted = true
                billing.secret = null
                paymentBillingRepository.saveEntity(billing)
            }
    }

    fun getCouponByCode(code: String): PaymentCoupon? {
        return paymentCouponRepository.findByCode(code)
    }

    fun validateCouponForGoods(
        userId: String,
        code: String,
        goodsId: String,
        optionId: String?,
    ): CouponResult? {
        val coupon = paymentCouponRepository.findByCode(code) ?: return null
        val usage = paymentCouponRepository.getUsage(coupon)
        val goods = paymentGoodsRepository.findById(goodsId).getOrNull() ?: return null

        // TODO: Add logic here to check if the coupon is applicable to the given goodsId.
        // This might involve checking a field in the PaymentCoupon entity or a separate mapping table.
        // For now, assuming all coupons are applicable to all goods for demonstration.

        if (usage.remaining <= 0) {
            return CouponResult.SoldOut(coupon, goods)
        }

        if (coupon.oncePerUser && paymentTransactionRepository.hasCouponUsed(userId, coupon.id)) {
            return CouponResult.AlreadyUsed(coupon, goods)
        }

        val option = optionId?.let { targetOptionId ->
            goods.options?.find {
                it.id == targetOptionId
            } ?: throw IllegalArgumentException("unknown option")
        }
        val realPrice = goods.basePrice.plus(option?.additionalPrice ?: BigDecimal.ZERO)
        val appliedPrice = coupon.discounts.map { discount ->
            when (coupon.type) {
                CouponType.AMOUNT -> realPrice - discount.value.toBigDecimal()
                CouponType.PERCENTAGE -> realPrice * (100 - discount.value).toBigDecimal() / 100.toBigDecimal()
            }
        }
        return CouponResult.Usable(coupon, goods, appliedPrice)
    }

    @Throws(UserErrorMessageException::class)
    fun getGoods(
        goodsId: String,
        optionId: String?,
    ): Pair<PaymentGoods, GoodsOption?> {
        val goods = paymentGoodsRepository.findById(goodsId).getOrNull()
            ?: throw UserErrorMessageException(
                HttpStatus.NOT_FOUND,
                "Goods not found"
            )
        val option = optionId?.let { targetOptionId ->
            goods.options?.find { it.id == targetOptionId }
                ?: throw UserErrorMessageException(
                    HttpStatus.NOT_FOUND,
                    "Option not found"
                )
        }
        return Pair(goods, option)
    }

    fun calculateDiscountedAmount(
        authentication: MosaicAuthenticatedToken,
        goodsId: String,
        optionId: String?,
        couponId: String?,
    ): BigDecimal {
        val (goods, option) = getGoods(goodsId, optionId)
        var calculatedAmount = goods.basePrice + (option?.additionalPrice ?: BigDecimal.ZERO)
        if (couponId != null) {
            val couponResult = validateCouponForGoods(authentication.userId, couponId, goodsId, optionId)
            if (couponResult is CouponResult.Usable) {
                calculatedAmount = couponResult.appliedAmounts.firstOrNull() ?: calculatedAmount
            } else {
                throw UserErrorMessageException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid coupon"
                )
            }
        }
        return calculatedAmount
    }

    @Transactional
    fun startSubscription(
        authentication: MosaicAuthenticatedToken,
        traceId: String,
        billingId: String,
        goodsId: String,
        optionId: String?,
        couponId: String?,
        firstAmount: BigDecimal,
        billingCycle: Int = 30,
        isUpdate: SubscriptionUpdateType? = null,
        subscription: PaymentSubscription? = null,
    ): PaymentSubscription {
        // 1. 금액 검증은 caller 에서 해야 함.

        // 2. goods, option 검증
        val (goods, option) = getGoods(goodsId, optionId)

        // 3. billing 확인
        val billing = paymentBillingRepository.findAllByUserIdAndId(authentication.userId, billingId)
            ?: throw UserErrorMessageException(
                HttpStatus.NOT_FOUND,
                "Billing method not found"
            )

        // 4. 이전 subscription 확인
        val latest = subscription ?: getCurrentSubscription(authentication, goodsId)
        val latestIsActive = latest?.let { it.active && isSubscriptionAvailable(it) } ?: false
        if (isUpdate == null && latestIsActive) {
            throw UserErrorMessageException(
                HttpStatus.CONFLICT,
                "Active subscription already exists for this goods"
            )
        } else if (isUpdate != null && !latestIsActive) {
            throw UserErrorMessageException(
                HttpStatus.CONFLICT,
                "No existing subscription"
            )
        }

        val now = Instant.now()
        when (isUpdate) {
            SubscriptionUpdateType.NOW -> {
                latest!!
                latest.active = false
                latest.validTo = now
                paymentSubscriptionRepository.saveEntity(latest)
            }
            SubscriptionUpdateType.NEXT -> {
                latest!!
                latest.active = false
                paymentSubscriptionRepository.saveEntity(latest)
            }
            else -> {}
        }
        val validFrom = latest?.validTo ?: now

        // 4. subscription 추가
        val subscriptionInput = PaymentSubscriptionInput(
            createdAt = now,
            traceId = traceId,
            userId = authentication.userId,
            goodsId = goodsId,
            version = latest?.version ?: 1,
            optionId = optionId,
            usedCouponIds = if (couponId != null) listOf(couponId) else null,
            billingId = billing.id,
            active = true,
            pgData = emptyMap<String, Any>(),
            billingCycle = billingCycle,
            validFrom = validFrom,
            validTo = validFrom.plus(billingCycle.toLong(), ChronoUnit.DAYS),
        )
        val subscription = paymentSubscriptionRepository.save(subscriptionInput)

        val paymentResult = pgRouter.getPg(billing.pg).processBillingPayment(
            authentication,
            traceId,
            billing,
            SubmitBillingPayment(
                goodsId = goods.id,
                goodsName = goods.name + option?.name.let { ": $it" },
                subscriptionId = subscription.id,
                amount = firstAmount,
            )
        ).getOrThrow()

        return subscription
    }

    @Transactional
    fun cancelSubscription(
        authentication: MosaicAuthenticatedToken,
        goodsId: String,
        subscription: PaymentSubscription? = null,
    ) {
        val latest = subscription ?: paymentSubscriptionRepository.findLatestByUserIdAndGoodsId(
            userId = authentication.userId,
            goodsId = goodsId,
        ) ?: throw UserErrorMessageException(
            HttpStatus.NOT_FOUND,
            "no existing subscription"
        )
        latest.active = false
        paymentSubscriptionRepository.saveEntity(latest)
    }

    fun findSubscriptions(
        authentication: MosaicAuthenticatedToken,
        goodsId: String?,
        active: Boolean?,
        pageRequest: PageRequest,
    ): Page<out PaymentSubscription> {
        val now = Instant.now()
        return paymentSubscriptionRepository.findSubscriptions(
            authentication.userId,
            goodsId,
            active,
            pageRequest,
        ).map {
            it.active = isSubscriptionAvailable(it, now)
            it
        }
    }

    fun getCurrentSubscription(
        authentication: MosaicAuthenticatedToken,
        goodsId: String,
    ): PaymentSubscription? {
        val subscription = paymentSubscriptionRepository.findLatestByUserIdAndGoodsId(
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
        return subscription.validTo.isAfter(now)
    }
}
