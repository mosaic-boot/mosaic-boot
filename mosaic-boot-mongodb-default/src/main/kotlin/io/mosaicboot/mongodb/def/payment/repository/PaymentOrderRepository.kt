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
import io.mosaicboot.mongodb.def.payment.entity.PaymentOrderEntity
import io.mosaicboot.payment.db.dto.PaymentOrderInput
import io.mosaicboot.payment.db.repository.PaymentOrderRepositoryBase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(prefix = "mosaic.datasource.mongodb.collections.payment-order", name = ["customized"], havingValue = "false", matchIfMissing = true)
interface PaymentOrderRepository :
    MongoRepository<PaymentOrderEntity, String>,
    PaymentOrderRepositoryBase<PaymentOrderEntity>,
    PaymentOrderCustomRepository
{
    override fun save(input: PaymentOrderInput): PaymentOrderEntity {
        return save(PaymentOrderEntity(
            id = UUIDv7.generate().toString(),
            createdAt = input.createdAt,
            updatedAt = input.createdAt,
            userId = input.userId,
            goodsId = input.goodsId,
            goodsName = input.goodsName,
            subscriptionId = input.subscriptionId,
            pg = input.pg,
            orderId = input.orderId,
            status = input.status,
            amount = input.amount,
            message = input.message,
            data = input.data,
        ))
    }
}