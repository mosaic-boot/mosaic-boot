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

package io.mosaicboot.core.auth.oauth2

import org.springframework.security.oauth2.core.user.OAuth2User

class KakaoOAuth2UserInfoHandler : OAuth2UserInfoHandler {
    override fun getProviderName(): String {
        return "kakao"
    }

    override fun handle(oAuth2User: OAuth2User): OAuth2BasicInfo {
        val email = oAuth2User.getAttribute<Map<String, Any>>("kakao_account")?.get("email") as String?
        val nickname = oAuth2User.getAttribute<Map<String, Any>>("properties")?.get("nickname") as String?
        val name = oAuth2User.getAttribute<Map<String, Any>>("properties")?.get("name") as String?
        return OAuth2BasicInfo(
            provider = getProviderName(),
            id = oAuth2User.name,
            name = name ?: nickname ?: "Unknown",
            email = email,
        )
    }
}