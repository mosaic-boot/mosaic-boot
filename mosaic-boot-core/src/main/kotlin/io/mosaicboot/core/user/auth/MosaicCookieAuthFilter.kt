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

package io.mosaicboot.core.user.auth

import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.core.auth.config.MosaicAuthProperties
import io.mosaicboot.core.user.model.ActiveTenantUser
import io.mosaicboot.core.auth.oauth2.MosaicOAuth2RegisterToken
import io.mosaicboot.core.auth.service.AuthTokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class MosaicCookieAuthFilter(
    private val mosaicAuthProperties: MosaicAuthProperties,
    private val authTokenService: AuthTokenService,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    companion object {
        private val log = LoggerFactory.getLogger(MosaicCookieAuthFilter::class.java)
    }

    private val authTokenCookieName = mosaicAuthProperties.cookie.prefix + "auth-token"
    private val activeTenantCookieName = mosaicAuthProperties.cookie.prefix + "active-tenant-user"
    private val oauth2RegisterTokenCookieName = mosaicAuthProperties.cookie.prefix + "oauth2-register-token"

    fun applyAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: MosaicAuthenticatedToken,
    ) {
        setCookie(request, response, authTokenCookieName, authentication.token)
        authentication.activeTenantUser?.let { activeTenantUser ->
            setCookie(request, response, activeTenantCookieName, objectMapper.writeValueAsString(activeTenantUser))
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
        customizer: ((Cookie) -> Unit)? = null
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
        filterChain: FilterChain
    ) {
        try {
            val activeTenantUser = request.cookies?.find { it.name == activeTenantCookieName }
                ?.let { cookie -> objectMapper.readValue(cookie.value, ActiveTenantUser::class.java) }

            request.cookies?.find { it.name == authTokenCookieName }
                ?.let { cookie ->
                    runCatching {
                        authTokenService.verifyAuthenticatedToken(cookie.value, activeTenantUser)
                    }.onFailure { ex ->
                        log.debug("auth token verification failed (token={})", cookie.value, ex)
                    }.getOrNull()
                }?.let { authentication ->
                    SecurityContextHolder.getContext().authentication = authentication
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
                    SecurityContextHolder.getContext().authentication = authentication
                }
        } finally {
            filterChain.doFilter(request, response)
        }
    }
}
