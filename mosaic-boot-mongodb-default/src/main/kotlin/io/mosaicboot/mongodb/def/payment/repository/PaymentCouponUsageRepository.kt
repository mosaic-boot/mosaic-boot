package io.mosaicboot.mongodb.def.payment.repository

import io.mosaicboot.mongodb.def.payment.entity.PaymentCouponUsageEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentCouponUsageRepository : MongoRepository<PaymentCouponUsageEntity, String>