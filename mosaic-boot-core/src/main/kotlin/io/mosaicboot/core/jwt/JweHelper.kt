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

package io.mosaicboot.core.jwt

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.*
import com.nimbusds.jose.crypto.AESDecrypter
import com.nimbusds.jose.crypto.AESEncrypter
import com.nimbusds.jose.crypto.DirectDecrypter
import com.nimbusds.jose.crypto.DirectEncrypter
import com.nimbusds.jose.crypto.ECDHDecrypter
import com.nimbusds.jose.crypto.ECDHEncrypter
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet
import io.mosaicboot.core.util.UnreachableException
import org.springframework.security.authentication.BadCredentialsException
import java.text.ParseException
import java.time.Instant
import java.util.*

class JweHelper(
    private val algorithm: JWEAlgorithm,
    jwkSecret: JWK,
    objectMapper: ObjectMapper,
    private val expirationSeconds: Long? = null,
) {
    private val objectMapper = objectMapper.copy()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    private val keyId: String = jwkSecret.keyID
    private val encrypter: JWEEncrypter
    private val decrypter: JWEDecrypter
    private val encryptionMethod: EncryptionMethod

    init {
        when (algorithm) {
            JWEAlgorithm.DIR -> {
                encrypter = DirectEncrypter(jwkSecret.toOctetSequenceKey())
                decrypter = DirectDecrypter(jwkSecret.toOctetSequenceKey())
                encryptionMethod = EncryptionMethod.A256GCM
            }
            JWEAlgorithm.A128KW, JWEAlgorithm.A192KW, JWEAlgorithm.A256KW,
            JWEAlgorithm.A128GCMKW, JWEAlgorithm.A192GCMKW, JWEAlgorithm.A256GCMKW -> {
                encrypter = AESEncrypter(jwkSecret.toOctetSequenceKey())
                decrypter = AESDecrypter(jwkSecret.toOctetSequenceKey())
                encryptionMethod = when (algorithm) {
                    JWEAlgorithm.A128KW -> EncryptionMethod.A128GCM
                    JWEAlgorithm.A192KW -> EncryptionMethod.A192GCM
                    JWEAlgorithm.A256KW -> EncryptionMethod.A256GCM
                    JWEAlgorithm.A128GCMKW -> EncryptionMethod.A128GCM
                    JWEAlgorithm.A192GCMKW -> EncryptionMethod.A192GCM
                    JWEAlgorithm.A256GCMKW -> EncryptionMethod.A256GCM
                    else -> throw UnreachableException()
                }
            }
            JWEAlgorithm.ECDH_ES, JWEAlgorithm.ECDH_ES_A128KW, JWEAlgorithm.ECDH_ES_A192KW, JWEAlgorithm.ECDH_ES_A256KW -> {
                encrypter = ECDHEncrypter(jwkSecret.toECKey())
                decrypter = ECDHDecrypter(jwkSecret.toECKey())
                encryptionMethod = when (algorithm) {
                    JWEAlgorithm.ECDH_ES -> EncryptionMethod.A256GCM
                    JWEAlgorithm.ECDH_ES_A128KW -> EncryptionMethod.A128GCM
                    JWEAlgorithm.ECDH_ES_A192KW -> EncryptionMethod.A192GCM
                    JWEAlgorithm.ECDH_ES_A256KW -> EncryptionMethod.A256GCM
                    else -> throw UnreachableException()
                }
            }
            else -> throw IllegalArgumentException("Unsupported algorithm: ${jwkSecret.algorithm}")
        }
    }

    fun <T> encrypt(
        providerName: String,
        builder: JWTClaimsSet.Builder,
        cty: String,
        claims: T,
    ): String {
        val now = Date.from(Instant.now())

        val claimsBuilder = builder
            .issueTime(now)
        expirationSeconds
            ?.let { Date.from(Instant.now().plusSeconds(it)) }
            ?.let {
                claimsBuilder.expirationTime(it)
            }
        objectMapper.convertValue(claims, object: TypeReference<Map<String, Any?>>(){})
            .forEach { (key, value) ->
                claimsBuilder.claim(key, value)
            }

        val jweHeader = JWEHeader.Builder(algorithm, encryptionMethod)
            .keyID("$providerName:$keyId")
            .contentType(cty)
            .compressionAlgorithm(CompressionAlgorithm.DEF)
            .build()
        val encryptedJWT = EncryptedJWT(jweHeader, claimsBuilder.build())

        try {
            encryptedJWT.encrypt(encrypter)
            return encryptedJWT.serialize()
        } catch (e: JOSEException) {
            throw RuntimeException("Failed to sign JWT", e)
        }
    }

    fun <T : Any> encode(
        providerName: String,
        builder: JWTClaimsSet.Builder,
        claims: T,
    ): String {
        val contentType = claims.javaClass.getAnnotation(JwtContentType::class.java)
        return encrypt(providerName, builder, contentType.value, claims)
    }

    fun decrypt(encryptedJWT: EncryptedJWT, cty: String) {
        try {
            encryptedJWT.decrypt(decrypter)

            val claims = encryptedJWT.jwtClaimsSet
            val now = Date.from(Instant.now())

            if (claims.expirationTime?.before(now) == true) {
                throw BadCredentialsException("Expired JWT token")
            }

            if (encryptedJWT.header.contentType != cty) {
                throw BadCredentialsException("Invalid Content Type")
            }
        } catch (e: ParseException) {
            throw BadCredentialsException(
                "Invalid JWT token",
                e
            )
        } catch (e: JOSEException) {
            throw BadCredentialsException(
                "Failed to verify JWT token",
                e
            )
        }
    }

    fun <T> decode(encryptedJWT: EncryptedJWT, type: Class<T>): T {
        val contentType = type.getAnnotation(JwtContentType::class.java)
        decrypt(encryptedJWT, contentType.value)
        return objectMapper.convertValue(encryptedJWT.jwtClaimsSet.toJSONObject(), type)
    }
}