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

import io.mosaicboot.mongodb.def.payment.entity.PaymentSubscriptionRenewEntity
import io.mosaicboot.mongodb.def.payment.repository.PaymentSubscriptionRenewCustomRepository
import io.mosaicboot.payment.db.entity.PaymentSubscriptionRenewInput
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate

class PaymentSubscriptionRenewCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : PaymentSubscriptionRenewCustomRepository {
    override fun saveOrIgnore(input: PaymentSubscriptionRenewInput): PaymentSubscriptionRenewEntity? {
        val entity = PaymentSubscriptionRenewEntity(
            id = input.id,
            createdAt = input.createdAt,
            updatedAt = input.updatedAt,
            userId = input.userId,
            subscriptionId = input.subscriptionId,
            idempotentKey = input.idempotentKey,
            paymentCount = input.paymentCount,
            status = input.status
        )
        return try {
            mongoTemplate.insert(entity)
        } catch (e: DuplicateKeyException) {
            null
        }
    }
}