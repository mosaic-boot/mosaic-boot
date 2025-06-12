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

package io.mosaicboot.mongodb.def.payment.repository.impl

import io.mosaicboot.core.util.UUIDv7
import io.mosaicboot.mongodb.def.payment.entity.PaymentBillingEntity
import io.mosaicboot.mongodb.def.payment.repository.PaymentBillingCustomRepository
import io.mosaicboot.payment.db.dto.PaymentBillingInput
import org.springframework.data.mongodb.core.MongoTemplate

class PaymentBillingCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : PaymentBillingCustomRepository {
    override fun save(input: PaymentBillingInput): PaymentBillingEntity {
        return mongoTemplate.save(PaymentBillingEntity(
            id = UUIDv7.generate().toString(),
            createdAt = input.createdAt,
            updatedAt = input.createdAt,
            userId = input.userId,
            pg = input.pg,
            deleted = false,
            alias = input.alias,
            secret = input.secret,
            addCardTxId = input.addCardTxId,
            deleteCardTxId = null,
        ))
    }
}