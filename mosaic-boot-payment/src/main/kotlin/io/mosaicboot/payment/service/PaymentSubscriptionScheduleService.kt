package io.mosaicboot.payment.service

import io.mosaicboot.core.util.UUIDv7
import io.mosaicboot.payment.db.entity.PaymentSubscriptionRenewInput
import io.mosaicboot.payment.db.entity.PaymentSubscriptionRenewStatus
import io.mosaicboot.payment.db.repository.PaymentSubscriptionRenewRepositoryBase
import io.mosaicboot.payment.db.repository.PaymentSubscriptionRepositoryBase
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@Service
class PaymentSubscriptionScheduleService(
    private val paymentSubscriptionService: PaymentSubscriptionService,
    private val paymentSubscriptionRepository: PaymentSubscriptionRepositoryBase<*>,
    private val paymentSubscriptionRenewRepository: PaymentSubscriptionRenewRepositoryBase<*>,
) {
    companion object {
        private val log = LoggerFactory.getLogger(PaymentSubscriptionScheduleService::class.java)
    }

    private val renewalTaskRunning = AtomicBoolean(false)

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.MINUTES)
    fun triggerRenewal() {
        if (renewalTaskRunning.compareAndSet(false, true)) {
            try {
                renewal()
            } finally {
                renewalTaskRunning.set(false)
            }
        }
    }

    private fun renewal() {
        log.info("start renewal")

        val stream = paymentSubscriptionRepository.findSubscriptionsNeedingRenewal(Instant.now())
        stream.forEach { entity ->
            log.info("renewal[id={}]", entity.id)

            val nextPaymentCount = entity.paymentCount + 1
            val idempotentKey = "${entity.id}-$nextPaymentCount"

            val now = Instant.now()
            val entity = paymentSubscriptionRenewRepository.saveOrIgnore(
                PaymentSubscriptionRenewInput(
                    id = UUIDv7.generate().toString(),
                    createdAt = now,
                    updatedAt = now,
                    userId = entity.userId,
                    subscriptionId = entity.id,
                    idempotentKey = idempotentKey,
                    paymentCount = nextPaymentCount,
                    status = PaymentSubscriptionRenewStatus.PENDING
                )
            )
            if (entity != null) {
                paymentSubscriptionService.renewSubscription(entity)
            } else {
                log.warn("renewal[idempotentKey={}] duplicate key, ignore", idempotentKey)
            }
        }
    }
}
