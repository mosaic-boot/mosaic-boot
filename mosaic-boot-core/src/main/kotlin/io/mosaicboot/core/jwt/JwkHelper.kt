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

import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.OctetSequenceKey
import io.mosaicboot.core.util.BCHelper
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
import java.io.StringReader
import java.security.KeyPair
import java.security.PrivateKey
import java.security.interfaces.ECPublicKey

object JwkHelper {
    fun loadSecret(algorithm: JWSAlgorithm, input: String, password: String? = null): JWK {
        tryLoadJson(input)?.let { return it }

        when (algorithm) {
            JWSAlgorithm.HS256, JWSAlgorithm.HS384, JWSAlgorithm.HS512 -> {
                return OctetSequenceKey.Builder(input.toByteArray(Charsets.UTF_8))
                    .keyIDFromThumbprint()
                    .build()
            }

            JWSAlgorithm.ES256, JWSAlgorithm.ES384, JWSAlgorithm.ES512 -> {
                val keyPair = loadECKeyPairFromPem(input, password)
                return ECKey.Builder(Curve.forJWSAlgorithm(algorithm).first(), keyPair.public as ECPublicKey)
                    .keyIDFromThumbprint()
                    .privateKey(keyPair.private)
                    .build()
            }

            else -> throw IllegalArgumentException("Unsupported algorithm: ${algorithm}")
        }
    }

    fun loadSecret(algorithm: JWEAlgorithm, input: String, password: String? = null): JWK {
        tryLoadJson(input)?.let { return it }
        val secret = input.trim()

        when (algorithm) {
            JWEAlgorithm.DIR,
            JWEAlgorithm.A128KW,
            JWEAlgorithm.A192KW,
            JWEAlgorithm.A256KW,
            JWEAlgorithm.A128GCMKW,
            JWEAlgorithm.A192GCMKW,
            JWEAlgorithm.A256GCMKW -> {
                return OctetSequenceKey.Builder(input.toByteArray(Charsets.UTF_8))
                    .keyIDFromThumbprint()
                    .build()
            }

            JWEAlgorithm.ECDH_ES,
            JWEAlgorithm.ECDH_ES_A128KW,
            JWEAlgorithm.ECDH_ES_A192KW,
            JWEAlgorithm.ECDH_ES_A256KW -> {
                val keyPair = loadECKeyPairFromPem(input, password)
                return ECKey.Builder(Curve.forECParameterSpec((keyPair.private as java.security.interfaces.ECPrivateKey).params), keyPair.public as ECPublicKey)
                    .keyIDFromThumbprint()
                    .privateKey(keyPair.private)
                    .build()
            }

            else -> throw IllegalArgumentException("Unsupported algorithm: ${algorithm}")
        }
    }

    fun tryLoadJson(input: String): JWK? {
        val secret = input.trim()
        if (secret.startsWith("{")) {
            // JWK
            val jwkSecret = JWK.parse(secret)
            if (!jwkSecret.isPrivate) {
                throw IllegalArgumentException("Require private key")
            }
            return jwkSecret
        }
        return null
    }

    fun loadECKeyPairFromPem(pemContent: String, password: String?): KeyPair {
        try {
            val privateKey = loadPrivateKeyFromPem(pemContent, password)

            // Generate public key from private key
            val ecPrivateKey = privateKey as BCECPrivateKey
            val ecPublicKey = BCHelper.convertECPrivateToPublic(ecPrivateKey)
            return KeyPair(ecPublicKey, privateKey)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to load EC key pair from PEM", e)
        }
    }

    fun loadPrivateKeyFromPem(pemContent: String, password: String?): PrivateKey {
        val reader = PEMParser(StringReader(pemContent))
        val converter = JcaPEMKeyConverter().setProvider(BouncyCastleProvider())

        return when (val obj = reader.readObject()) {
            is PrivateKeyInfo -> converter.getPrivateKey(obj)
            is PKCS8EncryptedPrivateKeyInfo -> {
                val decryptorBuilder = JceOpenSSLPKCS8DecryptorProviderBuilder()
                    .build(password?.toCharArray() ?: "".toCharArray()) // empty password as we assume unencrypted key
                val privateKeyInfo = obj.decryptPrivateKeyInfo(decryptorBuilder)
                converter.getPrivateKey(privateKeyInfo)
            }

            else -> throw IllegalArgumentException("Unsupported PEM content")
        }
    }

//    fun loadEdDSAKeyPairFromPem(pemContent: String): KeyPair {
//        try {
//            val privateKey = loadPrivateKeyFromPem(pemContent)
//
//            // For EdDSA, we can get the public key directly from the private key
//            val edPrivateKey = privateKey as EdECPrivateKey
//            val keyFactory = KeyFactory.getInstance("Ed25519", BouncyCastleProvider())
//            val publicKey = keyFactory.generatePublic(edPrivateKey.publicKeySpec)
//
//            return KeyPair(publicKey, privateKey)
//        } catch (e: Exception) {
//            throw IllegalArgumentException("Failed to load EdDSA key pair from PEM", e)
//        }
//    }
}