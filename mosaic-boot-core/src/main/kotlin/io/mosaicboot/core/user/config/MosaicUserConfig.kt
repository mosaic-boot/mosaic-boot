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
import io.mosaicboot.core.auth.service.CredentialService
import io.mosaicboot.core.auth.repository.AuthenticationRepositoryBase
import io.mosaicboot.core.permission.service.PermissionService
import io.mosaicboot.core.user.repository.TenantUserRepositoryBase
import io.mosaicboot.core.user.repository.UserAuditLogRepositoryBase
import io.mosaicboot.core.user.repository.UserRepositoryBase
import io.mosaicboot.core.user.controller.UserController
import io.mosaicboot.core.user.service.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MosaicUserProperties::class)
@ConditionalOnProperty(prefix = "mosaic.user", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class MosaicUserConfig(
    private val mosaicUserProperties: MosaicUserProperties,
) {
    @Bean
    fun auditService(
        userAuditLogRepository: UserAuditLogRepositoryBase<*, *>,
    ): AuditService {
        return AuditService(userAuditLogRepository)
    }

    @Bean
    fun userService(
        userRepository: UserRepositoryBase<*>,
        tenantUserRepository: TenantUserRepositoryBase<*>,
        authenticationRepository: AuthenticationRepositoryBase<*>,
        auditService: AuditService,
        credentialService: CredentialService,
        objectMapper: ObjectMapper,
    ): UserService {
        return UserService(
            userRepository = userRepository,
            tenantUserRepository = tenantUserRepository,
            authenticationRepository = authenticationRepository,
            auditService = auditService,
            objectMapper = objectMapper,
        )
    }

    @Bean
    fun permissionService(
        userRepositoryBase: UserRepositoryBase<*>,
        tenantUserRepositoryBase: TenantUserRepositoryBase<*>,
    ): PermissionService {
        return PermissionService(
            userRepositoryBase = userRepositoryBase,
            tenantUserRepositoryBase = tenantUserRepositoryBase,
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "mosaic.user.api", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun mosaicUserController(
        userService: UserService,
    ): UserController {
        return UserController(
            userService = userService,
        )
    }

    @Configuration(proxyBeanMethods = true)
    class WebConfig(
        private val mosaicUserProperties: MosaicUserProperties,
    ) : WebMvcConfigurer {
        @Bean
        fun mosaicUserSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
            http
                .securityMatcher("${mosaicUserProperties.api.path}/**")
                .sessionManagement { it.disable() }
                .securityContext { it.disable() }
                .anonymous { it.disable() }
                .httpBasic { it.disable() }
                .formLogin { it.disable() }
                .logout { it.disable() }
                .csrf { it.disable() }
                .authorizeHttpRequests { authorizeHttpRequests ->
                    authorizeHttpRequests.requestMatchers("${mosaicUserProperties.api.path}/**").authenticated()
                }
                .exceptionHandling { exceptionHandling ->
                    exceptionHandling.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                }
            return http.build()
        }
    }
}
