package io.mosaicboot.core.http

import io.mosaicboot.core.http.dto.SimpleErrorResponse
import io.mosaicboot.core.result.UserErrorMessageException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class MosaicErrorControllerAdvice {
    @ExceptionHandler(UserErrorMessageException::class)
    fun onUserErrorMessageException(ex: UserErrorMessageException): ResponseEntity<SimpleErrorResponse> {
        return ResponseEntity
            .status(ex.status)
            .body(SimpleErrorResponse(ex))
    }
}