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

import io.mosaicboot.data.entity.UpdatableEntity
import io.mosaicboot.payment.db.entity.PaymentCouponUsage
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "\${mosaic.datasource.mongodb.collections.payment-coupon-usage.collection:payment.couponUsages}")
data class PaymentCouponUsageEntity(
    @Id
    override val id: String,
    override val createdAt: Instant,
    override var updatedAt: Instant,
    override val remaining: Int,
) : PaymentCouponUsage, UpdatableEntity<String>
