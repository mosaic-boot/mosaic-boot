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

package io.mosaicboot.core.auth.controller

import io.mosaicboot.core.auth.MosaicAuthenticationHandler
import io.mosaicboot.core.auth.config.MosaicAuthProperties
import io.mosaicboot.core.auth.controller.dto.OAuth2ProviderInfo
import io.mosaicboot.core.auth.controller.dto.RegisterRequest
import io.mosaicboot.core.auth.controller.dto.RegisterResponse
import io.mosaicboot.common.auth.dto.RegisterResult
import io.mosaicboot.core.auth.oauth2.MosaicOAuth2RegisterToken
import io.mosaicboot.core.auth.oauth2.OAuth2BasicInfo
import io.mosaicboot.core.auth.service.AuthTokenService
import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.common.user.dto.UserInput
import io.mosaicboot.common.user.enums.UserStatus
import io.mosaicboot.data.entity.Authentication
import io.mosaicboot.data.entity.TenantUser
import io.mosaicboot.core.user.service.MosaicOAuth2UserService
import io.mosaicboot.core.util.WebClientInfo
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@MosaicController
class MosaicOAuth2Controller(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val authorizationRequestBaseUri: String,
    private val mosaicOAuth2UserService: MosaicOAuth2UserService,
    private val authTokenService: AuthTokenService,
    private val mosaicAuthenticationHandler: MosaicAuthenticationHandler,
) : BaseMosaicController {
    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        val mosaicAuthProperties = applicationContext.getBean(MosaicAuthProperties::class.java)
        return mosaicAuthProperties.api.path.trimEnd('/') + "/oauth2/"
    }

    @Operation(
        summary = "Get available OAuth2 providers",
        description = "Retrieve list of configured OAuth2 providers"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "OAuth2 providers retrieved successfully",
            content = [
                Content(mediaType = "application/json", array = ArraySchema(
                    schema = Schema(implementation = OAuth2ProviderInfo::class)
                ))
            ]
        )
    ])
    @GetMapping("/providers")
    fun getOAuth2Providers(): ResponseEntity<List<OAuth2ProviderInfo>> {
        val providers = mutableListOf<OAuth2ProviderInfo>()

        if (clientRegistrationRepository is Iterable<*>) {
            clientRegistrationRepository.forEach { registration ->
                registration as ClientRegistration
                val displayName = when (registration.registrationId) {
                    "google" -> "Google"
                    "github" -> "GitHub"
                    "kakao" -> "Kakao"
                    "naver" -> "Naver"
                    else -> registration.registrationId.replaceFirstChar { it.uppercase() }
                }

                providers.add(
                    OAuth2ProviderInfo(
                        provider = registration.registrationId,
                        displayName = displayName,
                        availableForRegistration = true,
                        availableForLinking = true
                    )
                )
            }
        }
        
        return ResponseEntity.ok(providers)
    }

    @Operation(
        summary = "Get temporary oauth2 user information",
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Successful login",
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = OAuth2BasicInfo::class))
            ]
        ),
        ApiResponse(
            responseCode = "401",
            description = "No registration token",
        )
    ])
    @GetMapping("/register-info")
    fun getOAuth2RegisterInfo(): ResponseEntity<OAuth2BasicInfo> {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication !is MosaicOAuth2RegisterToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        return ResponseEntity.ok(
            OAuth2BasicInfo(
                provider = authentication.data.provider,
                id = authentication.data.id,
                name = authentication.data.name,
                email = authentication.data.email,
            )
        )
    }

    @Operation(
        summary = "User registration by OAuth2 Login",
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Successfully registered",
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = RegisterResponse.Success::class))
            ]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Registration failed",
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = RegisterResponse.Failure::class))
            ]
        ),
        ApiResponse(
            responseCode = "403",
            description = "No registration token",
        )
    ])
    @PostMapping("/register")
    fun registerByOAuth2(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @Parameter(hidden = true) webClientInfo: WebClientInfo,
        @RequestBody body: RegisterRequest.OAuth2,
    ): ResponseEntity<RegisterResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication !is MosaicOAuth2RegisterToken) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val result = mosaicOAuth2UserService.register(
            userTemplate = UserInput(
                name = body.name,
                email = body.email,
                status = UserStatus.ACTIVE,
                roles = mutableSetOf(),
                timeZone = "Asia/Seoul",
            ),
            webClientInfo = webClientInfo,
            data = authentication.data,
        )

        if (result is RegisterResult.Success) {
            val authenticatedToken = authTokenService.issueAuthenticatedToken(
                webClientInfo,
                result.user,
                result.authentication as Authentication,
                result.tenantUsers as List<Pair<TenantUser, io.mosaicboot.common.auth.dto.TenantLoginStatus>>
            )
            mosaicAuthenticationHandler.onAuthenticationSuccess(
                request,
                response,
                authenticatedToken
            )
        }

        return when (result) {
            is RegisterResult.Success -> ResponseEntity.ok(
                RegisterResponse.Success()
            )
            is RegisterResult.Failure -> ResponseEntity.badRequest().body(
                RegisterResponse.Failure(
                    reason = result.reason,
                )
            )
        }
    }

//    @GetMapping("/authorize")
//    fun oauth2Authorize(): String {
//        val authentication = SecurityContextHolder.getContext().authentication
//        if (authentication !is MosaicOAuth2RegisterToken) {
//            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
//        }
//
//        val clientRegistration: ClientRegistration = clientRegistrationRepository
//            .findByRegistrationId(authentication.getAuthorizedClientRegistrationId())
//
//
//        // 새로운 스코프 요청을 위한 OAuth2 엔드포인트 구성
//        val baseUrl = clientRegistration.providerDetails.authorizationUri
//        val clientId = clientRegistration.clientId
//        val redirectUri = clientRegistration.redirectUri
//        val additionalScopes = "additional_scope1 additional_scope2" // 필요한 추가 스코프
//
//        val authorizationUrl = UriComponentsBuilder.fromUriString(baseUrl)
//            .queryParam("client_id", clientId)
//            .queryParam("response_type", "code")
//            .queryParam("redirect_uri", redirectUri)
//            .queryParam("scope", additionalScopes)
//            .toUriString()
//
//        return "redirect:${authorizationRequestBaseUri}/"
//    }
}
