package io.mosaicboot.account.authentication.jwt.config

import io.mosaicboot.account.authentication.jwt.JwtAuthenticationKeyRepository
import io.mosaicboot.account.authentication.jwt.JwtAuthenticationTokenWebRepository
import io.mosaicboot.account.authentication.jwt.cookie.JwtAuthenticationTokenCookieRepository
import io.mosaicboot.account.authentication.jwt.filter.JwtAuthenticationAuthWebFilter
import io.mosaicboot.account.authentication.jwt.service.JwtAuthenticationService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@EnableConfigurationProperties(MosaicJwtAuthenticationProperties::class)
class MosaicJwtAuthenticationConfig(
    private val mosaicJwtAuthenticationProperties: MosaicJwtAuthenticationProperties,
) {
    @Bean
    fun jwtAuthenticationService(
        jwtAuthenticationKeyRepository: JwtAuthenticationKeyRepository,
    ): JwtAuthenticationService {
        return JwtAuthenticationService(
            mosaicJwtAuthenticationProperties,
            jwtAuthenticationKeyRepository
        )
    }

    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationTokenWebRepository::class)
    fun jwtAuthenticationTokenWebRepository(): JwtAuthenticationTokenWebRepository {
        return JwtAuthenticationTokenCookieRepository(mosaicJwtAuthenticationProperties.cookie)
    }

    @Bean
    fun jwtAuthenticationCookieAuthWebFilter(
        jwtAuthenticationService: JwtAuthenticationService,
        jwtAuthenticationTokenWebRepository: JwtAuthenticationTokenWebRepository,
    ): JwtAuthenticationAuthWebFilter {
        return JwtAuthenticationAuthWebFilter(
            jwtAuthenticationService,
            jwtAuthenticationTokenWebRepository,
        )
    }

    @Bean
    fun mosaicJwtAuthenticationSecurityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationAuthWebFilter: JwtAuthenticationAuthWebFilter,
    ): SecurityFilterChain {
        http
            .addFilterAfter(jwtAuthenticationAuthWebFilter, BasicAuthenticationFilter::class.java)
        return http.build()
    }
}