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

import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.result.UserErrorMessageException
import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.core.http.dto.PagedResponse
import io.mosaicboot.core.http.dto.SimpleErrorResponse
import io.mosaicboot.core.user.controller.dto.MyTenant
import io.mosaicboot.core.util.PagedResult
import io.mosaicboot.core.util.UUIDv7
import io.mosaicboot.core.util.UnreachableException
import io.mosaicboot.payment.config.PaymentProperties
import io.mosaicboot.payment.controller.dto.BillingMethod
import io.mosaicboot.payment.controller.dto.Transaction
import io.mosaicboot.payment.controller.dto.TransactionDetail
import io.mosaicboot.payment.controller.dto.AddCardTypeKrRequest
import io.mosaicboot.payment.controller.dto.CardInfo
import io.mosaicboot.payment.controller.dto.StartSubscriptionRequest
import io.mosaicboot.payment.controller.dto.SubscriptionResponse
import io.mosaicboot.payment.db.repository.PaymentGoodsRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentTransactionRepositoryBase
import io.mosaicboot.payment.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import org.bouncycastle.util.Strings
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@MosaicController
class MosaicPaymentSubscriptionController(
    private val paymentProperties: PaymentProperties,
    private val goodsRepository: PaymentGoodsRepositoryBase<*>,
    private val paymentTransactionRepository: PaymentTransactionRepositoryBase<*>,
    private val paymentService: PaymentService,
) : BaseMosaicController {
    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        return paymentProperties.api.path.trimEnd('/') + "/subscriptions"
    }

    @Operation(summary = "start subscription")
    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    fun startSubscription(
        authentication: Authentication,
        @RequestBody body: StartSubscriptionRequest,
    ): ResponseEntity<Any> {
        authentication as MosaicAuthenticatedToken

        val result = paymentService.startSubscription(
            authentication,
            UUIDv7.generate().toString(),
            body.billingId,
            body.goodsId,
            body.optionId,
            body.couponId,
            body.firstAmount,
            30,
        )

        return ResponseEntity.ok().build()
    }

    @Operation(summary = "find current subscriptions")
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentSubscription(
        authentication: Authentication,
        @RequestParam("goods_id") goodsId: String,
    ): ResponseEntity<SubscriptionResponse> {
        authentication as MosaicAuthenticatedToken

        val result = paymentService.getCurrentSubscription(
            authentication,
            goodsId,
        ) ?: return ResponseEntity.notFound().build()

        val now = Instant.now()
        return ResponseEntity.ok(
            SubscriptionResponse(
                id = result.id,
                billingId = result.billingId,
                optionId = result.optionId,
                billingCycle = result.billingCycle,
                usedCouponIds = result.usedCouponIds,
                active = result.active && result.validTo.isAfter(now),
                cancelledAt = result.cancelledAt?.epochSecond,
                validFrom = result.validFrom.epochSecond,
                validTo = result.validTo.epochSecond,
            )
        )
    }

    @Operation(summary = "find subscriptions")
    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    fun getSubscriptions(
        authentication: Authentication,
        @RequestParam("goods_id") goodsId: String?,
        @RequestParam("active") active: Boolean?,
        @RequestParam("page") pageNumber: Int?,
        @RequestParam("size") pageSize: Int?,
    ): ResponseEntity<PagedResult<SubscriptionResponse>> {
        authentication as MosaicAuthenticatedToken

        val result = paymentService.findSubscriptions(
            authentication,
            goodsId,
            active,
            PageRequest.of(pageNumber ?: 0, pageSize ?: 10)
        )

        val now = Instant.now()
        return ResponseEntity.ok(
            PagedResult(
                result.content.map {
                    SubscriptionResponse(
                        id = it.id,
                        billingId = it.billingId,
                        optionId = it.optionId,
                        billingCycle = it.billingCycle,
                        usedCouponIds = it.usedCouponIds,
                        active = it.active && it.validTo.isAfter(now),
                        cancelledAt = it.cancelledAt?.epochSecond,
                        validFrom = it.validFrom.epochSecond,
                        validTo = it.validTo.epochSecond,
                    )
                },
                result.totalElements,
            )
        )
    }
}