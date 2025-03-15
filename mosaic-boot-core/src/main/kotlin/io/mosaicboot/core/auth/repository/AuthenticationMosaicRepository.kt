package io.mosaicboot.core.auth.repository

import io.mosaicboot.core.auth.dto.AuthenticationDetail
import io.mosaicboot.core.auth.dto.AuthenticationInput

interface AuthenticationMosaicRepository<T> {
    fun save(input: AuthenticationInput): T
    fun findByMethodAndUsername(method: String, username: String): AuthenticationDetail?
    fun findByMethodAndEmail(method: String, email: String): AuthenticationDetail?
}