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

package io.mosaicboot.core.user.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.core.jwt.JwtContentType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken

@JwtContentType("oauth2.refresh-token")
@JsonIgnoreProperties(ignoreUnknown = true)
data class OAuth2RefreshTokenJson(
    @JsonProperty("value")
    val value: String,
    @JsonProperty("issuedAt")
    val issuedAt: Long?,
    @JsonProperty("expiresAt")
    val expiresAt: Long?,
) {
    companion object {
        fun copyFrom(token: OAuth2RefreshToken): OAuth2RefreshTokenJson {
            return OAuth2RefreshTokenJson(
                value = token.tokenValue,
                issuedAt = token.issuedAt?.epochSecond,
                expiresAt = token.expiresAt?.epochSecond,
            )
        }
    }
}