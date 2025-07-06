package io.mosaicboot.payment.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.payment.db.entity.SubscriptionStatus

data class SubscriptionResponse(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("billingId")
    val billingId: String,
    @JsonProperty("optionId")
    val optionId: String?,
    @JsonProperty("billingCycle")
    val billingCycle: Int,
    @JsonProperty("usedCouponIds")
    val usedCouponIds: List<String>?,
    @JsonProperty("status")
    val status: SubscriptionStatus,
    /**
     * unix epoch seconds
     */
    @JsonProperty("validFrom")
    val validFrom: Long,
    /**
     * unix epoch seconds
     */
    @JsonProperty("validTo")
    val validTo: Long,
    @JsonProperty("scheduledOptionId")
    val scheduledOptionId: String?,
)
