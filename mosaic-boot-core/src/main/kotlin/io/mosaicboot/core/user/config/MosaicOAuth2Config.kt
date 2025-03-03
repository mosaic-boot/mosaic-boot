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

package io.mosaicboot.core.user.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.core.repository.AuthenticationRepositoryBase
import io.mosaicboot.core.user.auth.MosaicAuthenticationHandler
import io.mosaicboot.core.user.auth.MosaicCookieAuthFilter
import io.mosaicboot.core.user.auth.MosaicOAuth2CredentialHandler
import io.mosaicboot.core.user.controller.MosaicOAuth2Controller
import io.mosaicboot.core.user.model.OAuth2AccessTokenJson
import io.mosaicboot.core.user.oauth2.*
import io.mosaicboot.core.user.service.MosaicOAuth2UserService
import io.mosaicboot.core.user.service.AuthTokenService
import io.mosaicboot.core.user.service.MosaicOAuth2TokenService
import io.mosaicboot.core.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(
    name = ["org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService"]
)
class MosaicOAuth2Config {
    @Bean
    fun mosaicOAuth2CredentialHandler(): MosaicOAuth2CredentialHandler {
        return MosaicOAuth2CredentialHandler()
    }

    @Bean
    fun kakaoOAuth2UserInfoHandler(): KakaoOAuth2UserInfoHandler {
        return KakaoOAuth2UserInfoHandler()
    }

    @Bean
    fun mosaicOAuth2UserService(
        mosaicUserProperties: MosaicUserProperties,
        objectMapper: ObjectMapper,
        userService: UserService,
        authTokenService: AuthTokenService,
        oAuth2UserInfoHandlers: List<OAuth2UserInfoHandler>,
        @Autowired(required = false) mosaicOAuth2TokenService: MosaicOAuth2TokenService?,
    ): MosaicOAuth2UserService {
        return MosaicOAuth2UserService(
            mosaicUserProperties = mosaicUserProperties,
            objectMapper = objectMapper,
            userService = userService,
            authTokenService = authTokenService,
            oAuth2UserInfoHandlers = oAuth2UserInfoHandlers,
            mosaicOAuth2TokenService = mosaicOAuth2TokenService,
        )
    }

    @Bean
    fun mosaicOAuth2AuthorizedClientRepository(
        authTokenService: AuthTokenService,
        mosaicCookieAuthFilter: MosaicCookieAuthFilter,
        @Autowired(required = false) mosaicOAuth2TokenService: MosaicOAuth2TokenService?,
    ): MosaicOAuth2AuthorizedClientRepository {
        return MosaicOAuth2AuthorizedClientRepository(
            authTokenService = authTokenService,
            mosaicCookieAuthFilter = mosaicCookieAuthFilter,
            mosaicOAuth2TokenService = mosaicOAuth2TokenService,
        )
    }

    @Bean
    fun mosaicOAuth2Controller(
        mosaicOAuth2UserService: MosaicOAuth2UserService,
    ): MosaicOAuth2Controller {
        return MosaicOAuth2Controller(
            mosaicOAuth2UserService
        )
    }

    @Bean
    @ConditionalOnBean(OAuth2AccessTokenRepository::class)
    fun mosaicOAuth2TokenService(
        mosaicUserProperties: MosaicUserProperties,
        objectMapper: ObjectMapper,
        authenticationRepository: AuthenticationRepositoryBase<*>,
        oAuth2AccessTokenRepository: OAuth2AccessTokenRepository,
        clientRegistrationRepository: ClientRegistrationRepository,
    ): MosaicOAuth2TokenService {
        return MosaicOAuth2TokenService(
            mosaicUserProperties = mosaicUserProperties,
            objectMapper = objectMapper,
            authenticationRepository = authenticationRepository,
            oAuth2AccessTokenRepository = oAuth2AccessTokenRepository,
            clientRegistrationRepository = clientRegistrationRepository,
        )
    }

    @Configuration(proxyBeanMethods = true)
    class WebConfig(
        private val mosaicUserProperties: MosaicUserProperties,
        private val mosaicAuthenticationHandler: MosaicAuthenticationHandler,
        private val mosaicOAuth2UserService: MosaicOAuth2UserService,
        private val mosaicOAuth2AuthorizedClientRepository: MosaicOAuth2AuthorizedClientRepository,
    ) : WebMvcConfigurer {
        @Bean
        @Order(-2)
        fun mosaicOAuth2SecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
            http
                .securityMatcher("${mosaicUserProperties.api.path}/oauth2/**")
                .csrf { it.disable() }
                .authorizeHttpRequests { authorizeHttpRequests ->
                    authorizeHttpRequests.anyRequest().permitAll()
                }
                .oauth2Login { oauth2Login ->
                    oauth2Login.authorizedClientRepository(mosaicOAuth2AuthorizedClientRepository)
                    oauth2Login.authorizationEndpoint { endpoint ->
                        endpoint.baseUri("${mosaicUserProperties.api.path}/oauth2/request")
                    }
                    oauth2Login.redirectionEndpoint { endpoint ->
                        endpoint.baseUri("${mosaicUserProperties.api.path}/oauth2/callback/*")
                    }
                    oauth2Login.userInfoEndpoint { endpoint ->
                        endpoint.userService(mosaicOAuth2UserService)
                    }
                    oauth2Login.successHandler(mosaicAuthenticationHandler)
                }
            val output = http.build()
            val oAuth2LoginAuthenticationFilter = output.filters.find { it.javaClass == OAuth2LoginAuthenticationFilter::class.java }!!
                as OAuth2LoginAuthenticationFilter
            oAuth2LoginAuthenticationFilter.setAuthenticationResultConverter { authenticationResult ->
                val authentication = OAuth2AuthenticationToken(
                    authenticationResult.principal, authenticationResult.authorities,
                    authenticationResult.clientRegistration.registrationId
                )
                if (authenticationResult.principal is TemporaryOAuth2User) {
                    authentication.isAuthenticated = false
                }
                authentication
            }
            return output
        }
    }
}