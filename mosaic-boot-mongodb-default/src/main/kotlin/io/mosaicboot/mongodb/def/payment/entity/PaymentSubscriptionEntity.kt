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

package io.mosaicboot.mongodb.def.payment.entity

import io.mosaicboot.payment.db.entity.PaymentSubscription
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "\${mosaic.datasource.mongodb.collections.payment-subscription.collection:payment.subscriptions}")
data class PaymentSubscriptionEntity(
    @Id
    override val id: String,
    override val createdAt: Instant,
    override var updatedAt: Instant,
    override val traceId: String,
    @Indexed(unique = false)
    override val userId: String,
    override val goodsId: String,
    override val optionId: String?,
    override val version: Int,
    @Indexed(unique = true)
    override val idempotentKey: String,
    override val usedCouponIds: List<String>?,
    override val billingId: String,
    override val billingCycle: Int,
    override val customData: Map<String, *>,
    override var enabled: Boolean,
    override var validFrom: Instant,
    override var validTo: Instant,
    override var deleted: Boolean,
    override val prevSubscriptionId: String?,
) : PaymentSubscription
