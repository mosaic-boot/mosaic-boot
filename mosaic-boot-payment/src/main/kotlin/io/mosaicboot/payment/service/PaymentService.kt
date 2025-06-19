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
import io.mosaicboot.payment.dto.CouponResult
import kotlin.jvm.optionals.getOrNull

@Service
class PaymentService(
    private val pgRouter: PgRouter,
    private val paymentBillingRepository: PaymentBillingRepositoryBase<*>,
    private val paymentCouponRepository: PaymentCouponRepositoryBase<*>,
    private val paymentGoodsRepository: PaymentGoodsRepositoryBase<*>,
    private val paymentTransactionRepository: PaymentTransactionRepositoryBase<*>,
) {
    fun billingAddCard(
        authentication: MosaicAuthenticatedToken,
        request: AddCardTypeKrRequest,
    ): Result<PaymentBilling> {
        return pgRouter.getDefault().billingAddCard(authentication, request)
    }

    fun getCardList(userId: String): List<PaymentBilling> {
        return paymentBillingRepository.findAllByUserId(userId)
            .filter { !it.deleted }
    }

    fun deletePaymentMethod(authentication: MosaicAuthenticatedToken, billingId: String): Result<SimpleSuccess> {
        val billing = paymentBillingRepository.findAllByUserIdAndId(authentication.userId, billingId)
            ?: return Result.failure(UserErrorMessageException("Card not found"))

        return pgRouter.getPg(billing.pg)
            .billingDelete(authentication, billing)
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
        val realPrice = goods.basePrice + (option?.additionalPrice ?: 0).toBigDecimal()
        val appliedPrice = coupon.discounts.map { discount ->
            when (coupon.type) {
                CouponType.AMOUNT -> realPrice - discount.value.toBigDecimal()
                CouponType.PERCENTAGE -> realPrice * (100 - discount.value).toBigDecimal() / 100.toBigDecimal()
            }
        }
        return CouponResult.Usable(coupon, goods, appliedPrice)
    }
}
