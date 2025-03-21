package io.mosaicboot.mongodb.def.repository.impl

import io.mosaicboot.core.user.entity.TenantUser
import io.mosaicboot.core.user.dto.CurrentActiveUser
import io.mosaicboot.core.user.dto.TenantUserInput
import io.mosaicboot.core.user.enums.UserStatus
import io.mosaicboot.mongodb.def.config.MongodbCollectionsProperties
import io.mosaicboot.mongodb.def.entity.TenantRoleEntity
import io.mosaicboot.mongodb.def.entity.TenantUserEntity
import io.mosaicboot.mongodb.def.entity.UserEntity
import io.mosaicboot.mongodb.def.repository.TenantUserCustomRepository
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class TenantUserCustomRepositoryImpl(
    private val mongodbCollectionsProperties: MongodbCollectionsProperties,
    private val mongoTemplate: MongoTemplate,
) : TenantUserCustomRepository {
    private val ROLE_LOOKUP = Aggregation.lookup()
        .from(mongodbCollectionsProperties.tenantRoles.collection)
        .localField("roleIds")
        .foreignField("_id")
        .`as`("roles")

    private val USER_LOOKUP = Aggregation.lookup()
        .from(mongodbCollectionsProperties.user.collection)
        .localField("userId")
        .foreignField("_id")
        .`as`("user")

    override fun findByTenantIdAndId(tenantId: String, id: String): TenantUser? {
        return mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(
                    Criteria("tenantId").isEqualTo(tenantId)
                        .and("_id").isEqualTo(id)
                ),
                ROLE_LOOKUP
            ),
            TenantUserEntity::class.java,
            TenantUserEntityWithRole::class.java,
        ).uniqueMappedResult
    }

    override fun save(input: TenantUserInput): TenantUser {
        val now = Instant.now()
        return mongoTemplate.save(
            TenantUserEntity(
                id = UUID.randomUUID().toString(),
                tenantId = input.tenantId,
                createdAt = now,
                updatedAt = now,
                userId = input.userId,
                nickname = input.nickname,
                email = input.email,
                status = input.status,
                roleIds = input.roles.map { it.id },
            )
        )
    }

    override fun findAllByTenantId(tenantId: String, pageable: Pageable): Page<out TenantUser> {
        return mongoTemplate.pagedAggregation(
            pageable,
            Aggregation.newAggregation(
                Aggregation.match(
                    Criteria("tenantId").isEqualTo(tenantId)
                ),
                ROLE_LOOKUP
            ),
            TenantUserEntity::class.java,
            PagedTenantUserEntityWithRole::class.java,
        )
    }

    override fun findAllByUserId(userId: String): List<TenantUser> {
        return mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(
                    Criteria("userId").isEqualTo(userId)
                ),
                ROLE_LOOKUP
            ),
            TenantUserEntity::class.java,
            TenantUserEntityWithRole::class.java,
        ).mappedResults
    }

    override fun findWithUser(
        userId: String,
        tenantId: String,
        tenantUserId: String,
    ): CurrentActiveUser? {
        return mongoTemplate.aggregate(
            Aggregation.newAggregation(
                Aggregation.match(
                    Criteria("tenantId").isEqualTo(tenantId)
                        .and("id").isEqualTo(tenantUserId)
                ),
                USER_LOOKUP,
                Aggregation.unwind("user"),
                ROLE_LOOKUP,
            ),
            TenantUserEntity::class.java,
            TenantUserEntityWithUser::class.java,
        ).uniqueMappedResult?.let {
            CurrentActiveUser(
                user = it.user,
                tenantUser = it,
            )
        }
    }

    open class TenantUserEntityWithRole(
        @Field("_id")
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
        @Field("roleIds")
        var roleIds: List<String>,
        @Field("roles")
        override var roles: List<TenantRoleEntity>,
    ) : TenantUser

    data class PagedTenantUserEntityWithRole(
        @Field("total")
        override val total: Long,
        @Field("items")
        override val items: List<TenantUserEntityWithRole>,
    ) : Paged<TenantUserEntityWithRole>

    open class TenantUserEntityWithUser(
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
        @Field("roleIds")
        var roleIds: List<String>,
        @Field("roles")
        override var roles: List<TenantRoleEntity>,
        @Field("user")
        val user: UserEntity,
    ) : TenantUser
}