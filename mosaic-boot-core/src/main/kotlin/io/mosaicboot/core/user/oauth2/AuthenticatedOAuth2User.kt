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

import io.mosaicboot.core.user.auth.MosaicAuthenticatedToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class AuthenticatedOAuth2User(
    val authenticatedToken: MosaicAuthenticatedToken,
    val oAuth2User: OAuth2User,
) : OAuth2User {
    override fun getName(): String = oAuth2User.name

    override fun getAttributes(): MutableMap<String, Any> = oAuth2User.attributes

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()
}