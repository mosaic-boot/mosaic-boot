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

import io.mosaicboot.core.auth.config.MosaicAuthProperties
import io.mosaicboot.core.user.enums.UserStatus
import io.mosaicboot.core.user.dto.UserInput
import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.core.auth.controller.dto.RegisterRequest
import io.mosaicboot.core.auth.controller.dto.RegisterResponse
import io.mosaicboot.core.auth.oauth2.MosaicOAuth2RegisterToken
import io.mosaicboot.core.auth.oauth2.OAuth2BasicInfo
import io.mosaicboot.core.user.controller.toResponseEntity
import io.mosaicboot.core.user.service.MosaicOAuth2UserService
import io.mosaicboot.core.util.WebClientInfo
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@MosaicController
class MosaicOAuth2Controller(
    private val mosaicOAuth2UserService: MosaicOAuth2UserService,
) : BaseMosaicController {
    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        val mosaicAuthProperties = applicationContext.getBean(MosaicAuthProperties::class.java)
        return mosaicAuthProperties.api.path.trimEnd('/') + "/oauth2/"
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
        return result.toResponseEntity()
    }
}