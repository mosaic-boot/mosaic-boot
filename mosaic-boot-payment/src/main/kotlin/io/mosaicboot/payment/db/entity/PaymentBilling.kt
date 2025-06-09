package io.mosaicboot.payment.db.entity

import io.mosaicboot.core.entity.UpdatableEntity

interface PaymentBilling : UpdatableEntity<String> {
    val userId: String
    val pg: String
    val deleted: Boolean

    /**
     * JWE Encrypted Data
     */
    val secret: String

    val addCardTxId: String?
    var deleteCardTxId: String?
}