package io.mosaicboot.core.error

class UserErrorMessageException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}