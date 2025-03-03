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

package io.mosaicboot.core.user.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jwt.JWTClaimsSet
import io.mosaicboot.core.domain.user.Authentication
import io.mosaicboot.core.domain.user.TenantUser
import io.mosaicboot.core.domain.user.User
import io.mosaicboot.core.jwt.JweHelper
import io.mosaicboot.core.jwt.JwkHelper
import io.mosaicboot.core.jwt.JwtHelper
import io.mosaicboot.core.user.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.user.oauth2.MosaicOAuth2RegisterToken
import io.mosaicboot.core.user.config.MosaicUserProperties
import io.mosaicboot.core.user.model.*
import io.mosaicboot.core.user.oauth2.OAuth2BasicInfo
import io.mosaicboot.core.user.oauth2.OAuth2RegisterTokenData
import io.mosaicboot.core.util.WebClientInfo
import org.springframework.stereotype.Service

@Service
class AuthTokenService(
    private val mosaicUserProperties: MosaicUserProperties,
    private val objectMapper: ObjectMapper,
    private val userService: UserService,
) {
    private val jwtConfig = mosaicUserProperties.jwt
    private val jwtTokenHelper = let {
        val algorithm = JWSAlgorithm.parse(jwtConfig.algorithm.uppercase())
        JwtHelper(
            algorithm = algorithm,
            jwkSecret = JwkHelper.loadSecret(algorithm, jwtConfig.secret),
            objectMapper = objectMapper,
            expirationSeconds = jwtConfig.expiration.toLong(),
        )
    }
    private val jweConfig = mosaicUserProperties.jwe
    private val jweTokenHelper = let {
        val algorithm = JWEAlgorithm.parse(jweConfig.algorithm.uppercase())
        JweHelper(
            algorithm = algorithm,
            jwkSecret = JwkHelper.loadSecret(algorithm, jweConfig.secret),
            objectMapper = objectMapper,
            expirationSeconds = jweConfig.expiration.toLong(),
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
            authorities = null,
        )
    }

    fun verifyAuthenticatedToken(
        token: String,
        activeTenantUser: ActiveTenantUser?,
    ): MosaicAuthenticatedToken {
        val data = jwtTokenHelper.decode(token, AuthTokenData::class.java)
        return MosaicAuthenticatedToken(
            token = token,
            userId = data.userId,
            authenticationId = data.authId,
            authorities = null,
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
        val token = jweTokenHelper.encode(
            claimsBuilder,
            data
        )
        return MosaicOAuth2RegisterToken(
            token = token,
            data = data,
        )
    }

    fun verifySocialRegisterTokenData(token: String): MosaicOAuth2RegisterToken {
        val data = jweTokenHelper.decode(token, OAuth2RegisterTokenData::class.java)
        return MosaicOAuth2RegisterToken(token, data)
    }
}
