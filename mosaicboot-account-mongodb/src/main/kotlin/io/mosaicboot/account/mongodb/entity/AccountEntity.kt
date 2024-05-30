package io.mosaicboot.account.mongodb.entity

import io.mosaicboot.account.entity.Account
import org.springframework.data.mongodb.core.mapping.Document
import java.util.Date

@Document(collection = AccountEntity.COLLECTION_NAME)
class AccountEntity(
    override var tenantId: String,
    override var accountId: String,
    override var deleted: Boolean,
    override var blocked: Boolean,
    override var name: String,
    override var email: String,
    override var registeredAt: Date,
    override var deletedAt: Date? = null,
) : Account {
    companion object {
        const val COLLECTION_NAME = "accounts"
    }
}