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

package io.mosaicboot.core.auth

import io.mosaicboot.core.auth.config.MosaicAuthProperties
import io.mosaicboot.core.auth.oauth2.AttributedOAuth2AuthenticationToken
import io.mosaicboot.core.auth.oauth2.AuthenticatedOAuth2User
import io.mosaicboot.core.auth.oauth2.NewLinkOAuth2User
import io.mosaicboot.core.auth.oauth2.TemporaryOAuth2User
import io.mosaicboot.core.util.UnreachableException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.web.util.UriComponentsBuilder

class MosaicAuthenticationHandler(
    private val mosaicAuthProperties: MosaicAuthProperties,
    private val mosaicCookieAuthFilter: MosaicCookieAuthFilter,
) :
    AuthenticationSuccessHandler,
    AuthenticationFailureHandler,
    LogoutHandler
{
    private val failureHandler = SimpleUrlAuthenticationFailureHandler()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val authRedirectUrl = mosaicCookieAuthFilter.getAuthRedirectUrl(request)
        when (authentication) {
            is MosaicAuthenticatedToken -> {
                mosaicCookieAuthFilter.applyAuthentication(
                    request,
                    response,
                    authentication,
                )
            }
            is AttributedOAuth2AuthenticationToken -> {
                val principal = authentication.principal
                when (principal) {
                    is NewLinkOAuth2User -> {
                        response.sendRedirect(
                            UriComponentsBuilder.fromUriString(authRedirectUrl)
                                .replaceQueryParam("status", "success")
                                .build().toString()
                        )
                    }
                    is TemporaryOAuth2User -> {
                        response.sendRedirect(mosaicAuthProperties.oauth2.registerUrl)
                    }
                    is AuthenticatedOAuth2User -> {
                        mosaicCookieAuthFilter.applyAuthentication(
                            request,
                            response,
                            principal.authenticatedToken,
                        )
                        response.sendRedirect(authRedirectUrl ?: mosaicAuthProperties.oauth2.successUrl)
                    }
                    else -> throw UnreachableException()
                }
            }
        }
    }

    override fun logout(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication?) {
        mosaicCookieAuthFilter.clearCookies(
            request,
            response,
        )
    }

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        if (exception is AuthenticationRedirectException && exception.redirectUri != null) {
            response.sendRedirect(exception.redirectUri)
            return
        }
        failureHandler.onAuthenticationFailure(request, response, exception)
    }
}