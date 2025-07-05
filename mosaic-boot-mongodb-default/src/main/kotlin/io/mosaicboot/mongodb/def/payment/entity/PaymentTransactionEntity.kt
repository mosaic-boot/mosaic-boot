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

package io.mosaicboot.mongodb.def.payment.entity

import io.mosaicboot.payment.db.dto.OrderStatus
import io.mosaicboot.payment.db.dto.TransactionType
import io.mosaicboot.payment.db.dto.VbankInfo
import io.mosaicboot.payment.db.entity.PaymentTransaction
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@Document(collection = "\${mosaic.datasource.mongodb.collections.payment-transaction.collection:payment.transactions}")
class PaymentTransactionEntity(
    @Id
    override val id: String,
    override val createdAt: Instant,
    override var updatedAt: Instant,
    override val userId: String,
    override val traceId: String,
    override val type: TransactionType,
    override val pg: String,
    @Indexed(unique = true)
    override val pgUniqueId: String,
    override var pgData: Map<String, *>?,

    override val goodsId: String?,
    override val goodsName: String?,
    override val subscriptionId: String?,
    override val usedCouponIds: List<String>?,
    override val amount: BigDecimal?,

    override var orderStatus: OrderStatus,
    override var paidAt: Instant? = null,
    override var cancelledAt: Instant? = null,
    override var message: String? = null,
    override var vbank: VbankInfo? = null,
) : PaymentTransaction