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

package io.mosaicboot.payment.nicepay.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.payment.db.repository.PaymentLogRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentOrderRepositoryBase
import io.mosaicboot.payment.nicepay.api.NicepayApiClient
import io.mosaicboot.payment.nicepay.controller.NicepayController
import io.mosaicboot.payment.nicepay.service.NicepayService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean

@EnableFeignClients(
    clients = [
        NicepayApiClient::class,
    ]
)
@EnableConfigurationProperties(NicepayProperties::class)
@ConditionalOnProperty("mosaic.pg.nicepay", havingValue = "true", matchIfMissing = true)
class NicepayConfig {
    @Bean
    fun nicepayService(
        nicepayApiClient: NicepayApiClient,
        paymentLogRepository: PaymentLogRepositoryBase<*, *>,
        paymentOrderRepository: PaymentOrderRepositoryBase<*>,
        objectMapper: ObjectMapper,
    ): NicepayService {
        return NicepayService(
            nicepayApiClient = nicepayApiClient,
            paymentLogRepository = paymentLogRepository,
            paymentOrderRepository = paymentOrderRepository,
            objectMapper = objectMapper,
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "mosaic.pg.nicepay.api", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun nicepayWebhookController(
        nicepayProperties: NicepayProperties,
        objectMapper: ObjectMapper,
        nicepayService: NicepayService,
    ): NicepayController {
        return NicepayController(
            nicepayProperties = nicepayProperties,
            objectMapper = objectMapper,
            nicepayService = nicepayService,
        )
    }
}