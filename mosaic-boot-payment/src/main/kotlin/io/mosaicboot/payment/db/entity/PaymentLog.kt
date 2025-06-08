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

import io.mosaicboot.core.entity.BaseEntity

/**
 * ID : UUIDv7
 */
interface PaymentLog<ID> : BaseEntity<ID> {
    /**
     * pg name
     */
    val pg: String

    /**
     * "specific.webhook" - webhook raw data
     * "specific.{sub_name}"
     */
    val type: String
    val orderId: String
    val data: Map<String, *>
}