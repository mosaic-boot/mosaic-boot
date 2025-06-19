package io.mosaicboot.payment.db.dto

import io.mosaicboot.payment.db.entity.CouponType
import java.time.Instant

data class PaymentCouponInput(
    val createdAt: Instant,
    val code: String,
    val count: Long,
    val type: CouponType,
    val oncePerUser: Boolean,
    val discounts: List<PaymentCouponDiscount>,
)