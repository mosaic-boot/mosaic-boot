package io.mosaicboot.payment.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class StartSubscriptionRequest(
    @JsonProperty("goodsId")
    val goodsId: String,
    @JsonProperty("optionId")
    val optionId: String?,
    @JsonProperty("couponId")
    val couponId: String?,
    @JsonProperty("firstAmount")
    val firstAmount: BigDecimal,
)