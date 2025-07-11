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

package io.mosaicboot.payment.db.dto

import java.math.BigDecimal
import java.time.Instant

data class PaymentTransactionInput(
    val id: String? = null,
    val userId: String,
    val createdAt: Instant,
    val traceId: String,
    val type: TransactionType,
    val paymentMethodAlias: String,
    val billingId: String? = null,
    val pg: String,
    val pgUniqueId: String,
    var pgData: Map<String, *>? = null,

    val goodsId: String? = null,
    val goodsName: String? = null,
    val subscriptionId: String? = null,
    val usedCouponId: String? = null,
    val amount: BigDecimal? = null,

    var orderStatus: OrderStatus,
    var message: String? = null,
)