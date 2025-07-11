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
import io.mosaicboot.payment.controller.MosaicPaymentSubscriptionController
import io.mosaicboot.payment.db.repository.PaymentBillingRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentCouponRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentGoodsRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentSubscriptionLogRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentSubscriptionRenewRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentSubscriptionRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentTransactionRepositoryBase
import io.mosaicboot.payment.service.PaymentService
import io.mosaicboot.payment.service.PaymentSubscriptionScheduleService
import io.mosaicboot.payment.service.PaymentSubscriptionService
import io.mosaicboot.payment.service.PgRouter
import io.mosaicboot.payment.service.PgService
import io.mosaicboot.payment.service.SinglePgRouter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@EnableConfigurationProperties(PaymentProperties::class)
@ConditionalOnProperty("mosaic.payment", havingValue = "true", matchIfMissing = true)
class PaymentConfig {
    @Bean
    @ConditionalOnMissingBean(PgRouter::class)
    fun pgRouter(
        pgServices: List<PgService>,
    ): SinglePgRouter {
        if (pgServices.size != 1) {
            throw IllegalStateException("pgServices is not single (count: ${pgServices.size})")
        }
        return SinglePgRouter(pgServices.first())
    }

    @Bean
    fun paymentService(
        pgRouter: PgRouter,
        paymentBillingRepository: PaymentBillingRepositoryBase<*>,
        paymentCouponRepository: PaymentCouponRepositoryBase<*>,
        paymentGoodsRepository: PaymentGoodsRepositoryBase<*>,
        paymentTransactionRepository: PaymentTransactionRepositoryBase<*>,
        paymentSubscriptionRepository: PaymentSubscriptionRepositoryBase<*>,
        paymentSubscriptionLogRepository: PaymentSubscriptionLogRepositoryBase<*>,
    ): PaymentService {
        return PaymentService(
            pgRouter = pgRouter,
            paymentBillingRepository = paymentBillingRepository,
            paymentCouponRepository = paymentCouponRepository,
            paymentGoodsRepository = paymentGoodsRepository,
            paymentTransactionRepository = paymentTransactionRepository,
            paymentSubscriptionRepository = paymentSubscriptionRepository,
            paymentSubscriptionLogRepository = paymentSubscriptionLogRepository,
        )
    }

    @Bean
    fun paymentSubscriptionService(
        pgRouter: PgRouter,
        paymentService: PaymentService,
        paymentBillingRepository: PaymentBillingRepositoryBase<*>,
        paymentCouponRepository: PaymentCouponRepositoryBase<*>,
        paymentGoodsRepository: PaymentGoodsRepositoryBase<*>,
        paymentTransactionRepository: PaymentTransactionRepositoryBase<*>,
        paymentSubscriptionRepository: PaymentSubscriptionRepositoryBase<*>,
        paymentSubscriptionLogRepository: PaymentSubscriptionLogRepositoryBase<*>,
        paymentSubscriptionRenewRepository: PaymentSubscriptionRenewRepositoryBase<*>,
    ): PaymentSubscriptionService {
        return PaymentSubscriptionService(
            pgRouter = pgRouter,
            paymentService = paymentService,
            paymentBillingRepository = paymentBillingRepository,
            paymentCouponRepository = paymentCouponRepository,
            paymentGoodsRepository = paymentGoodsRepository,
            paymentTransactionRepository = paymentTransactionRepository,
            paymentSubscriptionRepository = paymentSubscriptionRepository,
            paymentSubscriptionLogRepository = paymentSubscriptionLogRepository,
            paymentSubscriptionRenewRepository = paymentSubscriptionRenewRepository,
        )
    }

    @Bean
    fun paymentSubscriptionScheduleService(
        paymentSubscriptionService: PaymentSubscriptionService,
        paymentSubscriptionRepository: PaymentSubscriptionRepositoryBase<*>,
        paymentSubscriptionRenewRepository: PaymentSubscriptionRenewRepositoryBase<*>,
    ): PaymentSubscriptionScheduleService {
        return PaymentSubscriptionScheduleService(
            paymentSubscriptionService = paymentSubscriptionService,
            paymentSubscriptionRepository = paymentSubscriptionRepository,
            paymentSubscriptionRenewRepository = paymentSubscriptionRenewRepository,
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "mosaic.payment.api", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun mosaicPaymentController(
        paymentProperties: PaymentProperties,
        goodsRepository: PaymentGoodsRepositoryBase<*>,
        paymentOrderRepository: PaymentTransactionRepositoryBase<*>,
        paymentService: PaymentService,
    ): MosaicPaymentController {
        return MosaicPaymentController(
            paymentProperties = paymentProperties,
            goodsRepository = goodsRepository,
            paymentTransactionRepository = paymentOrderRepository,
            paymentService = paymentService,
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "mosaic.payment.subscription.api", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun mosaicPaymentSubscriptionController(
        paymentProperties: PaymentProperties,
        goodsRepository: PaymentGoodsRepositoryBase<*>,
        paymentOrderRepository: PaymentTransactionRepositoryBase<*>,
        paymentService: PaymentService,
        paymentSubscriptionService: PaymentSubscriptionService,
    ): MosaicPaymentSubscriptionController {
        return MosaicPaymentSubscriptionController(
            paymentProperties = paymentProperties,
            goodsRepository = goodsRepository,
            paymentTransactionRepository = paymentOrderRepository,
            paymentService = paymentService,
            paymentSubscriptionService = paymentSubscriptionService,
        )
    }
}