package io.mosaicboot.account.authentication.jwt

import com.nimbusds.jose.jwk.JWK

interface JwtAuthenticationKeyRepository {
    fun loadKey(keyId: String): JWK
    fun storeKey(keyId: String, key: JWK)
}