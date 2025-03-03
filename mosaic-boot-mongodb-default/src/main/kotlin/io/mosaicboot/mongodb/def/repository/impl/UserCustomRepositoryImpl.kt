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
import io.mosaicboot.core.domain.vo.UserVo
import io.mosaicboot.mongodb.def.entity.UserEntity
import io.mosaicboot.mongodb.def.repository.UserCustomRepository
import org.springframework.data.mongodb.core.MongoTemplate
import java.time.Instant

class UserCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : UserCustomRepository {
    override fun save(input: UserVo): UserEntity {
        val now = Instant.now()
        return mongoTemplate.save(UserEntity(
            id = Generators.timeBasedEpochGenerator().generate().toString(),
            createdAt = now,
            updatedAt = now,
            timeZone = input.timeZone,
            name = input.name,
            email = input.email,
            status = input.status,
            roles = input.roles,
        ))
    }
}
