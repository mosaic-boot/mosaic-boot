package io.mosaicboot.account.mongodb.authentication.jwt.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.Date

@Document(collection = JwkEntity.COLLECTION_NAME)
class JwkEntity(
    /**
     * KeyId
     */
    @Id
    var id: String,

    @Field("createdAt")
    var createdAt: Date,

    @Field("jwk")
    var jwk: Map<String, Any>,
) {
    companion object {
        const val COLLECTION_NAME = "jwks"
    }
}