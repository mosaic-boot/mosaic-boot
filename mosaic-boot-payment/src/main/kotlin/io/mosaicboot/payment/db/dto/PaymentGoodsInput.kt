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

import io.mosaicboot.payment.db.entity.GoodsOption
import io.mosaicboot.payment.db.entity.GoodsType

data class PaymentGoodsInput(
    val id: String? = null,
    val name: String,
    val type: GoodsType,
    val description: String? = null,
    val basePrice: Long,
    val options: List<GoodsOptionInput> = emptyList(),
)

data class GoodsOptionInput(
    override val id: String,
    override var name: String,
    override var additionalPrice: Long,
    override var data: Map<String, Any?>? = null,
) : GoodsOption