/**
 * Copyright 2025 JC-Lab (mosaicboot.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mosaicboot.core.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.core.domain.user.Authentication
import io.mosaicboot.core.domain.vo.AuthenticationDetail
import io.mosaicboot.core.repository.AuthenticationRepositoryBase
import io.mosaicboot.core.user.auth.MosaicCredentialHandler
import org.springframework.stereotype.Service

@Service
class CredentialService(
    credentialHandlers: List<MosaicCredentialHandler>,
    private val objectMapper: ObjectMapper,
) {
    private val namedCredentialHandlers = credentialHandlers
        .flatMap { handler ->
            handler.methods()?.map { method -> method to handler } ?: emptyList()
        }
        .toMap()

    private val anyCredentialHandlers = credentialHandlers.filter { it.methods() == null }

    fun isRegistrable(method: String): Boolean {
        val credentialHandler = namedCredentialHandlers[method]

        return when {
            credentialHandler != null -> credentialHandler.isRegistrable(method)
            else -> anyCredentialHandlers.firstNotNullOfOrNull { handler ->
                handler.isRegistrable(method)
            }
        } ?: false
    }

    fun encodeCredential(
        method: String,
        username: String,
        credential: String?,
    ): String {
        val credentialHandler = namedCredentialHandlers[method]

        return when {
            credentialHandler != null -> credentialHandler.encode(
                method,
                username,
                credential
            )!!
            else -> anyCredentialHandlers.firstNotNullOf { handler ->
                handler.encode(method, username, credential)
            }
        }
    }

    fun validateCredential(
        method: String,
        username: String,
        credential: String?,
        authDetail: Authentication
    ): Boolean {
        val credentialHandler = namedCredentialHandlers[authDetail.method]

        return when {
            credentialHandler != null -> credentialHandler.validate(
                method,
                username,
                credential,
                authDetail
            ) == true
            else -> anyCredentialHandlers.any { handler ->
                handler.validate(method, username, credential, authDetail) == true
            }
        }
    }
}