package io.mosaicboot.account.authentication.jwt.filter

import io.mosaicboot.account.authentication.jwt.JwtAuthenticationToken
import io.mosaicboot.account.authentication.jwt.JwtAuthenticationTokenWebRepository
import io.mosaicboot.account.authentication.jwt.service.JwtAuthenticationService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationAuthWebFilter(
    private val jwtAuthenticationService: JwtAuthenticationService,
    private val jwtAuthenticationTokenWebRepository: JwtAuthenticationTokenWebRepository,
) : OncePerRequestFilter() {
    private val securityContextRepository: SecurityContextRepository = RequestAttributeSecurityContextRepository()

    private fun setToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authenticationToken: JwtAuthenticationToken,
    ) {
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authenticationToken
        securityContextRepository.saveContext(context, request, response)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = jwtAuthenticationTokenWebRepository.loadToken(request)
            ?.let { jwtAuthenticationService.validateToken(it) }
        if (token != null) {
            setToken(request, response, token)
        }

        filterChain.doFilter(request, response)
    }
}