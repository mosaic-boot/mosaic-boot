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
import io.mosaicboot.core.auth.config.MosaicAuthProperties
import io.mosaicboot.core.auth.repository.AuthenticationRepositoryBase
import io.mosaicboot.core.user.controller.model.OAuth2AccessTokenJson
import io.mosaicboot.core.user.controller.model.OAuth2RefreshTokenJson
import io.mosaicboot.core.auth.oauth2.OAuth2AccessTokenRepository
import io.mosaicboot.core.util.ServerSideCrypto
import org.springframework.security.oauth2.client.ClientAuthorizationException
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest
import org.springframework.security.oauth2.client.endpoint.RestClientRefreshTokenTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Service
class MosaicOAuth2TokenService(
    private val mosaicAuthProperties: MosaicAuthProperties,
    private val objectMapper: ObjectMapper,
    private val authenticationRepository: AuthenticationRepositoryBase<*>,
    private val oAuth2AccessTokenRepository: OAuth2AccessTokenRepository,
    private val clientRegistrationRepository: ClientRegistrationRepository,
) {
    val serverSideCrypto = ServerSideCrypto(
        mosaicAuthProperties.jwe,
        objectMapper = objectMapper,
    )

    private val clock = Clock.systemUTC()
    private var accessTokenResponseClient = RestClientRefreshTokenTokenResponseClient()

    fun update(
        userId: String,
        authenticationId: String,
        authorizedClient: OAuth2AuthorizedClient,
    ) {
        var authentication = authenticationRepository.findById(authenticationId).get()
        if (authentication.userId != userId) {
            throw IllegalArgumentException("wrong userId")
        }

        val refreshToken = authorizedClient.refreshToken
        if (refreshToken != null) {
            authentication.credential = serverSideCrypto.encrypt(
                OAuth2RefreshTokenJson.copyFrom(refreshToken)
            )
            authentication.updatedAt = clock.instant()
            authentication = authenticationRepository.saveEntity(authentication);
        }

        updateAccessToken(userId, authenticationId, OAuth2AccessTokenJson.copyFrom(authorizedClient.accessToken))
    }

    fun updateAccessToken(
        userId: String,
        authenticationId: String,
        accessTokenJson: OAuth2AccessTokenJson,
    ) {
        oAuth2AccessTokenRepository.update(userId, authenticationId, accessTokenJson.expiresAt?.let { Instant.ofEpochSecond(it) }, serverSideCrypto.encrypt(accessTokenJson))
    }

    fun getAccessToken(userId: String, authenticationId: String, requireRemaining: Int = 180): OAuth2AccessTokenJson {
        var registration: ClientRegistration? = null

        // max retry 3 times
        for (i in 0 until 3) {
            val token = oAuth2AccessTokenRepository.read(userId, authenticationId)
                ?.let { serverSideCrypto.decrypt(it, OAuth2AccessTokenJson::class.java) }
            if (token != null && !isNeedRefresh(token.expiresAt)) {
                return token
            }
            val accessToken = token?.toToken() ?: OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "", null, null, emptySet(),
            )

            var authentication = authenticationRepository.findById(authenticationId).get()
            if (authentication.userId != userId) {
                throw IllegalArgumentException("wrong userId")
            }

            val refreshToken = serverSideCrypto.decrypt(
                authentication.credential!!,
                OAuth2RefreshTokenJson::class.java
            ).toToken()

            registration = registration ?: authentication.method.split(":")[0].let {
                clientRegistrationRepository.findByRegistrationId(it)
            }

            val lockResult = oAuth2AccessTokenRepository.tryLock(userId, authenticationId)
            if (lockResult.success) {
                val client = OAuth2AuthorizedClient(
                    registration,
                    authentication.username,
                    accessToken,
                    refreshToken
                )
                val response = getTokenResponse(
                    client,
                    OAuth2RefreshTokenGrantRequest(
                        registration, client.accessToken, client.refreshToken,
                    )
                )
                val accessTokenJson = OAuth2AccessTokenJson.copyFrom(response.accessToken)
                updateAccessToken(userId, authenticationId, accessTokenJson)
                response.refreshToken
                    ?.let { OAuth2RefreshTokenJson.copyFrom(it) }
                    ?.let { refreshTokenJson ->
                        authentication.credential = serverSideCrypto.encrypt(refreshTokenJson)
                        authentication = authenticationRepository.saveEntity(authentication)
                    }
                return accessTokenJson
            } else {
                while (true) {
                    val remainingToUnlock = Duration.between(clock.instant(), lockResult.unlockAt).toMillis()
                    if (remainingToUnlock > 0) {
                        Thread.sleep(remainingToUnlock.coerceAtMost(100))
                    }
                }
            }
        }

        throw RuntimeException("failure")
    }


    private fun getTokenResponse(
        authorizedClient: OAuth2AuthorizedClient,
        refreshTokenGrantRequest: OAuth2RefreshTokenGrantRequest
    ): OAuth2AccessTokenResponse {
        try {
            return this.accessTokenResponseClient.getTokenResponse(refreshTokenGrantRequest)
        } catch (ex: OAuth2AuthorizationException) {
            throw ClientAuthorizationException(
                ex.error,
                authorizedClient.clientRegistration.registrationId, ex
            )
        }
    }

    /**
     * @param expiresAt seconds epoch
     */
    fun isNeedRefresh(expiresAt: Long?): Boolean {
        if (expiresAt == null) {
            return false
        }
        val duration = expiresAt - clock.instant().epochSecond
        return (duration <= 300)
    }

    fun OAuth2AccessTokenJson.toToken(): OAuth2AccessToken {
        return OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            this.value,
            this.issuedAt?.let { Instant.ofEpochSecond(it) },
            this.expiresAt?.let { Instant.ofEpochSecond(it) },
            this.scopes
        )
    }

    fun OAuth2RefreshTokenJson.toToken(): OAuth2RefreshToken {
        return OAuth2RefreshToken(
            this.value,
            this.issuedAt?.let { Instant.ofEpochSecond(it) },
            this.expiresAt?.let { Instant.ofEpochSecond(it) }
        )
    }
}