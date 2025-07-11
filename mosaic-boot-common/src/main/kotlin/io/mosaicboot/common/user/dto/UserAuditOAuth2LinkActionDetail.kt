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

package io.mosaicboot.common.user.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.common.auth.dto.RegisterFailureReason

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
