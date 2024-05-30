package io.mosaicboot.account.authentication.jwt.jose

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyType

class JwkHolder {
    val privateKey: JWK?
    val kid: String
    val publicKey: JWK
    val algorithm: JWSAlgorithm
    val signer: JWSSigner
    val verifier: JWSVerifier

    constructor(
        jwk: JWK,
        kid: String,
    ) {
        this.privateKey = jwk.takeIf { it.isPrivate }

        this.kid = kid
        this.publicKey = jwk.toPublicJWK()
        when (jwk.keyType) {
            KeyType.EC -> {
                signer = ECDSASigner(jwk.toECKey())
                verifier = ECDSAVerifier(publicKey.toECKey())
                algorithm = signer.supportedJWSAlgorithms().first()
            }
            KeyType.RSA -> {
                signer = RSASSASigner(jwk.toRSAKey())
                verifier = RSASSAVerifier(publicKey.toRSAKey())
                algorithm = signer.supportedJWSAlgorithms().first()
            }
            else -> {
                throw RuntimeException("not supported key")
            }
        }
    }

    fun newHeaderBuilder(): JWSHeader.Builder {
        return JWSHeader.Builder(algorithm)
            .keyID(kid)
    }
}