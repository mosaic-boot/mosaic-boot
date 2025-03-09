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

package io.mosaicboot.core.user.auth

import io.mosaicboot.core.domain.user.AuthMethod
import io.mosaicboot.core.domain.user.Authentication

class MosaicOAuth2CredentialHandler : MosaicCredentialHandler {
    override fun methods(): List<String>? {
        return null
    }

    override fun isRegistrable(method: String): Boolean? {
        if (isOAuth2Method(method)) {
            return false
        }
        return null
    }

    override fun encode(method: String, username: String, credential: String): String {
        return credential
    }

    override fun validate(
        method: String,
        username: String,
        credential: String?,
        authentication: Authentication
    ): Boolean? {
        if (isOAuth2Method(method)) {
            return username.isNotEmpty()
        }
        return null
    }

    private fun isOAuth2Method(method: String): Boolean {
        return method.startsWith("${AuthMethod.PREFIX_OAUTH2}:")
    }
}