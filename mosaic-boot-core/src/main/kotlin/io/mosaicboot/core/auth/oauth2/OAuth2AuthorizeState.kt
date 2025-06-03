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