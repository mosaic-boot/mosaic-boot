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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.payment.db.dto.OrderStatus
import java.math.BigDecimal
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
open class Order(
    @field:JsonProperty("createdAt")
    val createdAt: Instant?,

    @field:JsonProperty("orderId")
    val orderId: String,

    @field:JsonProperty("goodsId")
    val goodsId: String,
    @field:JsonProperty("goodsName")
    val goodsName: String,

    @field:JsonProperty("amount")
    val amount: BigDecimal,

    @field:JsonProperty("status")
    val status: OrderStatus,

    @field:JsonProperty("paidAt")
    val paidAt: Instant?,
    @field:JsonProperty("cancelledAt")
    val cancelledAt: Instant?,
)