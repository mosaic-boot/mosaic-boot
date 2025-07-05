package io.mosaicboot.payment.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BillingMethod(
    @field:JsonProperty("billingId")
    val billingId: String,
    @field:JsonProperty("alias")
    val alias: String,
)
