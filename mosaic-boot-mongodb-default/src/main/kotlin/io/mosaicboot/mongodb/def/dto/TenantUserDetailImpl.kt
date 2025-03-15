package io.mosaicboot.mongodb.def.dto

import io.mosaicboot.core.user.entity.SiteRole
import io.mosaicboot.core.user.enums.UserStatus
import io.mosaicboot.core.user.dto.TenantUserDetail
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

data class TenantUserDetailImpl(
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
    override var roles: Set<SiteRole>,
) : TenantUserDetail
