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

package io.mosaicboot.payment.config

import io.mosaicboot.payment.controller.MosaicPaymentController
import io.mosaicboot.payment.db.repository.PaymentOrderRepositoryBase
import io.mosaicboot.payment.goods.GoodsRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@EnableConfigurationProperties(PaymentProperties::class)
@ConditionalOnProperty("mosaic.payment", havingValue = "true", matchIfMissing = true)
class PaymentConfig {
    @Bean
    @ConditionalOnProperty(prefix = "mosaic.payment.api", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun mosaicPaymentController(
        paymentProperties: PaymentProperties,
        goodsRepository: GoodsRepository,
        paymentOrderRepository: PaymentOrderRepositoryBase<*>,
    ): MosaicPaymentController {
        return MosaicPaymentController(
            paymentProperties = paymentProperties,
            goodsRepository = goodsRepository,
            paymentOrderRepository = paymentOrderRepository,
        )
    }
}