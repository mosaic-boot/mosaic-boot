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
import io.mosaicboot.payment.db.repository.PaymentSubscriptionLogRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentTransactionRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentSubscriptionRepositoryBase
import io.mosaicboot.payment.dto.CouponResult
import io.mosaicboot.payment.dto.SubmitBillingPayment
import io.mosaicboot.payment.db.dto.PaymentSubscriptionInput
import io.mosaicboot.payment.db.dto.PaymentSubscriptionLogInput
import io.mosaicboot.payment.db.entity.GoodsOption
import io.mosaicboot.payment.db.entity.PaymentGoods
import io.mosaicboot.payment.db.entity.PaymentSubscription
import io.mosaicboot.payment.db.entity.PaymentTransaction
import io.mosaicboot.payment.db.entity.SubscriptionStatus
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
    private val paymentSubscriptionLogRepository: PaymentSubscriptionLogRepositoryBase<*>,
) {
    fun billingAddCard(
        authentication: MosaicAuthenticatedToken,
        traceId: String,
        request: AddCardTypeKrRequest,
    ): Result<PaymentBilling> {
        val noPrimary = paymentBillingRepository.findAllByUserId(authentication.userId)
            .find { it.primary } == null
        return pgRouter.getDefault().addBillingCard(authentication.userId, traceId, request.copy(
            primary = request.primary || noPrimary,
        ))
    }

    fun getCardList(userId: String): List<PaymentBilling> {
        return paymentBillingRepository.findAllByUserId(userId)
            .filter { !it.deleted }
    }

    @Transactional
    fun setPrimaryPaymentMethod(authentication: MosaicAuthenticatedToken, billingId: String): Result<SimpleSuccess> {
        val updated = paymentBillingRepository.updatePrimary(authentication.userId, billingId)
            ?: return Result.failure(UserErrorMessageException(
                HttpStatus.NOT_FOUND,
                "Card not found"
            ))

        return Result.success(SimpleSuccess())
    }

    fun deletePaymentMethod(authentication: MosaicAuthenticatedToken, traceId: String, billingId: String): Result<SimpleSuccess> {
        val billing = paymentBillingRepository.findByUserIdAndId(authentication.userId, billingId)
            ?: return Result.failure(UserErrorMessageException(
                HttpStatus.NOT_FOUND,
                "Card not found"
            ))

        return pgRouter.getPg(billing.pg)
            .removeBillingMethod(authentication.userId, traceId, billing)
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

    /**
     * 첫 번째 금액
     * TODO: TEST CODE 필요
     */
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

    /**
     * 이후 결제 시 계산
     * TODO: TEST CODE 필요
     */
    fun calculateDiscountedAmount2(
        goodsId: String,
        optionId: String?,
        couponId: String?,
        paymentCount: Int,
    ): BigDecimal {
        val (goods, option) = getGoods(goodsId, optionId)
        var calculatedAmount = goods.basePrice + (option?.additionalPrice ?: BigDecimal.ZERO)

        if (couponId != null) {
            val coupon = paymentCouponRepository.findById(couponId).getOrNull()
                ?: throw UserErrorMessageException(HttpStatus.NOT_FOUND, "Coupon not found")

            var cumulativePeriod = 0
            val discount = coupon.discounts.find {
                if ((paymentCount >= cumulativePeriod && paymentCount < cumulativePeriod + it.period) || it.period == 0) {
                    true
                } else {
                    cumulativePeriod += it.period
                    false
                }
            }

            if (discount != null) {
                calculatedAmount = when (coupon.type) {
                    CouponType.AMOUNT -> calculatedAmount - discount.value.toBigDecimal()
                    CouponType.PERCENTAGE -> calculatedAmount * (100 - discount.value).toBigDecimal() / 100.toBigDecimal()
                }
            }
        }

        return calculatedAmount
    }

    fun getTransactionRecipeUrl(transaction: PaymentTransaction): String {
        return pgRouter.getPg(transaction.pg).getTransactionRecipeUrl(transaction)
    }
}
