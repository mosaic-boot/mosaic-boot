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

import io.mosaicboot.mongodb.def.payment.entity.PaymentCouponEntity
import io.mosaicboot.mongodb.def.payment.entity.PaymentCouponUsageEntity
import io.mosaicboot.mongodb.def.payment.repository.PaymentCouponCustomRepository
import io.mosaicboot.payment.db.dto.PaymentCouponInput
import io.mosaicboot.payment.db.entity.PaymentCoupon
import io.mosaicboot.payment.db.entity.PaymentCouponUsage
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.Instant
import java.util.UUID

class PaymentCouponCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : PaymentCouponCustomRepository {
    override fun save(input: PaymentCouponInput): PaymentCouponEntity {
        return mongoTemplate.save(PaymentCouponEntity(
            id = UUID.randomUUID().toString(),
            createdAt = input.createdAt,
            updatedAt = input.createdAt,
            code = input.code,
            count = input.count,
            type = input.type,
            oncePerUser = input.oncePerUser,
            discounts = input.discounts,
        ))
    }

    override fun findAndDecrementRemainingCount(code: String): Pair<PaymentCouponEntity, Boolean>? {
        val coupon = findCouponByCode(code) ?: return null
        val usage = upsertUsage(
            Query.query(
                Criteria("_id").`is`(coupon.id)
                    .and("remaining").gt(0)
            ),
            coupon,
        ) { update ->
            update.set("updatedAt", Instant.now())
            update.inc("remaining", -1)
        }
        // FIXME: error cause
        return Pair(coupon, usage != null)
    }

    override fun getUsage(id: String): PaymentCouponUsage {
        val coupon = mongoTemplate.findOne(
            Query.query(
                Criteria.where("_id").`is`(id)
            ),
            PaymentCouponEntity::class.java
        )
        return getUsage(coupon!!)
    }

    override fun getUsage(coupon: PaymentCoupon): PaymentCouponUsage {
        return upsertUsage(
            Query.query(
                Criteria.where("_id").`is`(coupon.id),
            ),
            coupon
        ) {}
    }

    private fun upsertUsage(query: Query, coupon: PaymentCoupon, fn: (update: Update) -> Unit): PaymentCouponUsage {
        val now = Instant.now()
        return mongoTemplate.findAndModify(
            query,
            Update()
                .setOnInsert("_id", coupon.id)
                .setOnInsert("createdAt", now)
                .setOnInsert("updatedAt", now)
                .setOnInsert("remaining", coupon.count)
                .also {
                    fn(it)
                },
            FindAndModifyOptions.options()
                .upsert(true)
                .returnNew(true),
            PaymentCouponUsageEntity::class.java,
        )!!
    }

    private fun findCouponByCode(code: String): PaymentCouponEntity? {
        return mongoTemplate.findOne(
            Query.query(
                Criteria.where("code").`is`(code)
            ),
            PaymentCouponEntity::class.java
        )
    }
}
