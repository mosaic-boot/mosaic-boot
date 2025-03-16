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

import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.auth.MosaicAuthenticationHandler
import io.mosaicboot.core.auth.dto.RegisterResult
import io.mosaicboot.core.auth.controller.dto.RegisterResponse
import io.mosaicboot.core.auth.service.AuthTokenService
import io.mosaicboot.core.auth.service.AuthenticationService
import io.mosaicboot.core.util.UnreachableException
import io.mosaicboot.core.util.WebClientInfo
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity

fun RegisterResult.toResponseEntity(): ResponseEntity<RegisterResponse> {
    return when (this) {
        is RegisterResult.Success -> ResponseEntity.ok(
            RegisterResponse.Success()
        )

        is RegisterResult.Failure -> ResponseEntity.badRequest().body(
            RegisterResponse.Failure(
                reason = this.reason,
            )
        )

        else -> throw UnreachableException()
    }
}

class AuthControllerHelper(
    private val authenticationService: AuthenticationService,
    private val authTokenService: AuthTokenService,
    private val mosaicAuthenticationHandler: MosaicAuthenticationHandler,
) {
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse,
        webClientInfo: WebClientInfo,
        authentication: MosaicAuthenticatedToken,
    ) {
        val result = authenticationService.refresh(
            userId = authentication.userId,
            authenticationId = authentication.authenticationId,
            webClientInfo = webClientInfo,
        )
        val newAuthenticationToken = authTokenService.issueAuthenticatedToken(
            webClientInfo,
            result.user,
            result.authentication,
            result.tenantUsers
        )
        newAuthenticationToken.activeTenantId = authentication.activeTenantId
        mosaicAuthenticationHandler.onAuthenticationSuccess(
            request,
            response,
            newAuthenticationToken
        )
    }
}