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

package io.mosaicboot.core.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.core.auth.oauth2.*
import io.mosaicboot.core.auth.repository.AuthenticationRepositoryBase
import io.mosaicboot.core.auth.MosaicAuthenticationHandler
import io.mosaicboot.core.auth.MosaicCookieAuthFilter
import io.mosaicboot.core.auth.MosaicOAuth2CredentialHandler
import io.mosaicboot.core.auth.controller.MosaicOAuth2Controller
import io.mosaicboot.core.user.service.MosaicOAuth2UserService
import io.mosaicboot.core.auth.service.AuthTokenService
import io.mosaicboot.core.auth.service.AuthenticationService
import io.mosaicboot.core.user.service.MosaicOAuth2TokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(
    name = ["org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService"]
)
class MosaicOAuth2Config(
    private val mosaicAuthProperties: MosaicAuthProperties,
) {
    private val oauth2AuthorizationRequestBaseUri = "${mosaicAuthProperties.api.path}/oauth2/request"

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
        objectMapper: ObjectMapper,
        authenticationService: AuthenticationService,
        authTokenService: AuthTokenService,
        oAuth2UserInfoHandlers: List<OAuth2UserInfoHandler>,
        @Autowired(required = false) mosaicOAuth2TokenService: MosaicOAuth2TokenService?,
    ): MosaicOAuth2UserService {
        return MosaicOAuth2UserService(
            mosaicAuthProperties = mosaicAuthProperties,
            objectMapper = objectMapper,
            authenticationService = authenticationService,
            authTokenService = authTokenService,
            oAuth2UserInfoHandlers = oAuth2UserInfoHandlers,
            mosaicOAuth2TokenService = mosaicOAuth2TokenService,
        )
    }

    @Bean
    fun mosaicOAuth2AuthorizedClientRepository(
        authTokenService: AuthTokenService,
        mosaicCookieAuthFilter: FilterRegistrationBean<MosaicCookieAuthFilter>,
        @Autowired(required = false) mosaicOAuth2TokenService: MosaicOAuth2TokenService?,
    ): MosaicOAuth2AuthorizedClientRepository {
        return MosaicOAuth2AuthorizedClientRepository(
            authTokenService = authTokenService,
            mosaicCookieAuthFilter = mosaicCookieAuthFilter.filter,
            mosaicOAuth2TokenService = mosaicOAuth2TokenService,
        )
    }

    @Bean
    fun mosaicOAuth2Controller(
        clientRegistrationRepository: ClientRegistrationRepository,
        mosaicOAuth2UserService: MosaicOAuth2UserService,
        authTokenService: AuthTokenService,
        mosaicAuthenticationHandler: MosaicAuthenticationHandler,
    ): MosaicOAuth2Controller {
        return MosaicOAuth2Controller(
            clientRegistrationRepository,
            oauth2AuthorizationRequestBaseUri,
            mosaicOAuth2UserService,
            authTokenService,
            mosaicAuthenticationHandler,
        )
    }

    @Bean
    @ConditionalOnBean(OAuth2AccessTokenRepository::class)
    fun mosaicOAuth2TokenService(
        objectMapper: ObjectMapper,
        authenticationRepository: AuthenticationRepositoryBase<*>,
        oAuth2AccessTokenRepository: OAuth2AccessTokenRepository,
        clientRegistrationRepository: ClientRegistrationRepository,
    ): MosaicOAuth2TokenService {
        return MosaicOAuth2TokenService(
            mosaicAuthProperties = mosaicAuthProperties,
            objectMapper = objectMapper,
            authenticationRepository = authenticationRepository,
            oAuth2AccessTokenRepository = oAuth2AccessTokenRepository,
            clientRegistrationRepository = clientRegistrationRepository,
        )
    }

    @Configuration(proxyBeanMethods = false)
    class WebConfig(
        private val mosaicAuthProperties: MosaicAuthProperties,
        private val mosaicAuthenticationHandler: MosaicAuthenticationHandler,
        private val mosaicOAuth2UserService: MosaicOAuth2UserService,
        private val mosaicOAuth2AuthorizedClientRepository: MosaicOAuth2AuthorizedClientRepository,
        private val mosaicAuthFilter: FilterRegistrationBean<MosaicCookieAuthFilter>,
        private val authenticationRepository: AuthenticationRepositoryBase<*>,
    ) : WebMvcConfigurer {
        private val oauth2AuthorizationRequestBaseUri = "${mosaicAuthProperties.api.path}/oauth2/request"

        private val mosaicOAuth2AuthorizationRequestResolver = MosaicOAuth2AuthorizationRequestResolver(
            oauth2AuthorizationRequestBaseUri,
            authenticationRepository,
            mosaicOAuth2UserService,
        )

        @Autowired
        fun setClientRegistrationRepository(clientRegistrationRepository: ClientRegistrationRepository) {
            mosaicOAuth2AuthorizationRequestResolver.setClientRegistrationRepository(clientRegistrationRepository)
        }

        @Bean
        @Order(-1)
        fun mosaicOAuth2SecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
            http
                .securityMatcher("${mosaicAuthProperties.api.path}/oauth2/**")
                .sessionManagement { it.disable() }
                .securityContext { it.disable() }
                .anonymous { it.disable() }
                .httpBasic { it.disable() }
                .formLogin { it.disable() }
                .logout { it.disable() }
                .csrf { it.disable() }
                .authorizeHttpRequests { authorizeHttpRequests ->
                    authorizeHttpRequests
                        .requestMatchers("${mosaicAuthProperties.api.path}/oauth2/authorize").authenticated()
                        .requestMatchers("${mosaicAuthProperties.api.path}/oauth2/**").permitAll()
                }
                .oauth2Login { oauth2Login ->
                    oauth2Login
                        .authorizedClientRepository(mosaicOAuth2AuthorizedClientRepository)
                        .authorizationEndpoint { endpoint ->
                            endpoint.baseUri(oauth2AuthorizationRequestBaseUri)
                                .authorizationRequestRepository(
                                    CookieAuthorizationRequestRepository(
                                        mosaicOAuth2UserService,
                                        mosaicAuthFilter.filter,
                                    )
                                )
                                .authorizationRequestResolver(mosaicOAuth2AuthorizationRequestResolver)
                        }
                        .redirectionEndpoint { endpoint ->
                            endpoint.baseUri("${mosaicAuthProperties.api.path}/oauth2/callback/*")
                        }
                        .userInfoEndpoint { endpoint ->
                            endpoint.userService(mosaicOAuth2UserService)
                        }
                        .successHandler(mosaicAuthenticationHandler)
                        .failureHandler(mosaicAuthenticationHandler)
                }
            val output = http.build()
            val oAuth2LoginAuthenticationFilter = output.filters.find { it.javaClass == OAuth2LoginAuthenticationFilter::class.java }!!
                as OAuth2LoginAuthenticationFilter
            oAuth2LoginAuthenticationFilter.setAuthenticationResultConverter { authenticationResult ->
                val authentication = AttributedOAuth2AuthenticationToken(
                    authenticationResult.principal,
                    authenticationResult.authorities,
                    authenticationResult.clientRegistration.registrationId,
                    authenticationResult.authorizationExchange.authorizationRequest.attributes
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