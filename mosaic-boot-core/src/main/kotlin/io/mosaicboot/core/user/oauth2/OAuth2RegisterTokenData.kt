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

package io.mosaicboot.core.user.oauth2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.core.jwt.JwtContentType
import io.mosaicboot.core.user.model.OAuth2AccessTokenJson
import io.mosaicboot.core.user.model.OAuth2RefreshTokenJson

@JwtContentType("oauth2-register")
@JsonIgnoreProperties(ignoreUnknown = true)
data class OAuth2RegisterTokenData(
    @field:JsonProperty("provider")
    val provider: String,
    @field:JsonProperty("id")
    val id: String,
    @field:JsonProperty("name")
    val name: String,
    @field:JsonProperty("email")
    val email: String?,
    @field:JsonProperty("accessToken")
    val accessToken: OAuth2AccessTokenJson,
    @field:JsonProperty("refreshToken")
    val refreshToken: OAuth2RefreshTokenJson?,
)
