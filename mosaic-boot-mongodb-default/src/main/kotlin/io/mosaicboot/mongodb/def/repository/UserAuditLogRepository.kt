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

import io.mosaicboot.core.repository.UserAuditLogRepositoryBase
import io.mosaicboot.mongodb.def.entity.UserAuditLogEntity
import org.bson.types.ObjectId
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(prefix = "mosaic.datasource.mongodb.collections.user-audit-log", name = ["customized"], havingValue = "false", matchIfMissing = true)
interface UserAuditLogRepository : MongoRepository<UserAuditLogEntity, ObjectId>,
    UserAuditLogRepositoryBase<UserAuditLogEntity, ObjectId>,
    UserAuditLogCustomRepository
