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
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Login request payload", example = "{\n" +
    "  \"method\": \"username:mosaic-sha256\",\n" +
    "  \"username\": \"test\",\n" +
    "  \"credential\": \"z8MrYdsLzdccunILZfn7bmt0sARMLR9eemofkEmhz5s=\"\n" + // "1234"
    "}")
@JsonIgnoreProperties(ignoreUnknown = true)
data class LoginRequest(
    @JsonProperty("method")
    @field:Schema(description = "Login method", required = true)
    val method: String,

    @JsonProperty("username")
    @field:Schema(description = "Username", required = true)
    val username: String,

    @JsonProperty("credential")
    @field:Schema(description = "User credential", required = true)
    val credential: String
)
