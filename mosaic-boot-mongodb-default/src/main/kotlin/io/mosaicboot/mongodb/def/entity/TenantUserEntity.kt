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

import io.mosaicboot.core.user.entity.TenantUser
import io.mosaicboot.core.user.enums.UserStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "\${mosaic.datasource.mongodb.collections.tenant-user.collection:tenantUsers}")
@CompoundIndexes(value = [
    CompoundIndex(def = "{'tenantId': 1, 'userId': 1}", unique = true)
])
data class TenantUserEntity(
    @Id
    override val id: String,
    @Field("tenantId")
    override val tenantId: String,
    @Field("createdAt")
    override val createdAt: Instant,
    @Field("userId")
    override val userId: String,
    @Field("updatedAt")
    override var updatedAt: Instant,
    @Field("nickname")
    override var nickname: String,
    @Field("email")
    override var email: String?,
    @Field("status")
    override var status: UserStatus,
    @Field("roles")
    var roles: List<String>,
) : TenantUser