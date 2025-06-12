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

import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet

class ServerSideCrypto(
    private val providers: List<ServerSideCryptoProvider>,
) {
    fun <T : Any> encrypt(
        claims: T,
        builder: JWTClaimsSet.Builder = JWTClaimsSet.Builder(),
    ): String {
        val provider = providers.find { it.support(claims.javaClass) }
            ?: throw IllegalStateException("No suitable provider found for ${claims.javaClass}")
        return provider.encrypt(builder, claims)
    }

    fun <T> decrypt(token: String, type: Class<T>): T {
        val encryptedJWT = EncryptedJWT.parse(token)
        val providerName = encryptedJWT.header.keyID.split(":", limit = 2).first()
        val provider = providers.find { it.name() == providerName }
            ?: throw IllegalStateException("No provider found for ${providerName}")
        return provider.decrypt(encryptedJWT, type)
    }
}