package io.mosaicboot.core.user.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.core.auth.controller.dto.RegisterFailureReason

data class UserAuditOAuth2LinkActionDetail(
    @JsonProperty("method")
    val method: String,
    @JsonProperty("username")
    val username: String,
    @JsonProperty("authenticationId")
    val authenticationId: String? = null,
    @JsonProperty("failureReason")
    val failureReason: RegisterFailureReason? = null,
)
