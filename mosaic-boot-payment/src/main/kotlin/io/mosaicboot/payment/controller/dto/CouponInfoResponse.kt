package io.mosaicboot.payment.controller.dto

import io.mosaicboot.payment.db.dto.PaymentCouponDiscount
import java.math.BigDecimal

data class CouponInfoResponse(
    val id: String,
    val type: String,
    val discounts: List<PaymentCouponDiscount>,
    val appliedAmounts: List<BigDecimal>,
)
