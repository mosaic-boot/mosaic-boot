package io.mosaicboot.core.result

import org.springframework.http.HttpStatusCode

class UserErrorMessageException : Exception {
    val status: HttpStatusCode
    constructor(status: HttpStatusCode, message: String) : super(message) {
        this.status = status
    }
    constructor(status: HttpStatusCode, message: String, cause: Throwable) : super(message, cause) {
        this.status = status
    }
}