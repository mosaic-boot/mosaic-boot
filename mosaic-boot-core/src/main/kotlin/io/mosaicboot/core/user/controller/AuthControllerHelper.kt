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

package io.mosaicboot.core.user.controller

import io.mosaicboot.core.auth.dto.RegisterResult
import io.mosaicboot.core.auth.controller.dto.RegisterResponse
import io.mosaicboot.core.util.UnreachableException
import org.springframework.http.ResponseEntity

fun RegisterResult.toResponseEntity(): ResponseEntity<RegisterResponse> {
    return when (this) {
        is RegisterResult.Success -> ResponseEntity.ok(
            RegisterResponse.Success()
        )

        is RegisterResult.Failure -> ResponseEntity.badRequest().body(
            RegisterResponse.Failure(
                reason = this.reason,
            )
        )

        else -> throw UnreachableException()
    }
}
