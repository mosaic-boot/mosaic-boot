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
import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.core.http.dto.PagedResponse
import io.mosaicboot.core.util.PagedResult
import io.mosaicboot.payment.config.PaymentProperties
import io.mosaicboot.payment.controller.dto.Order
import io.mosaicboot.payment.controller.dto.OrderDetail
import io.mosaicboot.payment.db.repository.PaymentOrderRepositoryBase
import io.mosaicboot.payment.goods.GoodsRepository
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.query.Param
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.client.HttpClientErrorException
import java.nio.charset.StandardCharsets

@MosaicController
class MosaicPaymentController(
    private val paymentProperties: PaymentProperties,
    private val goodsRepository: GoodsRepository,
    private val paymentOrderRepository: PaymentOrderRepositoryBase<*>,
) : BaseMosaicController {
    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        return paymentProperties.api.path
    }

    @GetMapping("/order/")
    @PreAuthorize("isAuthenticated()")
    @PageableAsQueryParam
    fun getOrderList(
        authentication: Authentication,
        @Param("page") page: Int,
        @Param("size") size: Int,
    ): PagedResponse<Order> {
        authentication as MosaicAuthenticatedToken

        val realPage = page.coerceAtLeast(0)
        val realSize = size.coerceAtMost(100)

        val result = paymentOrderRepository.getOrderListByUserIdWithPaged(
            authentication.userId,
            PageRequest.of(realPage, realSize)
        )
        return PagedResponse(
            items = result.content.map {
                Order(
                    createdAt = it.createdAt,
                    orderId = it.orderId,
                    goodsId = it.goodsId,
                    goodsName = it.goodsName,
                    amount = it.amount,
                    status = it.status,
                    paidAt = it.paidAt,
                    cancelledAt = it.cancelledAt,
                )
            },
            total = result.totalElements,
            page = realPage,
            size = realSize,
        )
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    fun getOrderDetail(
        @PathVariable("orderId") orderId: String,
        authentication: Authentication,
    ): OrderDetail {
        authentication as MosaicAuthenticatedToken

        val result = paymentOrderRepository.findByUserIdAndOrderId(
            authentication.userId,
            orderId,
        ) ?: throw HttpClientErrorException.create(
            HttpStatus.NOT_FOUND,
            "invalid order id",
            HttpHeaders(),
            "".toByteArray(),
            StandardCharsets.UTF_8
        )

        return OrderDetail(
            createdAt = result.createdAt,
            orderId = result.orderId,
            goodsId = result.goodsId,
            goodsName = result.goodsName,
            amount = result.amount,
            status = result.status,
            paidAt = result.paidAt,
            cancelledAt = result.cancelledAt,
            message = result.message,
            vbank = result.vbank,
        )
    }
}