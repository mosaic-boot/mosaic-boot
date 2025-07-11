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

package io.mosaicboot.core.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jwt.JWTClaimsSet
import io.mosaicboot.core.auth.config.MosaicAuthProperties
import io.mosaicboot.core.auth.controller.dto.AuthTokenData
import io.mosaicboot.data.entity.Authentication
import io.mosaicboot.data.entity.TenantUser
import io.mosaicboot.data.entity.User
import io.mosaicboot.core.jwt.JweHelper
import io.mosaicboot.core.jwt.JwkHelper
import io.mosaicboot.core.jwt.JwtHelper
import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.auth.oauth2.MosaicOAuth2RegisterToken
import io.mosaicboot.core.auth.oauth2.OAuth2BasicInfo
import io.mosaicboot.core.auth.oauth2.OAuth2RegisterTokenData
import io.mosaicboot.core.encryption.ServerSideCrypto
import io.mosaicboot.core.permission.exception.PermissionDeniedException
import io.mosaicboot.core.user.controller.dto.ActiveTenantUser
import io.mosaicboot.core.user.controller.dto.OAuth2AccessTokenJson
import io.mosaicboot.core.user.controller.dto.OAuth2RefreshTokenJson
import io.mosaicboot.common.auth.dto.TenantLoginStatus
import io.mosaicboot.core.user.service.UserService
import io.mosaicboot.core.util.WebClientInfo
import org.springframework.stereotype.Service

@Service
class AuthTokenService(
    private val mosaicAuthProperties: MosaicAuthProperties,
    private val objectMapper: ObjectMapper,
    private val userService: UserService,
    private val serverSideCrypto: ServerSideCrypto,
) {
    private val jwtConfig = mosaicAuthProperties.jwt
    private val jwtTokenHelper = let {
        val algorithm = JWSAlgorithm.parse(jwtConfig.algorithm.uppercase())
        JwtHelper(
            algorithm = algorithm,
            jwkSecret = JwkHelper.loadSecret(algorithm, jwtConfig.secret),
            objectMapper = objectMapper,
            expirationSeconds = jwtConfig.expiration.toLong(),
        )
    }

    fun getIss(webClientInfo: WebClientInfo): String {
        return jwtConfig.issuer.takeIf { it.isNotEmpty() }
            ?: webClientInfo.host
    }

    fun issueAuthenticatedToken(
        webClientInfo: WebClientInfo,
        user: User,
        authentication: Authentication,
        tenantUsers: List<Pair<TenantUser, TenantLoginStatus>>,
    ): MosaicAuthenticatedToken {
        val claimsBuilder = JWTClaimsSet.Builder()
            .issuer(getIss(webClientInfo))
            .subject(user.id)
        val token = jwtTokenHelper.encode(
            claimsBuilder,
            AuthTokenData(
                userId = user.id,
                authId = authentication.id,
                tenants = tenantUsers.map {
                    AuthTokenData.TenantItem(
                        id = it.first.tenantId,
                        userId = it.first.id,
                        status = it.second,
                    )
                }
            ),
        )
        return MosaicAuthenticatedToken(
            token = token,
            userId = user.id,
            authenticationId = authentication.id,
            tenants = tenantUsers.associate {
                it.first.tenantId to AuthTokenData.TenantItem(
                    id = it.first.tenantId,
                    userId = it.first.id,
                    status = it.second,
                )
            },
            authorities = null,
            activeTenantId = tenantUsers.takeIf { it.size == 1 }
                ?.firstOrNull()?.first?.tenantId,
        )
    }

    fun verifyAuthenticatedToken(
        token: String,
        activeTenantId: String?,
    ): MosaicAuthenticatedToken {
        val data = jwtTokenHelper.decode(token, AuthTokenData::class.java)
        return MosaicAuthenticatedToken(
            token = token,
            userId = data.userId,
            authenticationId = data.authId,
            tenants = data.tenants.associateBy { it.id },
            authorities = null,
            activeTenantId = activeTenantId?.takeIf { tenantId ->
                data.tenants.find { it.id == tenantId } != null
            },
        )
    }

    fun issueSocialRegisterTokenData(
        webClientInfo: WebClientInfo,
        basicInfo: OAuth2BasicInfo,
        accessToken: OAuth2AccessTokenJson,
        refreshToken: OAuth2RefreshTokenJson?,
    ): MosaicOAuth2RegisterToken {
        val claimsBuilder = JWTClaimsSet.Builder()
            .issuer(getIss(webClientInfo))
        val data = OAuth2RegisterTokenData(
            provider = basicInfo.provider,
            id = basicInfo.id,
            name = basicInfo.name,
            email = basicInfo.email,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
        val token = serverSideCrypto.encrypt(
            data,
            claimsBuilder,
        )
        return MosaicOAuth2RegisterToken(
            token = token,
            data = data,
        )
    }

    fun verifySocialRegisterTokenData(token: String): MosaicOAuth2RegisterToken {
        val data = serverSideCrypto.decrypt(token, OAuth2RegisterTokenData::class.java)
        return MosaicOAuth2RegisterToken(token, data)
    }
}
