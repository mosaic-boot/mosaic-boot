package io.mosaicboot.payment.db.repository

import io.mosaicboot.payment.db.dto.PaymentCouponInput
import io.mosaicboot.payment.db.entity.PaymentCoupon
import io.mosaicboot.payment.db.entity.PaymentCouponUsage

interface PaymentCouponMosaicRepository<T : PaymentCoupon> {
    fun save(input: PaymentCouponInput): T
    fun findAndDecrementRemainingCount(code: String): Pair<T, Boolean>?

    fun getUsage(id: String): PaymentCouponUsage
    fun getUsage(coupon: PaymentCoupon): PaymentCouponUsage
}