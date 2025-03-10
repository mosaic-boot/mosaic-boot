package io.mosaicboot.core.auth.oauth2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.mosaicboot.core.jwt.JwtContentType
import io.mosaicboot.core.util.JacksonByteArrayConverter

@JwtContentType("oauth2.authorization-request")
@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthorizationRequestJson(
    @JsonProperty("data")
    @JsonDeserialize(using = JacksonByteArrayConverter.Deserializer::class)
    @field:JsonProperty("data")
    @field:JsonSerialize(using = JacksonByteArrayConverter.Serializer::class)
    val data: ByteArray,
)