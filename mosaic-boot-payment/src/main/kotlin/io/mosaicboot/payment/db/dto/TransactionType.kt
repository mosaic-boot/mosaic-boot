package io.mosaicboot.payment.db.dto

enum class TransactionType(val value: String) {
    BILLING_ADD_CARD("billing.add-card"),
    BILLING_REMOVE_CARD("billing.remove-card"),
    BILLING_TEST("billing.test"),
    ORDER("order"),
    ;
}