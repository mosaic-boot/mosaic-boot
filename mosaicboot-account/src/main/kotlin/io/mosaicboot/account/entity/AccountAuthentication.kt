package io.mosaicboot.account.entity

import io.mosaicboot.account.constants.AuthenticationMethod

interface AccountAuthentication {
    val tenantId: String

    /**
     * Primary Key
     */
    val authenticationId: String

    val accountId: String

    val method: AuthenticationMethod

    val username: String
    val credential: String
}