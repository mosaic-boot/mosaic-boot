package io.mosaicboot.core.user.repository

import io.mosaicboot.core.user.dto.CurrentActiveUser
import io.mosaicboot.core.user.dto.TenantUserInput
import io.mosaicboot.core.user.entity.TenantUser

interface TenantUserMosaicRepository {
    fun save(input: TenantUserInput): TenantUser
    fun findByTenantIdAndId(tenantId: String, id: String): TenantUser?
    fun findAllByUserId(userId: String): List<TenantUser>
    fun findWithUser(
        userId: String,
        tenantId: String,
        tenantUserId: String,
    ): CurrentActiveUser?
}