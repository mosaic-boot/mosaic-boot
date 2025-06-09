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
import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.encryption.ServerSideCrypto
import io.mosaicboot.core.util.UUIDv7
import io.mosaicboot.payment.controller.dto.AddCardTypeKrRequest
import io.mosaicboot.payment.db.dto.OrderStatus
import io.mosaicboot.payment.db.dto.PaymentBillingInput
import io.mosaicboot.payment.db.dto.PaymentLogInput
import io.mosaicboot.payment.db.dto.PaymentTransactionInput
import io.mosaicboot.payment.db.dto.TransactionType
import io.mosaicboot.payment.db.dto.VbankInfo
import io.mosaicboot.payment.db.entity.PaymentTransaction
import io.mosaicboot.payment.db.repository.PaymentBillingRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentLogRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentTransactionRepositoryBase
import io.mosaicboot.payment.nicepay.api.AuthResponse
import io.mosaicboot.payment.nicepay.api.NicepayApiClient
import io.mosaicboot.payment.nicepay.api.PaymentNotificationResponse
import io.mosaicboot.payment.nicepay.api.V1PaymentNetCancelRequestBody
import io.mosaicboot.payment.nicepay.api.V1PaymentRequestBody
import io.mosaicboot.payment.nicepay.api.V1PaymentResultBase
import io.mosaicboot.payment.nicepay.api.V1SubscribeRegistRequestBody
import io.mosaicboot.payment.nicepay.config.NicepayProperties
import io.mosaicboot.payment.nicepay.dto.NicepayBillingData
import io.mosaicboot.payment.nicepay.dto.NicepayConst
import io.mosaicboot.payment.nicepay.dto.OrderResult
import io.mosaicboot.payment.service.PgService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.format.DateTimeFormatter

@Service
class NicepayService(
    private val nicepayProperties: NicepayProperties,
    private val nicepayApiClient: NicepayApiClient,
    private val paymentLogRepository: PaymentLogRepositoryBase<*, *>,
    private val paymentTransactionRepository: PaymentTransactionRepositoryBase<*>,
    private val paymentBillingRepository: PaymentBillingRepositoryBase<*>,
    private val objectMapper: ObjectMapper,
    private val serverSideCrypto: ServerSideCrypto,
) : PgService {
    companion object {
        private val log = LoggerFactory.getLogger(NicepayService::class.java)
    }

    @Transactional
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

        val orderEntity = paymentTransactionRepository.findByPgAndOrderId(
            NicepayConst.PG,
            body.orderId,
        ) ?: let {
            log.error("order not found (orderId={}, data={})", body.orderId, body)
            return OrderResult.Failure("order-not-found")
        }

        orderEntity.message = body.authResultMsg

        if (body.authResultCode != "0000") {
            orderEntity.orderStatus = OrderStatus.FAILURE

            paymentTransactionRepository.saveEntity(orderEntity)

            return OrderResult.Failure(body.authResultMsg ?: body.authResultCode ?: "error")
        }

        if (body.amount!!.toBigDecimal() != orderEntity.amount) {
            return OrderResult.Failure("wrong-amount")
        }

        orderEntity.orderStatus = OrderStatus.PROCESSING

        try {
            val paymentResult = nicepayApiClient.postV1Payment(
                tid = body.tid!!,
                requestBody = V1PaymentRequestBody(
                    amount = body.amount,
                )
            )

            handleResult(orderEntity, paymentResult)

            paymentTransactionRepository.saveEntity(orderEntity)

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
            val orderEntity = paymentTransactionRepository.findByPgAndOrderId(
                NicepayConst.PG,
                body.orderId,
            ) ?: let {
                log.error("order not found (orderId={}, data={})", body.orderId, body)
                return true
            }

            handleResult(orderEntity, body)

            paymentTransactionRepository.saveEntity(orderEntity)

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

    @Transactional
    fun handleResult(
        orderEntity: PaymentTransaction,
        paymentResult: V1PaymentResultBase,
    ) {
        val paymentSuccess = (paymentResult.resultCode == "0000")

        orderEntity.message = paymentResult.resultMsg

        orderEntity.orderStatus = if (paymentSuccess) {
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

    override fun billingAddCard(
        authentication: MosaicAuthenticatedToken,
        request: AddCardTypeKrRequest,
    ) {
        val now = Instant.now()
        val txId = UUIDv7.generate().toString()
        val transaction = paymentTransactionRepository.save(PaymentTransactionInput(
            id = txId,
            userId = authentication.userId,
            createdAt = now,
            type = TransactionType.BILLING_ADD_CARD,
            pg = NicepayConst.PG,
            pgUniqueId = txId,
            orderStatus = OrderStatus.PROCESSING,
        ))
        val ediDate = DateTimeFormatter.ISO_INSTANT.format(now)
        try {
            val response = nicepayApiClient.postSubscribeRegist(
                V1SubscribeRegistRequestBody(
                    orderId = transaction.pgUniqueId,
                    buyerName = request.name?.takeIf { it.isNotBlank() },
                    buyerEmail = request.email?.takeIf { it.isNotBlank() },
                    buyerTel = request.tel?.takeIf { it.isNotBlank() }?.replace("-", ""),
                    ediDate = ediDate,
                    encMode = "A2",
                    encData = V1SubscribeRegistRequestBody.generateEncData(
                        secretKey = nicepayProperties.secretKey,
                        cardNo = request.cardNo,
                        expYear = request.expYear,
                        expMonth = request.expMonth,
                        idNo = request.idNo,
                        cardPw = request.cardPw,
                        encMode = "A2",
                    ),
                    signData = V1SubscribeRegistRequestBody.makeSignData(txId, ediDate, nicepayProperties.secretKey),
                )
            )
            transaction.pgData = objectMapper.convertValue(
                response,
                object: TypeReference<Map<String, *>>() {}
            )
            transaction.message = response.resultMsg
            transaction.orderStatus = if (response.resultCode == "0000") {
                OrderStatus.COMPLETED
            } else {
                OrderStatus.FAILURE
            }

            paymentBillingRepository.save(PaymentBillingInput(
                createdAt = Instant.now(),
                userId = authentication.userId,
                pg = NicepayConst.PG,
                addCardTxId = transaction.id,
                secret = serverSideCrypto.encrypt(
                    NicepayBillingData(
                        bid = response.bid!!,
                        authDate = response.authDate,
                        cardCode = response.cardCode,
                        cardName = response.cardName,
                    )
                ),
            ))
        } catch (e: Throwable) {
            transaction.orderStatus = OrderStatus.ERROR
            transaction.message = "server error"
            log.error("server error", e)
        }

        paymentTransactionRepository.saveEntity(transaction)
    }
}