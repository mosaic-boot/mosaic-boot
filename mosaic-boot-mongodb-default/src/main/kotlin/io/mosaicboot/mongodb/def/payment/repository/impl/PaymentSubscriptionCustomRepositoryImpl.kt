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
import io.mosaicboot.mongodb.def.payment.entity.PaymentSubscriptionEntity
import io.mosaicboot.mongodb.def.payment.repository.PaymentSubscriptionCustomRepository
import io.mosaicboot.mongodb.def.repository.impl.Paged
import io.mosaicboot.mongodb.def.repository.impl.pagedAggregation
import io.mosaicboot.payment.db.dto.PaymentSubscriptionInput
import io.mosaicboot.payment.db.entity.SubscriptionStatus
import io.mosaicboot.payment.db.entity.subscriptionIdempotentKey
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo

class PaymentSubscriptionCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : PaymentSubscriptionCustomRepository {
    override fun save(input: PaymentSubscriptionInput): PaymentSubscriptionEntity {
        return mongoTemplate.save(PaymentSubscriptionEntity(
            id = UUIDv7.generate().toString(),
            createdAt = input.createdAt,
            updatedAt = input.createdAt,
            traceId = input.traceId,
            userId = input.userId,
            goodsId = input.goodsId,
            version = input.version,
            idempotentKey = subscriptionIdempotentKey(
                userId = input.userId,
                goodsId = input.goodsId,
                version = input.version,
            ),
            optionId = input.optionId,
            usedCouponIds = input.usedCouponIds,
            billingId = input.billingId,
            billingCycle = input.billingCycle,
            status = input.status,
            customData = input.customData,
            validFrom = input.validFrom,
            validTo = input.validTo,
            scheduledOptionId = input.scheduledOptionId,
        ))
    }

    override fun findLatestByUserIdAndGoodsId(
        userId: String,
        goodsId: String
    ): PaymentSubscriptionEntity? {
        return mongoTemplate.findOne(
            Query.query(
                Criteria("userId").isEqualTo(userId)
                    .and("goodsId").isEqualTo(goodsId)
            )
                .with(Sort.by(Sort.Direction.DESC, "_id"))
                .limit(1),
            PaymentSubscriptionEntity::class.java,
        )
    }

    override fun findCurrentByUserIdAndGoodsId(userId: String, goodsId: String): PaymentSubscriptionEntity? {
        return mongoTemplate.findOne(
            Query.query(
                Criteria("userId").isEqualTo(userId)
                    .and("goodsId").isEqualTo(goodsId)
                    .and("status").ne(SubscriptionStatus.CANCELED)
            )
                .with(Sort.by(Sort.Direction.DESC, "_id"))
                .limit(1),
            PaymentSubscriptionEntity::class.java,
        )
    }

    override fun findSubscriptions(
        userId: String,
        goodsId: String?,
        statuses: List<SubscriptionStatus>?,
        pageRequest: PageRequest,
    ): Page<PaymentSubscriptionEntity> {
        val sort = if (pageRequest.sort.isUnsorted) {
            Sort.by(Sort.Direction.DESC, "_id")
        } else pageRequest.sort

        return mongoTemplate.pagedAggregation(
            pageRequest,
            Aggregation.newAggregation(
                Aggregation.match(
                    Criteria("userId").isEqualTo(userId)
                        .let { criteria ->
                            if (goodsId != null) {
                                criteria.and("goodsId").isEqualTo(goodsId)
                            } else criteria
                        }
                        .let { criteria ->
                            if (statuses != null) {
                                criteria.and("status").`in`(statuses)
                            } else criteria
                        }
                ),
                Aggregation.sort(sort),
            ),
            PaymentSubscriptionEntity::class.java,
            PagedPaymentSubscriptions::class.java,
        )
    }

    data class PagedPaymentSubscriptions(
        @Field("total")
        override val total: Long,
        @Field("items")
        override val items: List<PaymentSubscriptionEntity>,
    ) : Paged<PaymentSubscriptionEntity>
}
