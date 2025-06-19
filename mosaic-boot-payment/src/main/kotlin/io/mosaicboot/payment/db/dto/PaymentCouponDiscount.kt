package io.mosaicboot.payment.db.dto

import com.fasterxml.jackson.annotation.JsonProperty

open class PaymentCouponDiscount(
    @JsonProperty("period")
    @field:JsonProperty("period")
    val period: Int, // 0 for permanent, >=1 for number of billing cycles (e.g., 3 months)
    @JsonProperty("value")
    @field:JsonProperty("value")
    val value: Long, // Percentage (0-100) or fixed amount based on type
)