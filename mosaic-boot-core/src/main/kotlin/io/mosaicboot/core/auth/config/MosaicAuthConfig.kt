package io.mosaicboot.core.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.core.user.auth.MosaicAuthenticationHandler
import io.mosaicboot.core.user.auth.MosaicCookieAuthFilter
import io.mosaicboot.core.user.auth.MosaicSha256CredentialHandler
import io.mosaicboot.core.auth.controller.AuthController
import io.mosaicboot.core.auth.service.AuthTokenService
import io.mosaicboot.core.auth.service.AuthenticationService
import io.mosaicboot.core.auth.service.CredentialService
import io.mosaicboot.core.repository.AuthenticationRepositoryBase
import io.mosaicboot.core.repository.TenantUserRepositoryBase
import io.mosaicboot.core.repository.UserRepositoryBase
import io.mosaicboot.core.user.auth.MosaicCredentialHandler
import io.mosaicboot.core.user.service.AuditService
import io.mosaicboot.core.user.service.MosaicOAuth2UserService
import io.mosaicboot.core.user.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MosaicAuthProperties::class)
@ConditionalOnProperty(prefix = "mosaic.auth", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@Import(
    MosaicOAuth2Config::class,
)
class MosaicAuthConfig(
    private val mosaicAuthProperties: MosaicAuthProperties,
) {
    @Bean
    fun mosaicSha256CredentialHandler(): MosaicSha256CredentialHandler {
        return MosaicSha256CredentialHandler()
    }


    @Bean
    fun authTokenService(
        objectMapper: ObjectMapper,
        userService: UserService,
    ): AuthTokenService {
        return AuthTokenService(
            mosaicAuthProperties = mosaicAuthProperties,
            objectMapper = objectMapper,
            userService = userService,
        )
    }

    @Bean
    fun credentialService(
        credentialHandlers: List<MosaicCredentialHandler>,
        objectMapper: ObjectMapper,
    ): CredentialService {
        return CredentialService(
            credentialHandlers = credentialHandlers,
            objectMapper = objectMapper,
        )
    }

    @Bean
    fun authenticationService(
        authenticationRepository: AuthenticationRepositoryBase<*>,
        userRepository: UserRepositoryBase<*>,
        tenantUserRepository: TenantUserRepositoryBase<*>,
        credentialService: CredentialService,
        auditService: AuditService,
        objectMapper: ObjectMapper,
    ): AuthenticationService {
        return AuthenticationService(
            authenticationRepository = authenticationRepository,
            userRepository = userRepository,
            tenantUserRepository = tenantUserRepository,
            credentialService = credentialService,
            auditService = auditService,
            objectMapper = objectMapper,
        )
    }

    @Bean
    fun mosaicAuthFilter(
        mosaicAuthProperties: MosaicAuthProperties,
        authTokenService: AuthTokenService,
        objectMapper: ObjectMapper,
    ): FilterRegistrationBean<MosaicCookieAuthFilter> {
        val registrationBean = FilterRegistrationBean<MosaicCookieAuthFilter>()
        registrationBean.filter = MosaicCookieAuthFilter(
            mosaicAuthProperties = mosaicAuthProperties,
            authTokenService = authTokenService,
            objectMapper = objectMapper,
        )
        registrationBean.order = SecurityProperties.DEFAULT_FILTER_ORDER - 1
        return registrationBean
    }

    @Bean
    @ConditionalOnProperty(prefix = "mosaic.auth.api", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun authController(
        authenticationService: AuthenticationService,
        authTokenService: AuthTokenService,
        mosaicAuthenticationHandler: MosaicAuthenticationHandler,
        @Autowired(required = false) mosaicOAuth2UserService: MosaicOAuth2UserService?,
    ): AuthController {
        return AuthController(
            authenticationService = authenticationService,
            authTokenService = authTokenService,
            mosaicAuthenticationHandler = mosaicAuthenticationHandler,
            mosaicOAuth2UserService = mosaicOAuth2UserService,
        )
    }

    @Bean
    fun mosaicAuthenticationHandler(
        mosaicAuthProperties: MosaicAuthProperties,
        mosaicCookieAuthFilter: FilterRegistrationBean<MosaicCookieAuthFilter>,
    ): MosaicAuthenticationHandler {
        return MosaicAuthenticationHandler(
            mosaicAuthProperties = mosaicAuthProperties,
            mosaicCookieAuthFilter = mosaicCookieAuthFilter.filter,
        )
    }

    @Configuration(proxyBeanMethods = true)
    class WebConfig(
        private val mosaicAuthProperties: MosaicAuthProperties,
        private val mosaicAuthenticationHandler: MosaicAuthenticationHandler,
    ) : WebMvcConfigurer {
        @Bean
        fun mosaicAuthSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
            http
                .securityMatcher("${mosaicAuthProperties.api.path}/**")
                .csrf { it.disable() }
                .authorizeHttpRequests { authorizeHttpRequests ->
                    authorizeHttpRequests.requestMatchers("${mosaicAuthProperties.api.path}/current").authenticated()
                }
                .logout { logout ->
                    logout.logoutUrl("${mosaicAuthProperties.api.path}/logout")
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