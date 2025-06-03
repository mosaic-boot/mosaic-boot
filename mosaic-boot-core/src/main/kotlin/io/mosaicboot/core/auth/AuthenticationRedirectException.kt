package io.mosaicboot.core.auth

import org.springframework.security.core.AuthenticationException

class AuthenticationRedirectException : AuthenticationException {
    val redirectUri: String?

    constructor(
        message: String,
        redirectUri: String?,
    ) : super(message) {
        this.redirectUri = redirectUri
    }

    constructor(
        message: String,
        redirectUri: String?,
        cause: Throwable
    ) : super(message, cause) {
        this.redirectUri = redirectUri
    }
}