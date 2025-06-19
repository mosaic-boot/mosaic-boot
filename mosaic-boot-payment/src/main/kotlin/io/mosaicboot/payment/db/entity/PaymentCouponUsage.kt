package io.mosaicboot.payment.db.entity

import io.mosaicboot.data.entity.UpdatableEntity

/**
 * id is same of [PaymentCoupon]
 */
interface PaymentCouponUsage : UpdatableEntity<String> {
    val remaining: Int
}