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
import io.mosaicboot.core.result.SimpleSuccess
import io.mosaicboot.core.result.UserErrorMessageException
import io.mosaicboot.core.util.UUIDv7
import io.mosaicboot.payment.controller.dto.AddCardTypeKrRequest
import io.mosaicboot.payment.db.dto.OrderStatus
import io.mosaicboot.payment.db.dto.PaymentBillingInput
import io.mosaicboot.payment.db.dto.PaymentLogInput
import io.mosaicboot.payment.db.dto.PaymentTransactionInput
import io.mosaicboot.payment.db.dto.TransactionType
import io.mosaicboot.payment.db.dto.VbankInfo
import io.mosaicboot.payment.db.entity.PaymentBilling
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
import io.mosaicboot.payment.nicepay.api.V1SubscribeExpireRequestBody
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

    override fun getName(): String = NicepayConst.PG

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

        val orderEntity = paymentTransactionRepository.findByPgAndPgUniqueId(
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
            val orderEntity = paymentTransactionRepository.findByPgAndPgUniqueId(
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
    ): Result<PaymentBilling> {
        return handleTransaction(
            authentication,
            TransactionType.BILLING_ADD_CARD
        ) { transaction, ediDate ->
            val response = nicepayApiClient.postSubscribeRegist(
                V1SubscribeRegistRequestBody(
                    orderId = transaction.pgUniqueId,
                    buyerName = request.name?.takeIf { it.isNotBlank() },
                    buyerEmail = request.email?.takeIf { it.isNotBlank() },
                    buyerTel = request.tel?.takeIf { it.isNotBlank() }?.replace("-", ""),
                    ediDate = ediDate,
                    encMode = "A2",
                    encData = V1SubscribeRegistRequestBody.generateEncData(
                        secretKey = nicepayProperties.clientSecret,
                        cardNo = request.cardNo.replace("-", ""),
                        expYear = request.expYear.replace(Regex("[-/]"), ""),
                        expMonth = request.expMonth,
                        idNo = request.idNo.replace("-", ""),
                        cardPw = request.cardPw,
                        encMode = "A2",
                    ),
                ).withSign(nicepayProperties.clientSecret)
            )
            val bid = response.bid!!
            response.bid = "[REDACTED]"
            transaction.pgData = objectMapper.convertValue(
                response,
                object: TypeReference<Map<String, *>>() {}
            )
            transaction.message = response.resultMsg
            if (response.resultCode == "0000") {
                transaction.orderStatus = OrderStatus.COMPLETED

                val paymentBilling = paymentBillingRepository.save(PaymentBillingInput(
                    createdAt = Instant.now(),
                    userId = authentication.userId,
                    pg = NicepayConst.PG,
                    addCardTxId = transaction.id,
                    alias = request.alias ?: "",
                    description = "${response.cardName} (${request.cardNo.substring(request.cardNo.length-4)})",
                    secret = serverSideCrypto.encrypt(
                        NicepayBillingData(
                            bid = bid,
                            authDate = response.authDate,
                            cardCode = response.cardCode,
                            cardName = response.cardName,
                        )
                    ),
                ))

                Result.success(paymentBilling)
            } else {
                transaction.orderStatus = OrderStatus.FAILURE

                Result.failure(UserErrorMessageException("${response.resultCode}: ${response.resultMsg}"))
            }
        }
    }

    override fun billingDelete(
        authentication: MosaicAuthenticatedToken,
        billing: PaymentBilling
    ): Result<SimpleSuccess> {
        return handleTransaction(
            authentication,
            TransactionType.BILLING_ADD_CARD
        ) { transaction, ediDate ->
            val data = serverSideCrypto.decrypt(billing.secret!!, NicepayBillingData::class.java)
            val response = nicepayApiClient.postSubscribeExpire(
                data.bid,
                V1SubscribeExpireRequestBody(
                    orderId = transaction.pgUniqueId,
                    ediDate = ediDate,
                ).withSign(data.bid, nicepayProperties.clientSecret)
            )
            if (response.resultCode == "0000") {
                transaction.orderStatus = OrderStatus.COMPLETED
                Result.success(SimpleSuccess(response.resultMsg))
            } else {
                transaction.orderStatus = OrderStatus.FAILURE
                Result.failure(UserErrorMessageException("${response.resultCode}: ${response.resultMsg}"))
            }
        }
    }

    fun <T> handleTransaction(
        authentication: MosaicAuthenticatedToken,
        type: TransactionType,
        fn: (tx: PaymentTransaction, ediDate: String) -> Result<T>,
    ): Result<T> {
        val now = Instant.now()
        val txId = UUIDv7.generate().toString()
        val transaction = paymentTransactionRepository.save(PaymentTransactionInput(
            id = txId,
            userId = authentication.userId,
            createdAt = now,
            type = type,
            pg = NicepayConst.PG,
            pgUniqueId = txId,
            orderStatus = OrderStatus.PROCESSING,
        ))
        val ediDate = DateTimeFormatter.ISO_INSTANT.format(now)

        val result = try {
            fn(transaction, ediDate)
        } catch (e: Throwable) {
            transaction.orderStatus = OrderStatus.ERROR
            transaction.message = "server error"
            log.error("server error", e)

            Result.failure(e)
        }

        paymentTransactionRepository.saveEntity(transaction)

        return result
    }
}