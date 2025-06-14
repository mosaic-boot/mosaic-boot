package io.mosaicboot.payment.db.entity

import io.mosaicboot.data.entity.UpdatableEntity

interface PaymentBilling : UpdatableEntity<String> {
    val userId: String
    val pg: String
    val deleted: Boolean

    val alias: String

    /**
     * JWE Encrypted Data
     */
    val secret: String

    val addCardTxId: String?
    var deleteCardTxId: String?
}