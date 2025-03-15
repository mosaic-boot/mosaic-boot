package io.mosaicboot.mongodb.def.repository.impl

import io.mosaicboot.core.user.entity.TenantUser
import io.mosaicboot.core.user.dto.CurrentActiveUser
import io.mosaicboot.core.user.dto.TenantUserInput
import io.mosaicboot.mongodb.def.config.MongodbCollectionsProperties
import io.mosaicboot.mongodb.def.dto.TenantUserDetailImpl
import io.mosaicboot.mongodb.def.entity.TenantUserEntity
import io.mosaicboot.mongodb.def.repository.TenantUserCustomRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import java.time.Instant
import java.util.UUID

class TenantUserCustomRepositoryImpl(
    private val mongodbCollectionsProperties: MongodbCollectionsProperties,
    private val mongoTemplate: MongoTemplate,
) : TenantUserCustomRepository {
    override fun save(tenantUser: TenantUserInput): TenantUser {
        val now = Instant.now()
        return mongoTemplate.save(
            TenantUserEntity(
                id = UUID.randomUUID().toString(),
                tenantId = tenantUser.tenantId,
                createdAt = now,
                updatedAt = now,
                userId = tenantUser.userId,
                nickname = tenantUser.nickname,
                email = tenantUser.email,
                status = tenantUser.status,
                roles = tenantUser.roles.map { it.id },
            )
        )
    }

    override fun findByTenantIdAndUserId(tenantId: String, userId: String): TenantUserDetailImpl? {
        val result = mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(
                    Criteria("tenantId").isEqualTo(tenantId)
                        .and("userId").isEqualTo(userId)
                ),
                Aggregation.lookup()
                    .from(mongodbCollectionsProperties.roles.collection)
                    .localField("role")
                    .foreignField("_id")
                    .`as`("roles"),
            ),
            TenantUserEntity::class.java,
            TenantUserDetailImpl::class.java,
        )
        return result.uniqueMappedResult
    }

    override fun findAllByUserId(userId: String): List<TenantUser> {
        TODO("Not yet implemented")
    }

    override fun findCurrentActiveUserById(tenantUserId: String): CurrentActiveUser? {
        TODO("Not yet implemented")
    }
}