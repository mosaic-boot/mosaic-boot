package io.mosaicboot.core.auth.oauth2

import io.mosaicboot.core.auth.MosaicCookieAuthFilter
import io.mosaicboot.core.user.service.MosaicOAuth2UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.util.SerializationUtils

class CookieAuthorizationRequestRepository(
    private val mosaicOAuth2UserService: MosaicOAuth2UserService,
    private val mosaicCookieAuthFilter: MosaicCookieAuthFilter,
) : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest {
        val data = mosaicCookieAuthFilter.getOauth2AuthorizationRequest(request)
        return decryptRequest(data!!)
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest {
        val data = mosaicCookieAuthFilter.getOauth2AuthorizationRequest(request)
        mosaicCookieAuthFilter.applyOauth2AuthorizationRequest(request, response, null, null)
        return decryptRequest(data!!)
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val data = mosaicOAuth2UserService.serverSideCrypto.encrypt(
            AuthorizationRequestJson(
                data = SerializationUtils.serialize(authorizationRequest)!!,
            )
        )
        mosaicCookieAuthFilter.applyOauth2AuthorizationRequest(
            request,
            response,
            data,
            request.getParameter("redirect_uri")
        )
    }

    @Suppress("DEPRECATION")
    private fun decryptRequest(data: String): OAuth2AuthorizationRequest {
        val requestJson = mosaicOAuth2UserService.serverSideCrypto.decrypt(
            data, AuthorizationRequestJson::class.java
        )
        return SerializationUtils.deserialize(requestJson.data) as OAuth2AuthorizationRequest
    }
}