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

import com.fasterxml.uuid.Generators
import io.mosaicboot.core.tenant.dto.TenantInput
import io.mosaicboot.mongodb.def.entity.TenantEntity
import io.mosaicboot.mongodb.def.repository.TenantCustomRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class TenantCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : TenantCustomRepository {
    override fun save(tenant: TenantInput): TenantEntity {
        val now = Instant.now()
        return mongoTemplate.save(
            TenantEntity(
                id = UUID.randomUUID().toString(),
                createdAt = now,
                updatedAt = now,
                timeZone = tenant.timeZone,
                name = tenant.name,
                status = tenant.status,
            )
        )
    }
}