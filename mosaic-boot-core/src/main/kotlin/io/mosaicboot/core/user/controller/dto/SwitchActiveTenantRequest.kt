package io.mosaicboot.core.user.controller.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SwitchActiveTenantRequest(
    @JsonProperty("tenantId")
    val tenantId: String,
)