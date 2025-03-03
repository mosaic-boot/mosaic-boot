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

package io.mosaicboot.mongodb.def.entity

import io.mosaicboot.core.domain.user.UserAuditAction
import io.mosaicboot.core.domain.user.UserAuditLog
import io.mosaicboot.core.domain.user.UserAuditLogStatus
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "\${mosaic.datasource.mongodb.collections.user-audit-log.collection:userAuditLogs}")
class UserAuditLogEntity(
    @Id
    override val id: ObjectId,
    @Field("createdAt")
    override val createdAt: Instant,
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
) : UserAuditLog<ObjectId>