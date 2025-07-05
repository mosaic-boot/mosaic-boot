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

import com.fasterxml.jackson.annotation.JsonProperty

open class PaymentCouponDiscount(
    @JsonProperty("period")
    @field:JsonProperty("period")
    val period: Int, // 0 for permanent, >=1 for number of billing cycles (e.g., 3 months)
    @JsonProperty("value")
    @field:JsonProperty("value")
    val value: Long, // Percentage (0-100) or fixed amount based on type
)