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

import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.core.auth.config.MosaicAuthProperties
import io.mosaicboot.core.auth.oauth2.MosaicOAuth2RegisterToken
import io.mosaicboot.core.auth.service.AuthTokenService
import io.mosaicboot.core.user.controller.dto.ActiveTenantUser
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.filter.OncePerRequestFilter

class MosaicCookieAuthFilter(
    private val mosaicAuthProperties: MosaicAuthProperties,
    private val authTokenService: AuthTokenService,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    companion object {
        private val log = LoggerFactory.getLogger(MosaicCookieAuthFilter::class.java)
    }

    private val authRedirectUrl = mosaicAuthProperties.cookie.prefix + "auth-redirect-url"
    private val authTokenCookieName = mosaicAuthProperties.cookie.prefix + "auth-token"
    private val activeTenantCookieName = mosaicAuthProperties.cookie.prefix + "active-tenant-id"
    private val oauth2RegisterTokenCookieName = mosaicAuthProperties.cookie.prefix + "oauth2-register-token"
    private val oauth2AuthorizationRequestCookieName = mosaicAuthProperties.cookie.prefix + "oauth2-authorization-request"

    private var securityContextRepository: SecurityContextRepository =
        RequestAttributeSecurityContextRepository()

    private var securityContextHolderStrategy: SecurityContextHolderStrategy =
        SecurityContextHolder.getContextHolderStrategy()

    fun applyAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: MosaicAuthenticatedToken,
    ) {
        setCookie(request, response, authTokenCookieName, authentication.token)
        authentication.activeTenantId?.let { activeTenantId ->
            setCookie(request, response, activeTenantCookieName, activeTenantId)
        }
    }

    fun applyOAuth2RegisterToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
        token: MosaicOAuth2RegisterToken,
    ) {
        setCookie(request, response, oauth2RegisterTokenCookieName, token.token) { cookie ->
            cookie.maxAge = 3600
        }
    }

    fun applyOauth2AuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
        data: String?,
        redirectUrl: String?,
    ) {
        if (data != null) {
            setCookie(request, response, oauth2AuthorizationRequestCookieName, data) { cookie ->
                cookie.maxAge = 3600
            }
        } else {
            setCookie(request, response, oauth2AuthorizationRequestCookieName, null) { cookie ->
                cookie.maxAge = 0
            }
        }
        if (redirectUrl != null) {
            setCookie(request, response, authRedirectUrl, redirectUrl) { cookie ->
                cookie.maxAge = 3600
            }
        }
    }

    fun getOauth2AuthorizationRequest(
        request: HttpServletRequest,
    ): String? {
        return request.cookies?.find { it.name == oauth2AuthorizationRequestCookieName }?.value
    }

    fun getAuthRedirectUrl(
        request: HttpServletRequest,
    ): String? {
        return request.cookies?.find { it.name == authRedirectUrl }?.value
    }

    fun clearCookies(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        setCookie(request, response, authTokenCookieName, null) { cookie ->
            cookie.maxAge = 0
        }
        setCookie(request, response, activeTenantCookieName, null) { cookie ->
            cookie.maxAge = 0
        }
        setCookie(request, response, oauth2RegisterTokenCookieName, null) { cookie ->
            cookie.maxAge = 0
        }
    }

    private fun setCookie(
        request: HttpServletRequest,
        response: HttpServletResponse,
        name: String,
        value: String?,
        customizer: ((Cookie) -> Unit)? = null,
    ): Cookie {
        val cookie = Cookie(name, value)
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.secure = request.isSecure
        mosaicAuthProperties.cookie.expiration
            .takeIf { it > 0 }
            ?.let { cookie.maxAge = it }
        customizer?.invoke(cookie)
        response.addCookie(cookie)
        return cookie
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val activeTenantId = request.cookies?.find { it.name == activeTenantCookieName }?.value

            request.cookies?.find { it.name == authTokenCookieName }
                ?.let { cookie ->
                    runCatching {
                        authTokenService.verifyAuthenticatedToken(cookie.value, activeTenantId)
                    }.onFailure { ex ->
                        log.debug("auth token verification failed (token={})", cookie.value, ex)
                    }.getOrNull()
                }?.let { authentication ->
                    successfulAuthentication(request, response, authentication)
                    return
                }

            request.cookies?.find { it.name == oauth2RegisterTokenCookieName }
                ?.let { cookie ->
                    runCatching {
                        authTokenService.verifySocialRegisterTokenData(cookie.value)
                    }.onFailure { ex ->
                        log.debug("auth token verification failed (token={})", cookie.value, ex)
                    }.getOrNull()
                }?.let { authentication ->
                    successfulAuthentication(request, response, authentication)
                    return
                }
        } finally {
            filterChain.doFilter(request, response)
        }
    }

    private fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val context = this.securityContextHolderStrategy.createEmptyContext();
        context.authentication = authentication
        this.securityContextHolderStrategy.context = context;
        this.securityContextRepository.saveContext(context, request, response);
    }
}
