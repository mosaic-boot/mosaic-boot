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

import com.mongodb.client.result.UpdateResult
import io.mosaicboot.core.util.UUIDv7
import io.mosaicboot.mongodb.def.payment.entity.PaymentBillingEntity
import io.mosaicboot.mongodb.def.payment.repository.PaymentBillingCustomRepository
import io.mosaicboot.payment.db.dto.PaymentBillingInput
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.transaction.annotation.Transactional

open class PaymentBillingCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : PaymentBillingCustomRepository {
    @Transactional
    override fun save(input: PaymentBillingInput): PaymentBillingEntity {
        if (input.primary) {
            updateAllUnPrimary(input.userId)
        }
        return mongoTemplate.save(PaymentBillingEntity(
            id = UUIDv7.generate().toString(),
            createdAt = input.createdAt,
            updatedAt = input.createdAt,
            userId = input.userId,
            pg = input.pg,
            deleted = false,
            primary = input.primary,
            alias = input.alias,
            description = input.description,
            secret = input.secret,
            addCardTxId = input.addCardTxId,
            deletePaymentMethodTxId = null,
        ))
    }

    override fun findAllByUserId(userId: String): List<PaymentBillingEntity> {
        return mongoTemplate.find(
            Query.query(
                Criteria.where("userId").isEqualTo(userId)
                    .and("deleted").ne(true)
            ),
            PaymentBillingEntity::class.java
        )
    }

    @Transactional
    override fun updatePrimary(userId: String, newPrimaryBillingId: String): PaymentBillingEntity? {
        val updated = mongoTemplate.findAndModify(
            Query.query(
                Criteria.where("userId").isEqualTo(userId)
                    .and("_id").isEqualTo(newPrimaryBillingId)
                    .and("deleted").ne(true)
            ),
            Update()
                .set("primary", true),
            FindAndModifyOptions.options().returnNew(true),
            PaymentBillingEntity::class.java
        ) ?: return null
        mongoTemplate.updateMulti(
            Query.query(
                Criteria.where("userId").isEqualTo(userId)
                    .and("_id").ne(newPrimaryBillingId)
                    .and("deleted").ne(true)
                    .and("primary").isEqualTo(true)
            ),
            Update()
                .set("primary", false),
            PaymentBillingEntity::class.java
        )
        return updated
    }

    override fun findPrimaryByUserId(userId: String): PaymentBillingEntity? {
        return mongoTemplate.findOne(
            Query.query(
                Criteria.where("userId").isEqualTo(userId)
                    .and("deleted").ne(true)
                    .and("primary").isEqualTo(true)
            ).limit(1),
            PaymentBillingEntity::class.java
        )
    }

    fun updateAllUnPrimary(userId: String): UpdateResult {
        return mongoTemplate.updateMulti(
            Query.query(
                Criteria.where("userId").isEqualTo(userId)
                    .and("deleted").ne(true)
                    .and("primary").isEqualTo(true)
            ),
            Update()
                .set("primary", false),
            PaymentBillingEntity::class.java
        )
    }
}