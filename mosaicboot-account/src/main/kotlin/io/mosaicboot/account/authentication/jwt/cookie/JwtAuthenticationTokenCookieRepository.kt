package io.mosaicboot.account.authentication.jwt.cookie

import io.mosaicboot.account.authentication.jwt.JwtAuthenticationTokenWebRepository
import io.mosaicboot.account.authentication.jwt.config.MosaicJwtAuthenticationProperties
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class JwtAuthenticationTokenCookieRepository(
    private val cookieSetting: MosaicJwtAuthenticationProperties.Cookie,
) : JwtAuthenticationTokenWebRepository {
    override fun loadToken(request: HttpServletRequest): String? {
        return request.cookies
            .find { it.name == cookieSetting.name && it.isHttpOnly }
            ?.value
    }

    override fun saveToken(request: HttpServletRequest, response: HttpServletResponse, token: String?) {
        val cookie = createCookie(token ?: "")
        if (token == null) {
            cookie.maxAge = 0
        } else if (cookieSetting.maxAge > 0) {
            cookie.maxAge = cookieSetting.maxAge
        }
        response.addCookie(cookie)
    }

    private fun createCookie(value: String): Cookie {
        val cookie = Cookie(cookieSetting.name, value)
        cookie.isHttpOnly = true
        cookieSetting.path.takeIf { it.isNotBlank() }?.let { cookie.path = it }
        cookieSetting.domain.takeIf { it.isNotBlank() }?.let { cookie.domain = it }
        return cookie
    }

}