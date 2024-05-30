package io.mosaicboot.account.oauth2.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository

class MosaicOAuth2AuthorizedClientRepository : OAuth2AuthorizedClientRepository {
    override fun <T : OAuth2AuthorizedClient> loadAuthorizedClient(
        clientRegistrationId: String,
        principal: Authentication,
        request: HttpServletRequest
    ): T {
        TODO("Not yet implemented")
    }

    override fun saveAuthorizedClient(
        authorizedClient: OAuth2AuthorizedClient,
        authentication: Authentication,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
//        val state = loginService.oauth2ParseState(request.getParameter("state"))
//        val principal = authentication.principal
//
//        try {
//            var appAuthentication: LoginService.AppAuthentication?
//
//            when (principal) {
//                is OidcUser -> {
//                    val identity = AuthenticationEntity.makeSnsIdentity(
//                        authorizedClient.clientRegistration.registrationId,
//                        principal.name,
//                    )
//                    appAuthentication = if (state.register) {
//                        registerService.registerBySns(
//                            tenantIdentity = state.tenant,
//                            authenticationIdentity = identity,
//                            name = principal.attributes["name"] as String? ?: "",
//                            email = principal.attributes["email"] as String? ?: "",
//                        )
//                    } else {
//                        loginService.loginBySns(
//                            state.tenant,
//                            identity,
//                        )
//                    }
//                }
//
//                else -> throw RuntimeException("invalid type: ${principal?.javaClass}")
//            }
//
//            val redirectUriBuilder = UriComponentsBuilder.fromUriString(request.requestURI)
//            if (appAuthentication == null) {
//                response.sendRedirect(
//                    redirectUriBuilder
//                        .replacePath("/signin")
//                        .build().toString()
//                )
//            } else {
//                cookieJwtAuthenticationFilter.setAuthentication(request, response, appAuthentication)
//
//                response.sendRedirect(
//                    redirectUriBuilder
//                        .replacePath("/")
//                        .build().toString()
//                )
//            }
//        } catch (e: LoginService.LoginException) {
//            val redirectUri = UriComponentsBuilder.fromUriString(request.requestURI)
//                .replacePath("/signin")
//                .queryParam("error", e.code)
//                .build()
//            response.sendRedirect(redirectUri.toString())
//        }
    }

    override fun removeAuthorizedClient(
        clientRegistrationId: String,
        principal: Authentication,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        TODO("Not yet implemented")
    }
}