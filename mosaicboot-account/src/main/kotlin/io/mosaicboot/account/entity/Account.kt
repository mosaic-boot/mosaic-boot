package io.mosaicboot.account.entity

import java.util.*

interface Account {
    val tenantId: String
    /**
     * Primary Key
     */
    val accountId: String
    val deleted: Boolean
    val blocked: Boolean
    val name: String?
    val email: String?
    val registeredAt: Date?
    val deletedAt: Date?
}
