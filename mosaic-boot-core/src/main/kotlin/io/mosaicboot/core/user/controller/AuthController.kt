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

package io.mosaicboot.core.user.controller

import io.mosaicboot.core.domain.user.AuthMethod
import io.mosaicboot.core.domain.user.UserStatus
import io.mosaicboot.core.domain.vo.UserVo
import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.core.util.WebClientInfo
import io.mosaicboot.core.user.auth.LoginResult
import io.mosaicboot.core.user.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.user.auth.MosaicAuthenticationHandler
import io.mosaicboot.core.user.auth.RegisterResult
import io.mosaicboot.core.user.config.MosaicUserProperties
import io.mosaicboot.core.user.model.*
import io.mosaicboot.core.user.oauth2.MosaicOAuth2RegisterToken
import io.mosaicboot.core.user.service.AuthTokenService
import io.mosaicboot.core.user.service.MosaicOAuth2UserService
import io.mosaicboot.core.user.service.UserService
import io.mosaicboot.core.util.UnreachableException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@MosaicController
class AuthController(
    private val userService: UserService,
    private val authTokenService: AuthTokenService,
    private val mosaicAuthenticationHandler: MosaicAuthenticationHandler,
    @Autowired(required = false) private val mosaicOAuth2UserService: MosaicOAuth2UserService?,
) : BaseMosaicController {
    @Operation(
        summary = "User login",
        description = "Authenticate user and return tenant access information"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Successful login",
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = LoginResponse.Success::class))
            ]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Login failed",
            content = [
                Content(mediaType = "application/json", schema = Schema(implementation = LoginResponse.Failure::class))
            ]
        )
    ])
    @PostMapping("/login")
    fun login(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @Parameter(hidden = true) webClientInfo: WebClientInfo,
        @RequestBody body: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val result = userService.login(
            method = body.method,
            username = body.username,
            credential = body.credential,
            webClientInfo = webClientInfo,
        )

        return when (result) {
            is LoginResult.Success -> {
                val authenticatedToken = authTokenService.issueAuthenticatedToken(
                    webClientInfo,
                    result.user,
                    result.authentication,
                    result.tenantUsers
                )
                mosaicAuthenticationHandler.onAuthenticationSuccess(
                    request,
                    response,
                    authenticatedToken
                )
                ResponseEntity.ok(
                    LoginResponse.Success(
                        tenants = result.tenantUsers.associate { it.first.tenantId to it.second },
                    )
                )
            }

            is LoginResult.Failure -> ResponseEntity.status(401).body(
                LoginResponse.Failure(
                    reason = result.reason,
                )
            )

            else -> throw UnreachableException()
        }
    }

    @Operation(
        summary = "User registration",
        description = "Register a new user with given credentials"
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
            description = "invalid parameter",
        )
    ])
    @PostMapping("/register")
    fun register(
        request: HttpServletRequest,
        @Parameter(hidden = true) webClientInfo: WebClientInfo,
        @RequestBody body: RegisterRequest.Plain,
    ): ResponseEntity<RegisterResponse> {
        if (!userService.isRegistrable(body.method)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val result = userService.register(
            userTemplate = UserVo(
                name = body.name,
                email = body.email,
                status = UserStatus.ACTIVE,
                roles = mutableSetOf(),
                timeZone = "Asia/Seoul",
            ),
            method = body.method,
            username = body.username,
            credential = body.credential,
            webClientInfo = webClientInfo,
        )
        return result.toResponseEntity()
    }

    @Operation(
        summary = "Get current user information",
        description = "Retrieve information about the currently authenticated user"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Current user information retrieved successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = CurrentUserResponse::class))]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        )
    ])
    @GetMapping("/current")
    fun getCurrent(
        authentication: Authentication,
    ): ResponseEntity<CurrentUserResponse> {
        if (authentication !is MosaicAuthenticatedToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        return ResponseEntity.ok(
            CurrentUserResponse(
                userId = authentication.userId,
                activeTenantId = authentication.activeTenantUser?.tenantId,
                activeTenantUserId = authentication.activeTenantUser?.tenantUserId,
                name = "",
                email = "",
                permissions = emptyList(),
            )
        )
    }

    @Operation(
        summary = "Get user tenants",
        description = "Retrieve list of tenants for the current user"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Tenant list retrieved successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = List::class))]
        )
    ])
    @GetMapping("/current/tenants")
    fun getTenants(): List<MyTenant> {
        throw RuntimeException("not impl")
    }

    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        val mosaicUserProperties = applicationContext.getBean(MosaicUserProperties::class.java)
        return mosaicUserProperties.api.path
    }
}
