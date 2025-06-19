package io.mosaicboot.core.http.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.core.result.UserErrorMessageException

class SimpleErrorResponse(
    @JsonProperty("message")
    val message: String,
) {
    constructor(exception: UserErrorMessageException) : this(
        message = exception.message!!,
    )
}