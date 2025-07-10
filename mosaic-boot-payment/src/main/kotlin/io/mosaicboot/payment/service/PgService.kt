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

package io.mosaicboot.payment.service

import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.result.SimpleSuccess
import io.mosaicboot.payment.controller.dto.AddCardTypeKrRequest
import io.mosaicboot.payment.db.entity.PaymentBilling
import io.mosaicboot.payment.db.entity.PaymentTransaction
import io.mosaicboot.payment.dto.SubmitBillingPayment

interface PgService {
    fun getName(): String

    fun addBillingCard(
        userId: String,
        traceId: String,
        request: AddCardTypeKrRequest,
    ): Result<PaymentBilling> {
        throw NotImplementedError("Not implemented this PG")
    }

    fun removeBillingMethod(
        userId: String,
        traceId: String,
        billing: PaymentBilling,
    ): Result<SimpleSuccess> {
        throw NotImplementedError("Not implemented this PG")
    }

    fun processBillingPayment(
        userId: String,
        traceId: String,
        billing: PaymentBilling,
        request: SubmitBillingPayment,
    ): Result<PaymentTransaction> {
        throw NotImplementedError("Not implemented this PG")
    }

    fun getTransactionRecipeUrl(
        transaction: PaymentTransaction,
    ): String
}