package io.mosaicboot.payment.nicepay.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.payment.nicepay.api.AuthResponse
import io.mosaicboot.payment.nicepay.api.PaymentNotificationResponse
import io.mosaicboot.payment.nicepay.config.NicepayProperties
import io.mosaicboot.payment.nicepay.service.NicepayService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.util.UriComponentsBuilder
import java.nio.charset.StandardCharsets

@MosaicController
class NicepayController(
    private val nicepayProperties: NicepayProperties,
    private val objectMapper: ObjectMapper,
    private val nicepayService: NicepayService,
) : BaseMosaicController {
    companion object {
        private val log = LoggerFactory.getLogger(NicepayController::class.java)
    }

    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        return nicepayProperties.api.path
    }

    @PostMapping(
        path = ["/auth"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
    )
    fun postComplete(
        httpServletResponse: HttpServletResponse,
        @RequestParam("finish_to") finishTo: String,
        @RequestParam formData: MultiValueMap<String, String>,
    ) {
        val requestBody =
            objectMapper.convertValue(
                formData.map { it.key to it.value.first() }.toMap(),
                AuthResponse::class.java
            )
        if (
            requestBody.clientId != nicepayProperties.clientId ||
            !requestBody.verifySignature(nicepayProperties.secretKey)
        ) {
            throw HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "bad signature or data",
                HttpHeaders(),
                "".toByteArray(),
                StandardCharsets.UTF_8
            )
        }

        val finishUrlBuilder = UriComponentsBuilder.fromUriString(finishTo)
            .replaceQueryParam("order_id", requestBody.orderId)

        val order = nicepayService.onAuth(requestBody)

//        finishUrlBuilder.replaceQueryParam("state", orderEntity.state)
        httpServletResponse.sendRedirect(finishUrlBuilder.build().toUriString())
    }

    @PostMapping(
        path = ["/webhook"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = ["text/html;charset=utf-8"]
    )
    fun postWebhook(
        @RequestBody(required = false) body: PaymentNotificationResponse?,
    ): ResponseEntity<String> {
        body ?: return ResponseEntity.ok("OK")
        val isTest = body.mallReserved?.contains("TEST") == true

        if (!isTest && !body.verifySignature(nicepayProperties.secretKey)) {
            throw HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "bad signature or data",
                HttpHeaders(),
                "".toByteArray(),
                StandardCharsets.UTF_8
            )
        }

        log.debug("WEBHOOK: ${objectMapper.writeValueAsString(body)}")

        val result = if (!isTest) {
            nicepayService.onWebhook(body)
        } else false

        return ResponseEntity.ok(if (result) { "OK" } else { "ERROR" })
    }
}