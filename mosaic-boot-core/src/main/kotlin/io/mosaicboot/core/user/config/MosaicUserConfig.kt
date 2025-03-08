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
import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicRequestMappingHandlerMapping
import io.mosaicboot.core.repository.AuthenticationRepositoryBase
import io.mosaicboot.core.repository.TenantUserRepositoryBase
import io.mosaicboot.core.repository.UserAuditLogRepositoryBase
import io.mosaicboot.core.repository.UserRepositoryBase
import io.mosaicboot.core.user.auth.*
import io.mosaicboot.core.user.controller.AuthController
import io.mosaicboot.core.user.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.util.pattern.PathPatternParser
import java.util.function.Predicate


@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MosaicUserProperties::class)
@ConditionalOnProperty(prefix = "mosaic.user", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@Import(
    MosaicOAuth2Config::class,
)
class MosaicUserConfig(
    private val mosaicUserProperties: MosaicUserProperties,
) {
    @Bean
    fun mosaicSha256CredentialHandler(): MosaicSha256CredentialHandler {
        return MosaicSha256CredentialHandler()
    }

    @Bean
    fun auditService(
        userAuditLogRepository: UserAuditLogRepositoryBase<*, *>,
    ): AuditService {
        return AuditService(userAuditLogRepository)
    }

    @Bean
    fun authenticationService(
        authenticationRepository: AuthenticationRepositoryBase<*>,
        credentialHandlers: List<MosaicCredentialHandler>,
        objectMapper: ObjectMapper,
    ): AuthenticationService {
        return AuthenticationService(
            authenticationRepository = authenticationRepository,
            credentialHandlers = credentialHandlers,
            objectMapper = objectMapper,
        )
    }

    @Bean
    fun userService(
        userRepository: UserRepositoryBase<*>,
        tenantUserRepository: TenantUserRepositoryBase<*>,
        authenticationRepository: AuthenticationRepositoryBase<*>,
        credentialHandlers: List<MosaicCredentialHandler>,
        auditService: AuditService,
        authenticationService: AuthenticationService,
        objectMapper: ObjectMapper,
    ): UserService {
        return UserService(
            userRepository = userRepository,
            tenantUserRepository = tenantUserRepository,
            authenticationRepository = authenticationRepository,
            authenticationService = authenticationService,
            auditService = auditService,
            objectMapper = objectMapper,
        )
    }

    @Bean
    fun authTokenService(
        objectMapper: ObjectMapper,
        userService: UserService,
    ): AuthTokenService {
        return AuthTokenService(
            mosaicUserProperties = mosaicUserProperties,
            objectMapper = objectMapper,
            userService = userService,
        )
    }

    @Bean
    fun mosaicAuthFilter(
        mosaicUserProperties: MosaicUserProperties,
        authTokenService: AuthTokenService,
        objectMapper: ObjectMapper,
    ): FilterRegistrationBean<MosaicCookieAuthFilter> {
        val registrationBean = FilterRegistrationBean<MosaicCookieAuthFilter>()
        registrationBean.filter = MosaicCookieAuthFilter(
            mosaicUserProperties = mosaicUserProperties,
            authTokenService = authTokenService,
            objectMapper = objectMapper,
        )
        registrationBean.order = SecurityProperties.DEFAULT_FILTER_ORDER - 1
        return registrationBean
    }

    @Bean
    @ConditionalOnProperty(prefix = "mosaic.user.api", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun authController(
        userService: UserService,
        authTokenService: AuthTokenService,
        mosaicAuthenticationHandler: MosaicAuthenticationHandler,
        @Autowired(required = false) mosaicOAuth2UserService: MosaicOAuth2UserService?,
    ): AuthController {
        return AuthController(
            userService = userService,
            authTokenService = authTokenService,
            mosaicAuthenticationHandler = mosaicAuthenticationHandler,
            mosaicOAuth2UserService = mosaicOAuth2UserService,
        )
    }

    @Bean
    fun mosaicAuthenticationHandler(
        mosaicUserProperties: MosaicUserProperties,
        mosaicCookieAuthFilter: FilterRegistrationBean<MosaicCookieAuthFilter>,
    ): MosaicAuthenticationHandler {
        return MosaicAuthenticationHandler(
            mosaicUserProperties = mosaicUserProperties,
            mosaicCookieAuthFilter = mosaicCookieAuthFilter.filter,
        )
    }

    @Bean
    @ConditionalOnBean(AuthController::class)
    fun mosaicUserControllerMapping(
        applicationContext: ApplicationContext,
        controllers: List<BaseMosaicController>,
    ): MosaicRequestMappingHandlerMapping {
        val requestMappingHandlerMapping = MosaicRequestMappingHandlerMapping()
        val urlMap = HashMap<String, Predicate<Class<*>>>()
        controllers.forEach { controller ->
            urlMap[controller.getBaseUrl(applicationContext)] = Predicate<Class<*>> {
                    it: Class<*> -> it == controller.javaClass
            }
        }
        requestMappingHandlerMapping.order = -1
        requestMappingHandlerMapping.pathPrefixes = urlMap
        requestMappingHandlerMapping.patternParser = PathPatternParser()
        return requestMappingHandlerMapping
    }

    @Configuration(proxyBeanMethods = true)
    class WebConfig(
        private val mosaicUserProperties: MosaicUserProperties,
        private val mosaicAuthenticationHandler: MosaicAuthenticationHandler,
    ) : WebMvcConfigurer {
        @Bean
        fun mosaicUserSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
            http
                .securityMatcher("${mosaicUserProperties.api.path}/**")
                .csrf { it.disable() }
                .authorizeHttpRequests { authorizeHttpRequests ->
                    authorizeHttpRequests.requestMatchers("${mosaicUserProperties.api.path}/current").authenticated()
                    authorizeHttpRequests.requestMatchers("${mosaicUserProperties.api.path}/current/**").authenticated()
                    authorizeHttpRequests.requestMatchers("${mosaicUserProperties.api.path}/**").permitAll()
                }
                .logout { logout ->
                    logout.logoutUrl("${mosaicUserProperties.api.path}/logout")
                        .addLogoutHandler(mosaicAuthenticationHandler)
                        .logoutSuccessHandler(SimpleUrlLogoutSuccessHandler().apply {
                            setTargetUrlParameter("finish_to")
                        })
                }
                .exceptionHandling { exceptionHandling ->
                    exceptionHandling.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                }
            return http.build()
        }
    }
}
