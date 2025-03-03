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
import com.nimbusds.jose.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.security.authentication.BadCredentialsException
import java.text.ParseException
import java.time.Instant
import java.util.*

class JwtHelper(
    private val algorithm: JWSAlgorithm,
    jwkSecret: JWK,
    private val objectMapper: ObjectMapper,
    private val expirationSeconds: Long,
): JwtCodec {
    private val keyId: String = jwkSecret.keyID
    private val signer: JWSSigner
    private val verifier: JWSVerifier

    init {
        when (algorithm) {
            JWSAlgorithm.HS256, JWSAlgorithm.HS384, JWSAlgorithm.HS512 -> {
                signer = MACSigner(jwkSecret.toOctetSequenceKey())
                verifier = MACVerifier(jwkSecret.toOctetSequenceKey())
            }
            JWSAlgorithm.ES256, JWSAlgorithm.ES384, JWSAlgorithm.ES512 -> {
                val ecKey = jwkSecret.toECKey()
                signer = ECDSASigner(ecKey.toECPrivateKey())
                verifier = ECDSAVerifier(ecKey.toECPublicKey())
            }
            else -> throw IllegalArgumentException("Unsupported algorithm: ${jwkSecret.algorithm}")
        }
    }

    fun <T> sign(
        builder: JWTClaimsSet.Builder,
        cty: String,
        claims: T,
    ): String {
        val now = Date.from(Instant.now())
        val expirationTime = Date.from(Instant.now().plusSeconds(expirationSeconds))

        val claimsBuilder = builder
            .issueTime(now)
            .expirationTime(expirationTime)

        objectMapper.convertValue(claims, object: TypeReference<Map<String, Any?>>(){})
            .forEach { (key, value) ->
                claimsBuilder.claim(key, value)
            }

        val jwsHeader = JWSHeader.Builder(algorithm)
            .keyID(keyId)
            .contentType(cty)
            .build()
        val signedJWT = SignedJWT(jwsHeader, claimsBuilder.build())

        try {
            signedJWT.sign(signer)
            return signedJWT.serialize()
        } catch (e: JOSEException) {
            throw RuntimeException("Failed to sign JWT", e)
        }
    }

    fun verify(token: String, cty: String): SignedJWT {
        try {
            val signedJWT = SignedJWT.parse(token)

            if (!signedJWT.verify(verifier)) {
                throw BadCredentialsException("Invalid JWT signature")
            }

            val claims = signedJWT.jwtClaimsSet
            val now = Date.from(Instant.now())

            if (claims.expirationTime.before(now)) {
                throw BadCredentialsException("Expired JWT token")
            }

            if (signedJWT.header.contentType != cty) {
                throw BadCredentialsException("Invalid Content Type")
            }

            return signedJWT
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

    override fun <T : Any> encode(
        builder: JWTClaimsSet.Builder,
        claims: T,
    ): String {
        val contentType = claims.javaClass.getAnnotation(JwtContentType::class.java)
        return sign(builder, contentType.value, claims)
    }

    override fun <T> decode(token: String, type: Class<T>): T {
        val contentType = type.getAnnotation(JwtContentType::class.java)
        val signedJwt = verify(token, contentType.value)
        return objectMapper.convertValue(signedJwt.jwtClaimsSet.toJSONObject(), type)
    }
}