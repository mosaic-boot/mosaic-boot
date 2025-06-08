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

import io.mosaicboot.mongodb.def.payment.entity.PaymentLogEntity
import io.mosaicboot.payment.db.dto.PaymentLogInput
import io.mosaicboot.payment.db.repository.PaymentLogRepositoryBase
import org.bson.types.ObjectId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(prefix = "mosaic.datasource.mongodb.collections.payment-log", name = ["customized"], havingValue = "false", matchIfMissing = true)
interface PaymentLogRepository :
    MongoRepository<PaymentLogEntity, ObjectId>,
    PaymentLogRepositoryBase<PaymentLogEntity, ObjectId>
{
    override fun save(input: PaymentLogInput): PaymentLogEntity {
        return save(PaymentLogEntity(
            id = ObjectId.get(),
            createdAt = input.createdAt,
            pg = input.pg,
            type = input.type,
            orderId = input.orderId,
            data = input.data,
        ))
    }
}