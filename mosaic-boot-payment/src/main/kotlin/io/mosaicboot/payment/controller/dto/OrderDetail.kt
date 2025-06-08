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

package io.mosaicboot.payment.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.payment.db.dto.OrderStatus
import io.mosaicboot.payment.db.dto.VbankInfo
import java.math.BigDecimal
import java.time.Instant

class OrderDetail(
    createdAt: Instant?,
    orderId: String,
    goodsId: String,
    goodsName: String,
    amount: BigDecimal,
    status: OrderStatus,
    paidAt: Instant?,
    cancelledAt: Instant?,
    @field:JsonProperty("message")
    val message: String?,
    @field:JsonProperty("vbank")
    val vbank: VbankInfo?,
) : Order(
    createdAt = createdAt,
    orderId = orderId,
    goodsId = goodsId,
    goodsName = goodsName,
    amount = amount,
    status = status,
    paidAt = paidAt,
    cancelledAt = cancelledAt,
)