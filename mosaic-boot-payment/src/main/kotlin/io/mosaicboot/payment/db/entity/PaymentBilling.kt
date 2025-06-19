package io.mosaicboot.payment.db.entity

import io.mosaicboot.data.entity.UpdatableEntity

interface PaymentBilling : UpdatableEntity<String> {
    val userId: String
    val pg: String
    var deleted: Boolean

    var alias: String

    var description: String

    /**
     * JWE Encrypted Data
     */
    var secret: String?

    val addCardTxId: String?
    var deletePaymentMethodTxId: String?
}