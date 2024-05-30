package io.mosaicboot.account.authentication

import org.springframework.security.core.Authentication

interface AuthorizedAuthentication : Authentication {
    override fun isAuthenticated(): Boolean {
        return true
    }
}