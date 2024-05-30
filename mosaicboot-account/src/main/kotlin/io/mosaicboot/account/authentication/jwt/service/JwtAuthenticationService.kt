package io.mosaicboot.account.authentication.jwt.service

import com.google.common.cache.CacheBuilder
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.SignedJWT
import io.mosaicboot.account.authentication.jwt.JwtAuthenticationKeyRepository
import io.mosaicboot.account.authentication.jwt.JwtAuthenticationToken
import io.mosaicboot.account.authentication.jwt.config.MosaicJwtAuthenticationProperties
import io.mosaicboot.account.authentication.jwt.jose.JwkHolder
import jakarta.annotation.PostConstruct
import java.util.*

class JwtAuthenticationService(
    private val properties: MosaicJwtAuthenticationProperties,
    private val jwtAuthenticationKeyRepository: JwtAuthenticationKeyRepository,
) {
    val cachedKey = CacheBuilder.newBuilder()
        .maximumSize(128)
        .build<String, JwkHolder>()

    @PostConstruct
    fun initialize() {
        if (properties.key.autoGenerate) {
            generateKey()
        }
    }

    fun generateKey() {
        val generatedKey = ECKeyGenerator(Curve.P_256).generate()
            .let {
                ECKey.Builder(it)
                    .issueTime(Date())
                    .keyID(it.computeThumbprint().toString())
                    .build()
            }
        jwtAuthenticationKeyRepository.storeKey(generatedKey.keyID, generatedKey)
    }

    fun validateToken(token: String): JwtAuthenticationToken? {
        val signedJwt = SignedJWT.parse(token)
        val keyHolder = getKey(token)
        val now = Date()
        if (!signedJwt.verify(keyHolder.verifier) || signedJwt.jwtClaimsSet.expirationTime.before(now)) {
            return null
        }
        return JwtAuthenticationToken(signedJwt)
    }

    private fun getKey(keyId: String): JwkHolder {
        return cachedKey.get(keyId) {
            val jwk = jwtAuthenticationKeyRepository.loadKey(keyId)
            JwkHolder(jwk, jwk.keyID)
        }
    }
}