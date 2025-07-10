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

package io.mosaicboot.payment.db.dto

import io.mosaicboot.payment.db.entity.SubscriptionStatus
import java.time.Instant

data class PaymentSubscriptionInput(
    val createdAt: Instant = Instant.now(),
    val traceId: String,
    val userId: String,
    val goodsId: String,
    val optionId: String?,
    val version: Int,
    val usedCouponId: String?,
    val billingId: String,
    val status: SubscriptionStatus,
    val customData: Map<String, *> = emptyMap<String, Any>(),
    /**
     * Days
     */
    val billingCycle: Int,
    val validFrom: Instant,
    val validTo: Instant,
    val scheduledOptionId: String?,
)
