package io.mosaicboot.account.mongodb.authentication.jwt

import com.nimbusds.jose.jwk.JWK
import io.mosaicboot.account.authentication.jwt.JwtAuthenticationKeyRepository
import io.mosaicboot.account.mongodb.authentication.jwt.entity.JwkEntity
import io.mosaicboot.account.mongodb.authentication.jwt.repository.JwkRepository
import java.util.*

class MongoJwtAuthenticationKeyRepository(
    private val jwkRepository: JwkRepository,
) : JwtAuthenticationKeyRepository {
    override fun loadKey(keyId: String): JWK {
        val entity = jwkRepository.findById(keyId).orElseThrow()
        return JWK.parse(entity.jwk)
    }

    override fun storeKey(keyId: String, key: JWK) {
        jwkRepository.save(
            JwkEntity(
                id = key.keyID,
                createdAt = key.issueTime ?: Date(),
                jwk = key.toJSONObject(),
            )
        )
    }
}