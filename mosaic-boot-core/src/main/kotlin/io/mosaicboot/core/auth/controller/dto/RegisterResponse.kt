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

import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.common.auth.dto.RegisterFailureReason
import io.swagger.v3.oas.annotations.media.Schema

sealed class RegisterResponse(
    @field:JsonProperty("success")
    val success: Boolean,
) {
    @Schema(description = "Registration success")
    class Success() : RegisterResponse(
        success = true,
    )

    @Schema(description = "Registration failure")
    class Failure(
        @field:JsonProperty("reason")
        val reason: RegisterFailureReason,
    ) : RegisterResponse(
        success = false,
    )
}
