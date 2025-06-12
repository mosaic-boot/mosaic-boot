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

package io.mosaicboot.core.encryption

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet
import io.mosaicboot.core.config.MosaicEncryptionProperties
import io.mosaicboot.core.jwt.JweHelper
import io.mosaicboot.core.jwt.JwkHelper

class JweServerSideCryptoProvider(
    jweConfig: MosaicEncryptionProperties.Jwe,
    objectMapper: ObjectMapper,
): ServerSideCryptoProvider {
    private val jweTokenHelper = let {
        val algorithm = JWEAlgorithm.parse(jweConfig.algorithm.uppercase())
        JweHelper(
            algorithm = algorithm,
            jwkSecret = JwkHelper.loadSecret(algorithm, jweConfig.secret),
            objectMapper = objectMapper,
            expirationSeconds = jweConfig.expiration.toLong(),
        )
    }

    override fun name(): String {
        return "mosaic-jwe"
    }

    override fun support(clazz: Class<*>): Boolean {
        return true
    }

    override fun <T : Any> encrypt(
        builder: JWTClaimsSet.Builder,
        claims: T,
    ): String {
        return jweTokenHelper.encode(name(), builder, claims)
    }

    override fun <T> decrypt(encryptedJWT: EncryptedJWT, type: Class<T>): T {
        return jweTokenHelper.decode(encryptedJWT, type)
    }
}