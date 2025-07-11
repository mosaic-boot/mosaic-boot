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

package io.mosaicboot.payment.db.repository

import io.mosaicboot.payment.db.dto.PaymentBillingInput
import io.mosaicboot.payment.db.entity.PaymentBilling

interface PaymentBillingMosaicRepository<T : PaymentBilling> {
    fun save(input: PaymentBillingInput): T
    fun findAllByUserId(userId: String): List<T>
    fun findPrimaryByUserId(userId: String): T?
    fun updatePrimary(userId: String, newPrimaryBillingId: String): T?
}
