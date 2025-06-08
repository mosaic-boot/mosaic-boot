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

package io.mosaicboot.payment.nicepay.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.payment.db.dto.OrderStatus
import io.mosaicboot.payment.db.dto.PaymentLogInput
import io.mosaicboot.payment.db.dto.VbankInfo
import io.mosaicboot.payment.db.entity.PaymentOrder
import io.mosaicboot.payment.db.repository.PaymentLogRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentOrderRepositoryBase
import io.mosaicboot.payment.nicepay.api.AuthResponse
import io.mosaicboot.payment.nicepay.api.NicepayApiClient
import io.mosaicboot.payment.nicepay.api.PaymentNotificationResponse
import io.mosaicboot.payment.nicepay.api.V1PaymentNetCancelRequestBody
import io.mosaicboot.payment.nicepay.api.V1PaymentRequestBody
import io.mosaicboot.payment.nicepay.api.V1PaymentResultBase
import io.mosaicboot.payment.nicepay.dto.NicepayConst
import io.mosaicboot.payment.nicepay.dto.OrderResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class NicepayService(
    private val nicepayApiClient: NicepayApiClient,
    private val paymentLogRepository: PaymentLogRepositoryBase<*, *>,
    private val paymentOrderRepository: PaymentOrderRepositoryBase<*>,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private val log = LoggerFactory.getLogger(NicepayService::class.java)
    }

    fun onAuth(body: AuthResponse): OrderResult {
        paymentLogRepository.save(
            PaymentLogInput(
                createdAt = Instant.now(),
                pg = NicepayConst.PG,
                type = "specific.auth",
                orderId = body.orderId,
                data = objectMapper.convertValue(
                    body,
                    object: TypeReference<Map<String, Any?>>() {},
                )
            )
        )

        val orderEntity = paymentOrderRepository.findByPgAndOrderId(
            NicepayConst.PG,
            body.orderId,
        ) ?: let {
            log.error("order not found (orderId={}, data={})", body.orderId, body)
            return OrderResult.Failure("order-not-found")
        }

        orderEntity.message = body.authResultMsg

        if (body.authResultCode != "0000") {
            orderEntity.status = OrderStatus.FAILURE

            paymentOrderRepository.saveEntity(orderEntity)

            return OrderResult.Failure(body.authResultMsg ?: body.authResultCode ?: "error")
        }

        if (body.amount!!.toBigDecimal() != orderEntity.amount) {
            return OrderResult.Failure("wrong-amount")
        }

        orderEntity.status = OrderStatus.PROCESSING

        try {
            val paymentResult = nicepayApiClient.postV1Payment(
                tid = body.tid!!,
                requestBody = V1PaymentRequestBody(
                    amount = body.amount,
                )
            )

            handleResult(orderEntity, paymentResult)

            paymentOrderRepository.saveEntity(orderEntity)

            return OrderResult.Success()
        } catch (e: Exception) {
            nicepayApiClient.postV1PaymentNetCancel(
                requestBody = V1PaymentNetCancelRequestBody(
                    orderID = body.orderId,
                )
            )
            log.error("server error", e)
            return OrderResult.Failure("error")
        }
    }

    @Transactional
    fun onWebhook(body: PaymentNotificationResponse): Boolean {
        paymentLogRepository.save(
            PaymentLogInput(
                createdAt = Instant.now(),
                pg = NicepayConst.PG,
                type = "specific.webhook",
                orderId = body.orderId,
                data = objectMapper.convertValue(
                    body,
                    object: TypeReference<Map<String, Any?>>() {},
                )
            )
        )

        try {
            val orderEntity = paymentOrderRepository.findByPgAndOrderId(
                NicepayConst.PG,
                body.orderId,
            ) ?: let {
                log.error("order not found (orderId={}, data={})", body.orderId, body)
                return true
            }

            handleResult(orderEntity, body)

            paymentOrderRepository.saveEntity(orderEntity)

            return true
        } catch (e: Exception) {
            nicepayApiClient.postV1PaymentNetCancel(
                requestBody = V1PaymentNetCancelRequestBody(
                    orderID = body.orderId,
                )
            )
            log.error("server error", e)
            return false
        }
    }

    fun handleResult(
        orderEntity: PaymentOrder,
        paymentResult: V1PaymentResultBase,
    ) {
        val paymentSuccess = (paymentResult.resultCode == "0000")

        orderEntity.message = paymentResult.resultMsg

        orderEntity.status = if (paymentSuccess) {
            when (paymentResult.status) {
                "paid" -> OrderStatus.PAID
                "ready" -> OrderStatus.PROCESSING
                else -> OrderStatus.FAILURE
            }
        } else {
            OrderStatus.FAILURE
        }

        paymentResult.vbank?.let { vbank ->
            orderEntity.vbank = VbankInfo(
                name = vbank.vbankName,
                number = vbank.vbankNumber,
                expire = Instant.parse(vbank.vbankExpDate),
                holder = vbank.vbankHolder,
            )
        }
    }
}