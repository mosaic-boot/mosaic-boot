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

package io.mosaicboot.mongodb.def.oauth2.repository

import io.mosaicboot.core.auth.oauth2.LockResult
import io.mosaicboot.core.auth.oauth2.OAuth2AccessTokenRepository
import io.mosaicboot.mongodb.def.oauth2.entity.OAuth2AccessTokenEntity
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class OAuth2AccessTokenRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : OAuth2AccessTokenRepository {
    companion object {
        private const val LOCK_DURATION_MILLISECONDS = 3000L
    }

    override fun tryLock(userId: String, authenticationId: String): LockResult {
        val now = Instant.now()
        val lockId = UUID.randomUUID().toString()
        val unlockAt = now.plusMillis(LOCK_DURATION_MILLISECONDS)

        val query = Query.query(
            Criteria.where("id").`is`(authenticationId)
                .and("userId").`is`(userId)
                .orOperator(
                    Criteria.where("lockId").isNull(),
                    Criteria.where("unlockAt").lt(now)
                )
        )

        val update = Update()
            .set("lockId", lockId)
            .set("unlockAt", unlockAt)

        val result = mongoTemplate.updateFirst(
            query,
            update,
            OAuth2AccessTokenEntity::class.java
        )

        return LockResult(
            success = result.modifiedCount > 0,
            lockId = lockId,
            unlockAt = unlockAt
        )
    }

    override fun update(userId: String, authenticationId: String, expiresAt: Instant?, data: String) {
        val now = Instant.now()

        val query = Query.query(
            Criteria.where("id").`is`(authenticationId)
                .and("userId").`is`(userId)
        )

        val update = Update()
            .set("data", data)
            .set("issuedAt", now)
            .set("expireAt", expiresAt)
            .set("lockId", null)
            .set("unlockAt", null)

        mongoTemplate.upsert(
            query,
            update,
            OAuth2AccessTokenEntity::class.java
        )
    }

    override fun read(userId: String, authenticationId: String): String? {
        val query = Query.query(
            Criteria.where("id").`is`(authenticationId)
                .and("userId").`is`(userId)
        )

        return mongoTemplate.findOne(query, OAuth2AccessTokenEntity::class.java)?.data
    }
}