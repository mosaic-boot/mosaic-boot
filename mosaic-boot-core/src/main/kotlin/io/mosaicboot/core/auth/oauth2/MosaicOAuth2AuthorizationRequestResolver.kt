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

import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.common.auth.enums.AuthMethod
import io.mosaicboot.core.auth.repository.AuthenticationRepositoryBase
import io.mosaicboot.core.encryption.ServerSideCrypto
import io.mosaicboot.core.user.service.MosaicOAuth2UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.web.util.UriComponentsBuilder

class MosaicOAuth2AuthorizationRequestResolver(
    private val authorizationRequestBaseUri: String,
    private val authenticationRepository: AuthenticationRepositoryBase<*>,
    private val mosaicOAuth2UserService: MosaicOAuth2UserService,
    private val serverSideCrypto: ServerSideCrypto,
) : OAuth2AuthorizationRequestResolver {
    private lateinit var clientRegistrationRepository: ClientRegistrationRepository
    private lateinit var delegate: DefaultOAuth2AuthorizationRequestResolver

    fun setClientRegistrationRepository(clientRegistrationRepository: ClientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository
        this.delegate = DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            authorizationRequestBaseUri
        )
    }

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return processAdditionalParameters(request, delegate.resolve(request))
    }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? {
        return processAdditionalParameters(request, delegate.resolve(request, clientRegistrationId))
    }

    private fun processAdditionalParameters(
        request: HttpServletRequest,
        authorizationRequest: OAuth2AuthorizationRequest?
    ): OAuth2AuthorizationRequest? {
        if (authorizationRequest == null) {
            return null
        }
        val registrationId = authorizationRequest.getAttribute<String>(OAuth2ParameterNames.REGISTRATION_ID)
        val authentication = SecurityContextHolder.getContext().authentication
        val authEntity = if (authentication is MosaicAuthenticatedToken) {
            authenticationRepository.findByUserIdAndMethod(
                userId = authentication.userId,
                method = "${AuthMethod.PREFIX_OAUTH2}:${registrationId}"
            )
        } else null

        val queryParams = HashMap<String, String>()
        queryParams["access_type"] = "offline"
        queryParams["prompt"] = "consent"
        authEntity?.let { queryParams["login_hint"] = it.username }

        val state = OAuth2AuthorizeState(
            requestType = request.getParameter("request_type"),
            redirectUri = request.getParameter("redirect_uri"),
        )
        val encryptedState = serverSideCrypto.encrypt(state)

        val additionalParameters = HashMap(authorizationRequest.additionalParameters)
        additionalParameters.putAll(queryParams)
        val customAuthorizationRequestUri = UriComponentsBuilder
            .fromUriString(authorizationRequest.authorizationRequestUri)
            .also { builder ->
                builder.replaceQueryParam("state", encryptedState)
                queryParams.forEach { (k, v) ->
                    builder.queryParam(k, v)
                }
            }
            .build(true)
            .toUriString()

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .additionalParameters(additionalParameters)
            .authorizationRequestUri(customAuthorizationRequestUri)
            .state(encryptedState)
            .build()
    }
}
