package io.mosaicboot.core.auth.oauth2

import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.auth.enums.AuthMethod
import io.mosaicboot.core.auth.repository.AuthenticationRepositoryBase
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.web.util.UriComponentsBuilder

class MosaicOAuth2AuthorizationRequestResolver(
    private val authorizationRequestBaseUri: String,
    private val authenticationRepository: AuthenticationRepositoryBase<*>,
) : OAuth2AuthorizationRequestResolver {
    private lateinit var clientRegistrationRepository: ClientRegistrationRepository
    private lateinit var delegate: DefaultOAuth2AuthorizationRequestResolver

    fun setClientRegistrationRepository(clientRegistrationRepository: ClientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository
        this.delegate = DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository,
            authorizationRequestBaseUri
        )
    }

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return processAdditionalParameters(request, delegate.resolve(request))
    }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? {
        return processAdditionalParameters(request, delegate.resolve(request, clientRegistrationId))
    }

    private fun processAdditionalParameters(
        request: HttpServletRequest,
        authorizationRequest: OAuth2AuthorizationRequest?
    ): OAuth2AuthorizationRequest? {
        if (authorizationRequest == null) {
            return null
        }
        val registrationId = authorizationRequest.getAttribute<String>(OAuth2ParameterNames.REGISTRATION_ID)
        val authentication = SecurityContextHolder.getContext().authentication
        val authEntity = if (authentication is MosaicAuthenticatedToken) {
            authenticationRepository.findByUserIdAndMethod(
                userId = authentication.userId,
                method = "${AuthMethod.PREFIX_OAUTH2}:${registrationId}"
            )
        } else null

        val queryParams = HashMap<String, String>()
        queryParams["access_type"] = "offline"
        queryParams["prompt"] = "consent"
        authEntity?.let { queryParams["login_hint"] = it.username }

        val additionalParameters = HashMap(authorizationRequest.additionalParameters)
        additionalParameters.putAll(queryParams)
        val customAuthorizationRequestUri = UriComponentsBuilder
            .fromUriString(authorizationRequest.authorizationRequestUri)
            .also { builder ->
                queryParams.forEach { (k, v) ->
                    builder.queryParam(k, v)
                }
            }
            .build(true)
            .toUriString()
        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .additionalParameters(additionalParameters)
            .authorizationRequestUri(customAuthorizationRequestUri)
            .build()
    }
}