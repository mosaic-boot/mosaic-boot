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

import io.mosaicboot.core.util.WebClientInfo
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.core.user.OAuth2User

class TemporaryOAuth2User(
    val webClientInfo: WebClientInfo,
    val basicInfo: OAuth2BasicInfo,
) : OAuth2User {
    private val attributes = HashMap<String, Any?>()

    var client: OAuth2AuthorizedClient? = null

    override fun getName(): String {
        return basicInfo.id
    }

    override fun getAttributes(): MutableMap<String, Any?> = attributes

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()
}