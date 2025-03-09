package io.mosaicboot.core.auth.oauth2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.core.jwt.JwtContentType

@JwtContentType("oauth2.authorization-request")
@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthorizationRequestJson(
    @field:JsonProperty("data")
    val data: ByteArray,
)