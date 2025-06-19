package io.mosaicboot.core.result

class UserErrorMessageException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}