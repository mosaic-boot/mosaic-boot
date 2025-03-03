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

import io.mosaicboot.core.domain.SearchInput
import io.mosaicboot.core.domain.vo.UserAuditLogDetail
import io.mosaicboot.core.domain.vo.UserAuditLogVo
import io.mosaicboot.mongodb.def.entity.UserAuditLogEntity
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
interface UserAuditLogCustomRepository {
    fun save(input: UserAuditLogVo): UserAuditLogEntity
    fun saveAll(input: List<UserAuditLogVo>): List<UserAuditLogEntity>
    fun findByUserId(tenantId: String, userId: String, searchInput: SearchInput, pageable: Pageable): Page<UserAuditLogDetail<ObjectId>>
    fun findByTenantId(tenantId: String, searchInput: SearchInput, pageable: Pageable): Page<UserAuditLogDetail<ObjectId>>
}
