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
import io.mosaicboot.core.auth.dto.AuthenticationDetail
import io.mosaicboot.core.auth.dto.AuthenticationInput
import io.mosaicboot.core.auth.entity.Authentication
import io.mosaicboot.core.auth.enums.AuthMethod
import io.mosaicboot.mongodb.def.config.MongodbCollectionsProperties
import io.mosaicboot.mongodb.def.entity.AuthenticationEntity
import io.mosaicboot.mongodb.def.entity.UserEntity
import io.mosaicboot.mongodb.def.repository.AuthenticationCustomRepository
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class AuthenticationCustomRepositoryImpl(
    private val mongodbCollectionsProperties: MongodbCollectionsProperties,
    private val mongoTemplate: MongoTemplate,
) : AuthenticationCustomRepository {
    private val USER_LOOKUP = LookupOperation.newLookup()
        .from(mongodbCollectionsProperties.user.collection)
        .localField("userId")
        .foreignField("_id")
        .`as`("user")

    override fun save(input: AuthenticationInput): AuthenticationEntity {
        val now = Instant.now()
        return mongoTemplate.save(AuthenticationEntity(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
            userId = input.userId,
            method = input.method,
            username = input.username,
            credential = input.credential
        ))
    }

    override fun findByUserIdAndMethod(userId: String, method: String): AuthenticationEntity? {
        return mongoTemplate.findOne(
            Query.query(
                Criteria.where("userId").isEqualTo(userId)
                    .and("method").isEqualTo(method)
                    .and("deleted").ne(true)
            ).with(Sort.by(Sort.Direction.DESC, "createdAt")).limit(1),
            AuthenticationEntity::class.java,
        )
    }

    override fun findByMethodAndEmail(method: String, email: String): AuthenticationDetail? {
        val result = mongoTemplate.aggregate(
            Aggregation.newAggregation(
                // 1. 먼저 UserEntity 컬렉션에서 시작
                Aggregation.match(
                    Criteria.where("email").`is`(email)
                ),
                // 2. Authentication 컬렉션과 조인
                LookupOperation.newLookup()
                    .from(mongodbCollectionsProperties.authentication.collection)
                    .localField("_id")
                    .foreignField("userId")
                    .`as`("authentication"),
                // 3. authentication 배열을 풀어서 도큐먼트로 변환
                Aggregation.unwind("authentication"),
                // 4. method 조건 확인
                Aggregation.match(
                    Criteria.where("authentication.method").`is`(method)
                ),
                // 5. 결과 형식 재구성
                Aggregation.project()
                    .and("authentication._id").`as`("id")
                    .and("authentication.createdAt").`as`("createdAt")
                    .and("authentication.updatedAt").`as`("updatedAt")
                    .and("authentication.userId").`as`("userId")
                    .and("authentication.method").`as`("method")
                    .and("authentication.username").`as`("username")
                    .and("authentication.credential").`as`("credential")
                    .and("\$\$ROOT").`as`("user")
            ),
            UserEntity::class.java,
            AuthenticationDetailImpl::class.java
        )
        return result.uniqueMappedResult
    }

    override fun findByUserIdAndAuthenticationId(userId: String, id: String): AuthenticationDetail? {
        val result = mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(
                    Criteria.where("userId").`is`(userId)
                        .and("_id").`is`(id)
                ),
                USER_LOOKUP,
                Aggregation.unwind("user"),
            ),
            AuthenticationEntity::class.java,
            AuthenticationDetailImpl::class.java
        )
        return result.uniqueMappedResult
    }

    override fun findByMethodAndUsername(method: String, username: String): AuthenticationDetail? {
        val result = mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(
                    Criteria.where("method").`is`(method)
                        .and("username").`is`(username)
                ),
                USER_LOOKUP,
                Aggregation.unwind("user"),
            ),
            AuthenticationEntity::class.java,
            AuthenticationDetailImpl::class.java
        )
        return result.uniqueMappedResult
    }

    override fun softDelete(existing: Authentication): AuthenticationEntity {
        val now = Instant.now()
        return mongoTemplate.findAndModify(
            Query.query(Criteria.where("_id").`is`(existing.id)),
            Update()
                .set("updatedAt", now)
                .set("username", "${AuthMethod.PREFIX_DELETED}:${now.epochSecond}:${existing.username}")
                .set("deleted", true)
                .set("deletedAt", now),
            FindAndModifyOptions()
                .returnNew(true),
            AuthenticationEntity::class.java,
        )!!
    }

//    override fun appendUserToAuthentication(authentication: Authentication, userId: String): AuthenticationEntity {
//        TODO()
//        val query = Query().addCriteria(Criteria.where("id").`is`(authentication.id))
//        val update = Update()
//            .set("updatedAt", Instant.now())
//            .addToSet("userId", userId)
//
//        return mongoTemplate.findAndModify(
//            query,
//            update,
//            AuthenticationEntity::class.java
//        ) ?: throw IllegalStateException("Authentication not found with id: ${authentication.id}")
//    }
//
//    override fun removeUserFromAuthentication(authentication: Authentication, userId: String): AuthenticationEntity {
//        TODO()
//        val query = Query().addCriteria(Criteria.where("id").`is`(authentication.id))
//        val update = Update()
//            .set("updatedAt", Instant.now())
//            .pull("userId", userId)
//
//        return mongoTemplate.findAndModify(
//            query,
//            update,
//            AuthenticationEntity::class.java
//        ) ?: throw IllegalStateException("Authentication not found with id: ${authentication.id}")
//    }

    data class AuthenticationDetailImpl(
        @Id
        override val id: String,
        @Field("createdAt")
        override val createdAt: Instant,
        @Field("updatedAt")
        override var updatedAt: Instant,
        @Field("userId")
        override var userId: String,
        @Field("method")
        override val method: String,
        @Field("username")
        override val username: String,
        @Field("credential")
        override var credential: String?,
        @Field("deleted")
        override var deleted: Boolean = false,
        @Field("deletedAt")
        override var deletedAt: Instant? = null,
        @Field("user")
        override val user: UserEntity,
    ) : AuthenticationDetail
}