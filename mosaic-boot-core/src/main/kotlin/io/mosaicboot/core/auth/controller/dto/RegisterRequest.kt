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

package io.mosaicboot.core.auth.controller.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

sealed class RegisterRequest(
    @field:Schema(description = "User's full name", required = true)
    val name: String,
    @field:Schema(description = "User's email address", required = true)
    val email: String,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Registration request payload", example = "{\n" +
        "  \"name\": \"Tester\",\n" +
        "  \"email\": \"test@example.com\",\n" +
        "  \"method\": \"username:mosaic-sha256\",\n" +
        "  \"username\": \"test\",\n" +
        "  \"credential\": \"z8MrYdsLzdccunILZfn7bmt0sARMLR9eemofkEmhz5s=\"\n" + // "1234"
        "}")
    class Plain(
        @JsonProperty("name")
        name: String,

        @JsonProperty("email")
        email: String,

        @JsonProperty("method")
        @field:Schema(description = "Registration method", required = true)
        val method: String,

        @JsonProperty("username")
        @field:Schema(description = "Username", required = true)
        val username: String,

        @JsonProperty("credential")
        @field:Schema(description = "User credential", required = true)
        val credential: String,
    ) : RegisterRequest(
        name = name,
        email = email,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    class OAuth2(
        @JsonProperty("name")
        name: String,

        @JsonProperty("email")
        email: String,
    ) : RegisterRequest(
        name = name,
        email = email,
    )

}
