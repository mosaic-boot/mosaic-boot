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

package io.mosaicboot.mongodb.def.payment.repository

import io.mosaicboot.core.util.UUIDv7
import io.mosaicboot.mongodb.def.payment.entity.PaymentSubscriptionLogEntity
import io.mosaicboot.payment.db.dto.PaymentSubscriptionLogInput
import io.mosaicboot.payment.db.repository.PaymentSubscriptionLogRepositoryBase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import kotlin.String

@Repository
@ConditionalOnProperty(prefix = "mosaic.datasource.mongodb.collections.payment-subscription-log.collection", name = ["customized"], havingValue = "false", matchIfMissing = true)
interface PaymentSubscriptionLogRepository :
    MongoRepository<PaymentSubscriptionLogEntity, String>,
    PaymentSubscriptionLogRepositoryBase<PaymentSubscriptionLogEntity>,
    PaymentSubscriptionLogCustomRepository
{
    override fun save(input: PaymentSubscriptionLogInput): PaymentSubscriptionLogEntity {
        return save(
            PaymentSubscriptionLogEntity(
                id = UUIDv7.generate().toString(),
                createdAt = Instant.now(),
                userId = input.userId,
                subscriptionId = input.subscriptionId,
                traceId = input.traceId,
                status = input.status,
                fromOptionId = input.fromOptionId,
                toOptionId = input.toOptionId,
                reason = input.reason,
            )
        )
    }
}