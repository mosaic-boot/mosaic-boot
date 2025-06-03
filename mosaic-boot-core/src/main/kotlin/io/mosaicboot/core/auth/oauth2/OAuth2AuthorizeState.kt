package io.mosaicboot.core.auth.oauth2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.core.jwt.JwtContentType
import java.util.*

@JwtContentType("oauth2.authorization-state")
@JsonIgnoreProperties(ignoreUnknown = true)
data class OAuth2AuthorizeState(
    @JsonProperty("req")
    @field:JsonProperty("req")
    val requestId: String = UUID.randomUUID().toString(),
    @JsonProperty("typ")
    @field:JsonProperty("typ")
    val requestType: String? = null,
    @JsonProperty("to")
    @field:JsonProperty("to")
    val redirectUri: String? = null,
) {
    companion object {
        const val REQUEST_TYPE_LINK = "link"
    }
}