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
import io.mosaicboot.core.util.PagedResult
import io.mosaicboot.core.util.UUIDv7
import io.mosaicboot.payment.config.PaymentProperties
import io.mosaicboot.payment.controller.dto.StartSubscriptionRequest
import io.mosaicboot.payment.controller.dto.SubscriptionResponse
import io.mosaicboot.payment.db.entity.SubscriptionStatus
import io.mosaicboot.payment.db.repository.PaymentGoodsRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentTransactionRepositoryBase
import io.mosaicboot.payment.service.PaymentService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
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

        return ResponseEntity.ok(
            SubscriptionResponse(
                id = result.id,
                billingId = result.billingId,
                optionId = result.optionId,
                billingCycle = result.billingCycle,
                usedCouponIds = result.usedCouponIds,
                status = result.status,
                validFrom = result.validFrom.epochSecond,
                validTo = result.validTo.epochSecond,
                scheduledOptionId = result.scheduledOptionId
            )
        )
    }

    @Operation(summary = "find subscriptions")
    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    fun getSubscriptions(
        authentication: Authentication,
        @RequestParam("goods_id") goodsId: String?,
        @RequestParam("status") statuses: List<String>?,
        @RequestParam("page") pageNumber: Int?,
        @RequestParam("size") pageSize: Int?,
    ): ResponseEntity<PagedResult<SubscriptionResponse>> {
        authentication as MosaicAuthenticatedToken

        val result = paymentService.findSubscriptions(
            authentication,
            goodsId,
            statuses?.map { SubscriptionStatus.valueOf(it.uppercase()) },
            PageRequest.of(pageNumber ?: 0, pageSize ?: 10)
        )

        return ResponseEntity.ok(
            PagedResult(
                result.content.map {
                    SubscriptionResponse(
                        id = it.id,
                        billingId = it.billingId,
                        optionId = it.optionId,
                        billingCycle = it.billingCycle,
                        usedCouponIds = it.usedCouponIds,
                        status = it.status,
                        validFrom = it.validFrom.epochSecond,
                        validTo = it.validTo.epochSecond,
                        scheduledOptionId = it.scheduledOptionId
                    )
                },
                result.totalElements,
            )
        )
    }
}
