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

package io.mosaicboot.payment.controller

import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.error.UserErrorMessageException
import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.core.http.dto.PagedResponse
import io.mosaicboot.core.http.dto.SimpleErrorResponse
import io.mosaicboot.payment.config.PaymentProperties
import io.mosaicboot.payment.controller.dto.AddCardResponse
import io.mosaicboot.payment.controller.dto.Transaction
import io.mosaicboot.payment.controller.dto.TransactionDetail
import io.mosaicboot.payment.controller.dto.AddCardTypeKrRequest
import io.mosaicboot.payment.db.repository.PaymentTransactionRepositoryBase
import io.mosaicboot.payment.goods.GoodsRepository
import io.mosaicboot.payment.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.query.Param
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.client.HttpClientErrorException
import java.nio.charset.StandardCharsets

@MosaicController
class MosaicPaymentController(
    private val paymentProperties: PaymentProperties,
    private val goodsRepository: GoodsRepository,
    private val paymentTransactionRepository: PaymentTransactionRepositoryBase<*>,
    private val paymentService: PaymentService,
) : BaseMosaicController {
    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        return paymentProperties.api.path
    }

    @Operation(summary = "Get transaction list")
    @GetMapping("/transactions/")
    @PreAuthorize("isAuthenticated()")
    @PageableAsQueryParam
    fun getTransactionList(
        authentication: Authentication,
        @Param("page") page: Int,
        @Param("size") size: Int,
    ): PagedResponse<Transaction> {
        authentication as MosaicAuthenticatedToken

        val realPage = page.coerceAtLeast(0)
        val realSize = size.coerceAtMost(100)

        val result = paymentTransactionRepository.getOrderListByUserIdWithPaged(
            authentication.userId,
            PageRequest.of(realPage, realSize)
        )
        return PagedResponse(
            items = result.content.map {
                Transaction(
                    id = it.id,
                    createdAt = it.createdAt,
                    type = it.type,
                    goodsId = it.goodsId,
                    goodsName = it.goodsName,
                    subscriptionId = it.subscriptionId,
                    amount = it.amount,
                    orderStatus = it.orderStatus,
                    paidAt = it.paidAt,
                    cancelledAt = it.cancelledAt,
                )
            },
            total = result.totalElements,
            page = realPage,
            size = realSize,
        )
    }

    @Operation(summary = "Get a transaction details")
    @GetMapping("/transactions/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    fun getTransactionDetail(
        @PathVariable("transactionId") transactionId: String,
        authentication: Authentication,
    ): TransactionDetail {
        authentication as MosaicAuthenticatedToken

        val result = paymentTransactionRepository.findByUserIdAndId(
            authentication.userId,
            transactionId,
        ) ?: throw HttpClientErrorException.create(
            HttpStatus.NOT_FOUND,
            "invalid order id",
            HttpHeaders(),
            "".toByteArray(),
            StandardCharsets.UTF_8
        )

        return TransactionDetail(
            id = result.id,
            createdAt = result.createdAt,
            type = result.type,
            goodsId = result.goodsId,
            goodsName = result.goodsName,
            subscriptionId = result.subscriptionId,
            amount = result.amount,
            orderStatus = result.orderStatus,
            paidAt = result.paidAt,
            cancelledAt = result.cancelledAt,
            vbank = result.vbank,
        )
    }

    @Operation(summary = "Add korea-style credit card")
    @PostMapping("/cards/kr")
    @PreAuthorize("isAuthenticated()")
    fun cardAddKr(
        authentication: Authentication,
        @RequestBody body: AddCardTypeKrRequest,
    ): ResponseEntity<Any> {
        authentication as MosaicAuthenticatedToken
        val result = paymentService.billingAddCard(
            authentication,
            body,
        )
        if (result.isSuccess) {
            val billing = result.getOrThrow()
            return ResponseEntity.ok(AddCardResponse(
                billingId = billing.id,
                alias = billing.alias,
            ))
        } else {
            val exception = result.exceptionOrNull()!!
            if (exception is UserErrorMessageException) {
                return ResponseEntity.status(
                    HttpStatus.BAD_REQUEST
                ).body(SimpleErrorResponse(exception))
            } else {
                throw exception
            }
        }
    }
}