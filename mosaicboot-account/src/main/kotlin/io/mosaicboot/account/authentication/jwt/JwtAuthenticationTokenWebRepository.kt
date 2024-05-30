package io.mosaicboot.account.authentication.jwt

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface JwtAuthenticationTokenWebRepository {
    fun loadToken(request: HttpServletRequest): String?
    fun saveToken(request: HttpServletRequest, response: HttpServletResponse, token: String?)
}