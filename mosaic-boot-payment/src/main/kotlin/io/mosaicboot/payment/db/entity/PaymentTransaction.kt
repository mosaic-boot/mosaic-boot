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

package io.mosaicboot.payment.db.entity

import io.mosaicboot.data.iface.UserRelatedObject
import io.mosaicboot.data.entity.UpdatableEntity
import io.mosaicboot.payment.db.dto.OrderStatus
import io.mosaicboot.payment.db.dto.TransactionType
import io.mosaicboot.payment.db.dto.VbankInfo
import java.math.BigDecimal
import java.time.Instant

interface PaymentTransaction : UpdatableEntity<String>, UserRelatedObject {
    override val userId: String
    val traceId: String
    val type: TransactionType
    var paymentMethodAlias: String
    var billingId: String?
    val pg: String
    val pgUniqueId: String
    var pgData: Map<String, *>?

    val goodsId: String?
    val goodsName: String?
    val subscriptionId: String?
    val usedCouponId: String?
    val amount: BigDecimal?

    var orderStatus: OrderStatus
    var paidAt: Instant?
    var cancelledAt: Instant?
    var message: String?
    var vbank: VbankInfo?
}