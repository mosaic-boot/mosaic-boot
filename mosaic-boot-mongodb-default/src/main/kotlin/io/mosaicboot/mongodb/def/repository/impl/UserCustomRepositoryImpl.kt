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

import io.mosaicboot.common.user.dto.UserInput
import io.mosaicboot.common.user.enums.UserStatus
import io.mosaicboot.data.entity.GlobalRole
import io.mosaicboot.mongodb.def.config.MongodbCollectionsProperties
import io.mosaicboot.mongodb.def.entity.UserEntity
import io.mosaicboot.data.entity.User
import io.mosaicboot.mongodb.def.repository.UserCustomRepository
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class UserCustomRepositoryImpl(
    private val mongodbCollectionsProperties: MongodbCollectionsProperties,
    private val mongoTemplate: MongoTemplate,
) : UserCustomRepository {
    override fun getUserById(id: String): User {
        return mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(Criteria("_id").isEqualTo(id)),
                Aggregation.lookup()
                    .from(mongodbCollectionsProperties.globalRoles.collection)
                    .localField("roleIds")
                    .foreignField("_id")
                    .`as`("roles")
            ),
            UserEntity::class.java,
            UserEntityWithRole::class.java,
        ).uniqueMappedResult!!
    }

    override fun save(input: UserInput): UserEntity {
        val now = Instant.now()
        return mongoTemplate.save(
            UserEntity(
                id = UUID.randomUUID().toString(),
                createdAt = now,
                updatedAt = now,
                timeZone = input.timeZone,
                name = input.name,
                email = input.email,
                status = input.status,
                roleIds = input.roles.map { it.id },
            )
        )
    }

    data class UserEntityWithRole(
        @Id
        override val id: String,
        @Field("createdAt")
        override val createdAt: Instant,
        @Field("updatedAt")
        override var updatedAt: Instant,
        @Field("name")
        override var name: String,
        @Field("email")
        @Indexed(unique = true)
        override var email: String,
        @Field("status")
        override var status: UserStatus,
        @Field("timeZone")
        override var timeZone: String,
        @Field("roleIds")
        var roleIds: List<String>,
        @Field("roles")
        override var roles: List<GlobalRole>,
    ) : User
}
