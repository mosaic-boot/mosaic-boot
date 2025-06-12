package io.mosaicboot.payment.db.dto

import java.time.Instant

data class PaymentBillingInput(
    val createdAt: Instant,
    val userId: String,
    val pg: String,

    val alias: String,

    /**
     * JWE Encrypted Data
     */
    val secret: String,

    val addCardTxId: String?,
)
