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

import io.mosaicboot.core.user.auth.MosaicCookieAuthFilter
import io.mosaicboot.core.user.model.OAuth2AccessTokenJson
import io.mosaicboot.core.user.model.OAuth2RefreshTokenJson
import io.mosaicboot.core.auth.service.AuthTokenService
import io.mosaicboot.core.user.service.MosaicOAuth2TokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository

class MosaicOAuth2AuthorizedClientRepository(
    private val authTokenService: AuthTokenService,
    private val mosaicCookieAuthFilter: MosaicCookieAuthFilter,
    @Autowired(required = false) private val mosaicOAuth2TokenService: MosaicOAuth2TokenService?,
) : OAuth2AuthorizedClientRepository {
    override fun <T : OAuth2AuthorizedClient> loadAuthorizedClient(
        clientRegistrationId: String,
        principal: Authentication,
        request: HttpServletRequest
    ): T? {
        return null
    }

    override fun saveAuthorizedClient(
        authorizedClient: OAuth2AuthorizedClient,
        authentication: Authentication,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authentication !is OAuth2AuthenticationToken) {
            throw RuntimeException("principal is not OAuth2AuthenticationToken: ${authentication.javaClass}")
        }

        val principal = authentication.principal
        when (principal) {
            is TemporaryOAuth2User -> {
                val token = authTokenService.issueSocialRegisterTokenData(
                    webClientInfo = principal.webClientInfo,
                    basicInfo = principal.basicInfo,
                    accessToken = OAuth2AccessTokenJson.copyFrom(authorizedClient.accessToken),
                    refreshToken = authorizedClient.refreshToken?.let { OAuth2RefreshTokenJson.copyFrom(it) }
                )
                mosaicCookieAuthFilter.applyOAuth2RegisterToken(
                    request,
                    response,
                    token
                )
            }

            is AuthenticatedOAuth2User -> {
                mosaicOAuth2TokenService?.update(
                    principal.authenticatedToken.userId,
                    principal.authenticatedToken.authenticationId,
                    authorizedClient,
                )
            }
        }
    }

    override fun removeAuthorizedClient(
        clientRegistrationId: String,
        principal: Authentication,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (principal !is OAuth2AuthenticationToken) {
            return
        }
    }
}