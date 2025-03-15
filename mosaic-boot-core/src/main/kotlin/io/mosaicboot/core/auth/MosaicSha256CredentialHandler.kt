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

package io.mosaicboot.core.auth

import io.mosaicboot.core.auth.entity.Authentication
import java.util.*

class MosaicSha256CredentialHandler : MosaicCredentialHandler {
    private val passwordEncoder = MosaicSha256PasswordEncoder()

    override fun methods(): List<String> {
        return listOf(
            "username:mosaic-sha256",
            "email:mosaic-sha256"
        )
    }

    override fun isRegistrable(method: String): Boolean {
        return true
    }

    override fun encode(
        method: String,
        username: String,
        credential: String,
    ): String {
        return passwordEncoder.encode(
            Base64.getDecoder().decode(credential)
        )
    }

    override fun validate(
        method: String,
        username: String,
        credential: String?,
        authentication: Authentication
    ): Boolean {
        if (credential == null || authentication.credential == null) {
            return false
        }
        return try {
            passwordEncoder.verify(
                Base64.getDecoder().decode(credential),
                authentication.credential!!
            )
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}