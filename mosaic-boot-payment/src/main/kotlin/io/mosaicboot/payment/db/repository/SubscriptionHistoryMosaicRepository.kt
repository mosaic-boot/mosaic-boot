package io.mosaicboot.payment.db.repository

import io.mosaicboot.payment.db.dto.SubscriptionHistoryInput
import io.mosaicboot.payment.db.entity.PaymentSubscriptionHistory

interface SubscriptionHistoryMosaicRepository<T : PaymentSubscriptionHistory> {
    fun save(input: SubscriptionHistoryInput): T
}