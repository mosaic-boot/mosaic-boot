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
import io.mosaicboot.payment.db.dto.TransactionType
import java.math.BigDecimal
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
open class Transaction(
    @field:JsonProperty("id")
    val id: String,

    /**
     * unix timestamp milliseconds
     */
    @field:JsonProperty("createdAt")
    val createdAt: Long?,

    @field:JsonProperty("type")
    val type: TransactionType,

    @field:JsonProperty("paymentMethodAlias")
    val paymentMethodAlias: String,
    @field:JsonProperty("billingId")
    val billingId: String?,

    @field:JsonProperty("goodsId")
    val goodsId: String?,
    @field:JsonProperty("goodsName")
    val goodsName: String?,
    @field:JsonProperty("subscriptionId")
    val subscriptionId: String?,
    @field:JsonProperty("amount")
    val amount: BigDecimal?,

    @field:JsonProperty("orderStatus")
    val orderStatus: OrderStatus,

    /**
     * unix timestamp milliseconds
     */
    @field:JsonProperty("paidAt")
    val paidAt: Long?,

    /**
     * unix timestamp milliseconds
     */
    @field:JsonProperty("cancelledAt")
    val cancelledAt: Long?,
)