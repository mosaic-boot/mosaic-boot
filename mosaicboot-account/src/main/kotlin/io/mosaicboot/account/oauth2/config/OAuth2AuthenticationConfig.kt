package io.mosaicboot.account.oauth2.config

import io.mosaicboot.account.oauth2.service.MosaicOAuth2AuthorizedClientRepository
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.web.SecurityFilterChain

@EnableConfigurationProperties(MosaicOAuth2Properties::class)
class OAuth2AuthenticationConfig(
    private val mosaicOAuth2Properties: MosaicOAuth2Properties,
) {
    @Bean
    fun mosaicOAuth2AuthorizedClientRepository(): MosaicOAuth2AuthorizedClientRepository {
        return MosaicOAuth2AuthorizedClientRepository()
    }

//    @Bean
//    fun mosaicOAuth2AuthorizationRequestRedirectFilter(
//        clientRegistrationRepository: ClientRegistrationRepository,
//        mosaicOAuth2AuthorizedClientRepository: MosaicOAuth2AuthorizedClientRepository,
//    ): OAuth2AuthorizationRequestRedirectFilter {
//        return OAuth2AuthorizationRequestRedirectFilter(
//            clientRegistrationRepository,
//            mosaicOAuth2Properties.authorizationRequestBaseUri
//        )
//    }

    @Bean
    fun mosaicOauth2SecurityFilterChain(
        http: HttpSecurity,
//        oAuth2AuthorizationRequestRedirectFilter: OAuth2AuthorizationRequestRedirectFilter,
        mosaicOAuth2AuthorizedClientRepository: MosaicOAuth2AuthorizedClientRepository,
    ): SecurityFilterChain {
        http
            .oauth2Login { login ->
                login.failureUrl("/signin?error")
                login.redirectionEndpoint {
                    it.baseUri("/api/login/oauth2/callback/*")
                }
                login.authorizationEndpoint {
//                    it.authorizationRequestResolver(oAuth2AuthorizationRequestRedirectFilter.resol)
                    it.baseUri(mosaicOAuth2Properties.authorizationRequestBaseUri)
                }
                login.successHandler { request, response, authentication -> }
            }
            .oauth2Client { client ->
                client.authorizedClientRepository(mosaicOAuth2AuthorizedClientRepository)
            }
        return http.build()
    }
}