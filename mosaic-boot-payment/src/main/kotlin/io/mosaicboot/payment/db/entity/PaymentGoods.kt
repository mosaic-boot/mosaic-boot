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

import io.mosaicboot.data.entity.UpdatableEntity
import java.math.BigDecimal

interface PaymentGoods : UpdatableEntity<String> {
    val type: GoodsType
    var name: String
    var description: String?
    var basePrice: BigDecimal
    val options: List<GoodsOption>?
    var data: Map<String, Any?>?
}

enum class GoodsType {
    REGULAR,
    SUBSCRIPTION
}

interface GoodsOption {
    val id: String
    var name: String
    var additionalPrice: Long
    var data: Map<String, Any?>?
}
