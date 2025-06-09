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
import io.mosaicboot.mongodb.def.payment.entity.PaymentBillingEntity
import io.mosaicboot.mongodb.def.payment.entity.PaymentTransactionEntity
import io.mosaicboot.payment.db.dto.PaymentBillingInput
import io.mosaicboot.payment.db.dto.PaymentTransactionInput
import io.mosaicboot.payment.db.repository.PaymentBillingRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentTransactionRepositoryBase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(prefix = "mosaic.datasource.mongodb.collections.payment-billing", name = ["customized"], havingValue = "false", matchIfMissing = true)
interface PaymentBillingRepository :
    MongoRepository<PaymentBillingEntity, String>,
    PaymentBillingRepositoryBase<PaymentBillingEntity>
{
    override fun save(input: PaymentBillingInput): PaymentBillingEntity {
        return save(PaymentBillingEntity(
            id = UUIDv7.generate().toString(),
            createdAt = input.createdAt,
            updatedAt = input.createdAt,
            userId = input.userId,
            pg = input.pg,
            deleted = false,
            secret = input.secret,
            addCardTxId = input.addCardTxId,
            deleteCardTxId = null,
        ))
    }
}