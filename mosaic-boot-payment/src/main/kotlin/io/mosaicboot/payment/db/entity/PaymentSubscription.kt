/**
 * Copyright 2025 JC-Lab (mosaicboot.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mosaicboot.payment.db.entity

import io.mosaicboot.data.iface.UserRelatedObject
import io.mosaicboot.core.util.CryptoUtils
import io.mosaicboot.data.entity.UpdatableEntity
import java.time.Instant

/**
 * ID : UUIDv7
 */
interface PaymentSubscription : UpdatableEntity<String>, UserRelatedObject {
    override val userId: String
    val traceId: String
    val billingId: String
    val customData: Map<String, *>
    val goodsId: String
    val optionId: String?

    /**
     * Version for when userId and goodsId are the same.
     */
    val version: Int

    /**
     * must be unique key
     */
    val idempotentKey: String

    val billingCycle: Int

    val usedCouponIds: List<String>?
    var enabled: Boolean
    var validFrom: Instant
    var validTo: Instant

    var deleted: Boolean
    val prevSubscriptionId: String?
}

fun subscriptionIdempotentKey(userId: String, goodsId: String, version: Int): String {
    return CryptoUtils.sha256ForKey(userId, goodsId, version.toString())
}