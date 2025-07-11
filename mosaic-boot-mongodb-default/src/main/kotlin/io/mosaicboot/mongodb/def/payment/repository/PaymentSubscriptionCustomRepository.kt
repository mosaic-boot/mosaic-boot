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

package io.mosaicboot.mongodb.def.payment.repository

import io.mosaicboot.mongodb.def.payment.entity.PaymentSubscriptionEntity
import io.mosaicboot.payment.db.dto.PaymentSubscriptionInput
import io.mosaicboot.payment.db.entity.SubscriptionStatus
import io.mosaicboot.payment.db.repository.PaymentSubscriptionMosaicRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import java.time.Instant
import java.util.stream.Stream

interface PaymentSubscriptionCustomRepository : PaymentSubscriptionMosaicRepository<PaymentSubscriptionEntity> {
    override fun save(input: PaymentSubscriptionInput): PaymentSubscriptionEntity
    override fun findLatestByUserIdAndGoodsId(userId: String, goodsId: String): PaymentSubscriptionEntity?
    override fun findCurrentByUserIdAndGoodsId(userId: String, goodsId: String): PaymentSubscriptionEntity?
    override fun findSubscriptions(
        userId: String,
        goodsId: String?,
        statuses: List<SubscriptionStatus>?,
        pageRequest: PageRequest,
    ): Page<PaymentSubscriptionEntity>

    override fun findSubscriptionsNeedingRenewal(now: Instant): Stream<PaymentSubscriptionEntity>
}
