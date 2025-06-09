package io.mosaicboot.payment.nicepay.dto

import io.mosaicboot.core.jwt.JwtContentType

@JwtContentType("nicepay.billing")
data class NicepayBillingData(
    val bid: String,
    val authDate: String?,
    val cardCode: String?,
    val cardName: String?,
)
