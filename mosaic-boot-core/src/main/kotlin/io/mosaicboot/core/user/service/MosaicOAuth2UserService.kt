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
import io.mosaicboot.core.domain.user.AuthMethod
import io.mosaicboot.core.domain.vo.UserVo
import io.mosaicboot.core.user.auth.LoginResult
import io.mosaicboot.core.user.auth.RegisterResult
import io.mosaicboot.core.user.config.MosaicUserProperties
import io.mosaicboot.core.user.oauth2.*
import io.mosaicboot.core.util.ServerSideCrypto
import io.mosaicboot.core.util.UnreachableException
import io.mosaicboot.core.util.WebClientInfo
import io.mosaicboot.core.util.WebClientInfoResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Service
class MosaicOAuth2UserService(
    private val mosaicUserProperties: MosaicUserProperties,
    private val objectMapper: ObjectMapper,
    private val userService: UserService,
    private val authTokenService: AuthTokenService,
    oAuth2UserInfoHandlers: List<OAuth2UserInfoHandler>,
    @Autowired(required = false) private val mosaicOAuth2TokenService: MosaicOAuth2TokenService?,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    val serverSideCrypto = ServerSideCrypto(
        mosaicUserProperties.jwe,
        objectMapper = objectMapper,
    )

    private val oAuth2UserInfoHandlerMap = oAuth2UserInfoHandlers.associateBy { it.getProviderName() }

    @Transactional
    fun register(
        userTemplate: UserVo,
        webClientInfo: WebClientInfo,
        data: OAuth2RegisterTokenData,
    ): RegisterResult {
        val result = userService.register(
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

        val basicInfo = oAuth2UserInfoHandlerMap[userRequest.clientRegistration.registrationId]
            ?.handle(oAuth2User)
            ?: readInfoFromOther(userRequest.clientRegistration.registrationId, oAuth2User)

        val result = userService.login(
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