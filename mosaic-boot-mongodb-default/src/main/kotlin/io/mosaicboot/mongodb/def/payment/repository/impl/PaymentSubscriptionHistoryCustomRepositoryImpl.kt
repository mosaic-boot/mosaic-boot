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

import io.mosaicboot.mongodb.def.payment.entity.PaymentPaymentSubscriptionHistoryEntity
import io.mosaicboot.mongodb.def.payment.repository.PaymentSubscriptionHistoryCustomRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class PaymentSubscriptionHistoryCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : PaymentSubscriptionHistoryCustomRepository {

    override fun findAllBySubscriptionId(subscriptionId: String): List<PaymentPaymentSubscriptionHistoryEntity> {
        val query = Query(Criteria.where("subscriptionId").`is`(subscriptionId))
        return mongoTemplate.find(query, PaymentPaymentSubscriptionHistoryEntity::class.java)
    }
}
