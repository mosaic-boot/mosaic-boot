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
import io.mosaicboot.mongodb.def.payment.entity.PaymentTransactionEntity
import io.mosaicboot.mongodb.def.payment.repository.PaymentTransactionCustomRepository
import io.mosaicboot.mongodb.def.repository.impl.Paged
import io.mosaicboot.mongodb.def.repository.impl.pagedAggregation
import io.mosaicboot.payment.db.dto.PaymentTransactionInput
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo

class PaymentTransactionCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : PaymentTransactionCustomRepository {
    override fun save(input: PaymentTransactionInput): PaymentTransactionEntity {
        return mongoTemplate.save(PaymentTransactionEntity(
            id = input.id ?: UUIDv7.generate().toString(),
            createdAt = input.createdAt,
            updatedAt = input.createdAt,
            userId = input.userId,
            traceId = input.traceId,
            type = input.type,
            pg = input.pg,
            pgUniqueId = input.pgUniqueId,
            pgData = input.pgData,

            goodsId = input.goodsId,
            goodsName = input.goodsName,
            subscriptionId = input.subscriptionId,
            usedCouponIds = input.usedCouponIds,
            amount = input.amount,

            orderStatus = input.orderStatus,
            message = input.message,
        ))
    }

    override fun getOrderListByUserIdWithPaged(
        userId: String,
        pageable: Pageable,
    ): Page<PaymentTransactionEntity> {
        return mongoTemplate.pagedAggregation(
            pageable,
            Aggregation.newAggregation(
                Aggregation.match(
                    Criteria("userId").isEqualTo(userId)
                ),
            ),
            PaymentTransactionEntity::class.java,
            PagedPaymentOrder::class.java,
        )
    }

    data class PagedPaymentOrder(
        @Field("total")
        override val total: Long,
        @Field("items")
        override val items: List<PaymentTransactionEntity>,
    ) : Paged<PaymentTransactionEntity>


    override fun hasCouponUsed(userId: String, couponId: String): Boolean {
        return mongoTemplate.find(
            Query.query(
                Criteria("userId").isEqualTo(userId)
                    .and("usedCouponIds").`in`(couponId)
            ),
            PaymentTransactionEntity::class.java,
        ).isNotEmpty()
    }
}