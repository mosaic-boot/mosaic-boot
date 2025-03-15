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

package io.mosaicboot.mongodb.def.repository.impl

import io.mosaicboot.core.user.entity.User
import io.mosaicboot.core.user.enums.UserAuditAction
import io.mosaicboot.core.user.enums.UserAuditLogStatus
import io.mosaicboot.core.user.dto.UserAuditLogDetail
import io.mosaicboot.core.user.dto.UserAuditLogInput
import io.mosaicboot.mongodb.def.entity.UserAuditLogEntity
import io.mosaicboot.mongodb.def.repository.UserAuditLogCustomRepository
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class UserAuditLogCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : UserAuditLogCustomRepository {
    override fun save(input: UserAuditLogInput): UserAuditLogEntity {
        return mongoTemplate.save(input.toEntity())
    }

    override fun saveAll(input: List<UserAuditLogInput>): List<UserAuditLogEntity> {
        return input.map { mongoTemplate.save(it.toEntity()) }
    }
//
//    override fun findByUserId(
//        tenantId: String,
//        userId: String,
//        searchInput: SearchInput,
//        pageable: Pageable
//    ): Page<UserAuditLogDetail<ObjectId>> {
//        TODO("Not yet implemented")
//    }
//
//    override fun findByTenantId(
//        tenantId: String,
//        searchInput: SearchInput,
//        pageable: Pageable
//    ): Page<UserAuditLogDetail<ObjectId>> {
//        TODO("Not yet implemented")
//    }

    class UserAuditLogDetailImpl(
        @Id
        override val id: ObjectId,
        @Field("createdAt")
        override val createdAt: Instant,
        @Field("user")
        override val user: User,
        @Field("tenantId")
        override val tenantId: String?,
        @Field("userId")
        override val userId: String?,
        @Field("performedBy")
        override val performedBy: String?,
        @Field("action")
        override val action: UserAuditAction,
        @Field("actionDetail")
        override val actionDetail: Map<String, Any?>?,
        @Field("status")
        override val status: UserAuditLogStatus,
        @Field("ipAddress")
        override val ipAddress: String,
        @Field("userAgent")
        override val userAgent: String,
        @Field("errorMessage")
        override val errorMessage: String?,
    ) : UserAuditLogDetail<ObjectId>

    private fun UserAuditLogInput.toEntity(): UserAuditLogEntity {
        return UserAuditLogEntity(
            id = ObjectId.get(),
            createdAt = Instant.now(),
            tenantId = tenantId,
            userId = userId,
            performedBy = performedBy,
            action = action,
            actionDetail = actionDetail,
            status = status,
            ipAddress = ipAddress,
            userAgent = userAgent,
            errorMessage = errorMessage,
        )
    }
}