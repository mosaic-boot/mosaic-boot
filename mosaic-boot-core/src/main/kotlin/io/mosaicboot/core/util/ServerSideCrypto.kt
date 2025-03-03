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

package io.mosaicboot.core.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jwt.JWTClaimsSet
import io.mosaicboot.core.jwt.JweHelper
import io.mosaicboot.core.jwt.JwkHelper
import io.mosaicboot.core.user.config.MosaicUserProperties

class ServerSideCrypto(
    jweConfig: MosaicUserProperties.Jwe,
    objectMapper: ObjectMapper,
) {
    private val jweTokenHelper = let {
        val algorithm = JWEAlgorithm.parse(jweConfig.algorithm.uppercase())
        JweHelper(
            algorithm = algorithm,
            jwkSecret = JwkHelper.loadSecret(algorithm, jweConfig.secret),
            objectMapper = objectMapper,
            expirationSeconds = jweConfig.expiration.toLong(),
        )
    }

    fun <T : Any> encrypt(
        claims: T,
    ): String {
        return jweTokenHelper.encode(JWTClaimsSet.Builder(), claims)
    }

    fun <T> decrypt(token: String, type: Class<T>): T {
        return jweTokenHelper.decode(token, type)
    }
}