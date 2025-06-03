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

package io.mosaicboot.core.user.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.auth.AuthenticationRedirectException
import io.mosaicboot.core.auth.config.MosaicAuthProperties
import io.mosaicboot.core.auth.oauth2.*
import io.mosaicboot.core.auth.service.AuthTokenService
import io.mosaicboot.core.auth.service.AuthenticationService
import io.mosaicboot.core.auth.enums.AuthMethod
import io.mosaicboot.core.user.dto.UserInput
import io.mosaicboot.core.auth.dto.LoginResult
import io.mosaicboot.core.auth.dto.RegisterResult
import io.mosaicboot.core.util.ServerSideCrypto
import io.mosaicboot.core.util.UnreachableException
import io.mosaicboot.core.util.WebClientInfo
import io.mosaicboot.core.util.WebClientInfoResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.util.UriComponentsBuilder

@Service
class MosaicOAuth2UserService(
    private val mosaicAuthProperties: MosaicAuthProperties,
    private val objectMapper: ObjectMapper,
    private val authenticationService: AuthenticationService,
    private val authTokenService: AuthTokenService,
    oAuth2UserInfoHandlers: List<OAuth2UserInfoHandler>,
    @Autowired(required = false) private val mosaicOAuth2TokenService: MosaicOAuth2TokenService?,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    val serverSideCrypto = ServerSideCrypto(
        mosaicAuthProperties.jwe,
        objectMapper = objectMapper,
    )

    private val oAuth2UserInfoHandlerMap = oAuth2UserInfoHandlers.associateBy { it.getProviderName() }

    @Transactional
    fun register(
        userTemplate: UserInput,
        webClientInfo: WebClientInfo,
        data: OAuth2RegisterTokenData,
    ): RegisterResult {
        val result = authenticationService.register(
            userTemplate = userTemplate,
            method = "${AuthMethod.PREFIX_OAUTH2}:${data.provider}",
            username = data.id,
            credential = data.refreshToken?.let {
                serverSideCrypto.encrypt(it)
            },
            webClientInfo = webClientInfo,
        )

        if (result is RegisterResult.Success) {
            mosaicOAuth2TokenService?.updateAccessToken(
                userId = result.user.id,
                authenticationId = result.authentication.id,
                accessTokenJson = data.accessToken,
            )
        }

        return result
    }


    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)
        val attributes = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val webClientInfo = WebClientInfoResolver.fromHttpServletRequest(attributes.request)
        val state = serverSideCrypto.decrypt(attributes.request.getParameter("state"), OAuth2AuthorizeState::class.java)

        val basicInfo = oAuth2UserInfoHandlerMap[userRequest.clientRegistration.registrationId]
            ?.handle(oAuth2User)
            ?: readInfoFromOther(userRequest.clientRegistration.registrationId, oAuth2User)

        if (state.requestType == OAuth2AuthorizeState.REQUEST_TYPE_LINK) {
            val authentication = SecurityContextHolder.getContext().authentication as MosaicAuthenticatedToken
            val existingAuthentication = authenticationService.findAuthenticationDetail(
                "${AuthMethod.PREFIX_OAUTH2}:${basicInfo.provider}",
                basicInfo.id,
            )
            if (existingAuthentication != null) {
                if (authentication.userId == existingAuthentication.userId) {
                    // 이미 가입되어
                    throw AuthenticationRedirectException(
                        message = "mosaic.auth.oauth2.link.already-linked-self",
                        redirectUri = state.redirectUri?.let {
                            UriComponentsBuilder.fromUriString(it)
                                .replaceQueryParam("status", "error")
                                .replaceQueryParam("error", "already-linked-self")
                                .build().toString()
                        }
                    )
                } else {
                    throw AuthenticationRedirectException(
                        message = "mosaic.auth.oauth2.link.already-linked-other-user",
                        redirectUri = state.redirectUri?.let {
                            UriComponentsBuilder.fromUriString(it)
                                .replaceQueryParam("status", "error")
                                .replaceQueryParam("error", "already-linked-other-user")
                                .build().toString()
                        }
                    )
                }
            }
            val newAuthentication = authenticationService.addLoginMethod(
                authentication.userId,
                "${AuthMethod.PREFIX_OAUTH2}:${basicInfo.provider}",
                basicInfo.id,
                null,
                webClientInfo
            )
            return NewLinkOAuth2User(
                webClientInfo,
                basicInfo,
                authentication.userId,
                newAuthentication.id
            )
        }

        val result = authenticationService.login(
            "${AuthMethod.PREFIX_OAUTH2}:${basicInfo.provider}",
            basicInfo.id,
            null,
            webClientInfo,
        )
        when (result) {
            is LoginResult.Success -> {
                return AuthenticatedOAuth2User(
                    authenticatedToken = authTokenService.issueAuthenticatedToken(
                        webClientInfo,
                        result.user,
                        result.authentication,
                        result.tenantUsers,
                    ),
                    oAuth2User = oAuth2User,
                )
            }
            is LoginResult.Failure -> {
                return TemporaryOAuth2User(
                    webClientInfo,
                    basicInfo,
                )
            }
            else -> throw UnreachableException()
        }
    }

    private fun readInfoFromOther(provider: String, oAuth2User: OAuth2User): OAuth2BasicInfo {
        val name = oAuth2User.getAttribute<String?>("name")
        val email = oAuth2User.getAttribute<String?>("email")
        return OAuth2BasicInfo(
            provider = provider,
            id = oAuth2User.name,
            name = name ?: "Unknown",
            email = email,
        )
    }
}