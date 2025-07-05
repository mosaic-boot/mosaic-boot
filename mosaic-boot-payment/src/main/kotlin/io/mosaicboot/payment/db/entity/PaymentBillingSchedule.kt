package io.mosaicboot.payment.db.entity

import io.mosaicboot.data.entity.UpdatableEntity
import io.mosaicboot.data.iface.UserRelatedObject

interface PaymentBillingSchedule : UpdatableEntity<String>, UserRelatedObject {
    override val userId: String
    val subscriptionId: String
}