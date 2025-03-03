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

package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.core.domain.user.Authentication
import io.mosaicboot.core.domain.vo.AuthenticationDetail
import io.mosaicboot.core.domain.vo.AuthenticationVo
import io.mosaicboot.mongodb.def.entity.AuthenticationEntity

interface AuthenticationCustomRepository {
    fun saveEntity(input: Authentication): AuthenticationEntity
    fun save(input: AuthenticationVo): AuthenticationEntity
    fun findByMethodAndEmail(method: String, email: String): AuthenticationDetail?
    fun findByMethodAndUsername(method: String, username: String): AuthenticationDetail?
    fun appendUserToAuthentication(authentication: Authentication, userId: String): AuthenticationEntity
    fun removeUserFromAuthentication(authentication: Authentication, userId: String): AuthenticationEntity
}
