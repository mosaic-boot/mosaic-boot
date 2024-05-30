package io.mosaicboot.account.mongodb.entity

import io.mosaicboot.account.constants.AuthenticationMethod
import io.mosaicboot.account.entity.AccountAuthentication
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = AccountEntity.COLLECTION_NAME)
class AccountAuthenticationEntity(
    override var tenantId: String,
    override var authenticationId: String,
    override var accountId: String,
    override var method: AuthenticationMethod,
    override var username: String,
    override var credential: String
) : AccountAuthentication {
    companion object {
        const val COLLECTION_NAME = "authentications"
    }
}