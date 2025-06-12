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

package io.mosaicboot.payment.nicepay.api

import io.mosaicboot.payment.nicepay.config.NicepayFeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "nicepay",
    url = "https://api.nicepay.co.kr",
    configuration = [NicepayFeignConfig::class],
)
interface NicepayApiClient {
    @PostMapping(
        path = ["/v1/payments/{tid}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun postV1Payment(
        @PathVariable("tid") tid: String,
        @RequestBody requestBody: V1PaymentRequestBody,
    ): V1PaymentResponseBody

    @PostMapping(
        path = ["/v1/subscribe/regist"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun postSubscribeRegist(
        @RequestBody requestBody: V1SubscribeRegistRequestBody,
    ): V1SubscribeRegistResponseBody

    @PostMapping(
        path = ["/v1/subscribe/{bid}/payments"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun postSubscribePayment(
        @PathVariable("bid") bid: String,
        @RequestBody requestBody: V1SubscribePaymentRequestBody,
    ): V1PaymentResponseBody

    @PostMapping(
        path = ["/v1/subscribe/{bid}/expire"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun postSubscribeExpire(
        @PathVariable("bid") bid: String,
        @RequestBody requestBody: V1SubscribeExpireRequestBody,
    ): V1SubscribeExpireResponseBody

    @PostMapping(
        path = ["/v1/payments/netcancel"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun postV1PaymentNetCancel(
        @RequestBody requestBody: V1PaymentNetCancelRequestBody,
    ): Map<String, Any?>

    @PostMapping(
        path = ["/v1/payments/{tid}/cancel"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun postV1PaymentCancel(
        @PathVariable("tid") tid: String,
        @RequestBody requestBody: V1PaymentCancelBody,
    ): V1PaymentResponseBody
}