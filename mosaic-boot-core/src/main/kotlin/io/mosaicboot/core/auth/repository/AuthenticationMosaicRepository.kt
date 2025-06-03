package io.mosaicboot.core.auth.repository

import io.mosaicboot.core.auth.dto.AuthenticationDetail
import io.mosaicboot.core.auth.dto.AuthenticationInput
import io.mosaicboot.core.auth.entity.Authentication

interface AuthenticationMosaicRepository<T : Authentication> {
    fun save(input: AuthenticationInput): T
    fun findByUserIdAndMethod(userId: String, method: String): T?
    fun findByUserIdAndAuthenticationId(userId: String, id: String): AuthenticationDetail?
    fun findByMethodAndUsername(method: String, username: String): AuthenticationDetail?
    fun findByMethodAndEmail(method: String, email: String): AuthenticationDetail?
    fun softDelete(existing: Authentication): T
}