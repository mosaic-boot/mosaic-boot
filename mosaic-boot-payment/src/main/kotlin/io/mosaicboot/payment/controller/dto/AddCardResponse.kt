package io.mosaicboot.payment.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class AddCardResponse(
    @field:JsonProperty("billingId")
    val billingId: String,
    @field:JsonProperty("alias")
    val alias: String,
)
