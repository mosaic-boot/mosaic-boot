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

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.AuthenticatedPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User

class MosaicOAuth2RegisterToken(
    val token: String,
    val data: OAuth2RegisterTokenData,
) : OAuth2User, AuthenticatedPrincipal, AbstractAuthenticationToken(emptyList()) {
    private val attributes = HashMap<String, Any?>()

    override fun getName(): String = data.id

    override fun getAttributes(): MutableMap<String, Any?> {
        return attributes
    }

    override fun isAuthenticated(): Boolean {
        return false
    }

    override fun getCredentials(): Any? {
        return null
    }

    override fun getPrincipal(): String {
        return data.id
    }
}