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

import io.mosaicboot.data.repository.BaseRepository
import io.mosaicboot.payment.db.dto.PaymentLogInput
import io.mosaicboot.payment.db.entity.PaymentLog

interface PaymentLogRepositoryBase<T : PaymentLog<ID>, ID> :
    BaseRepository<PaymentLog<ID>, T, ID>
{
    fun save(input: PaymentLogInput): T
}