package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.core.user.entity.TenantUser
import io.mosaicboot.core.user.dto.CurrentActiveUser
import io.mosaicboot.core.user.dto.TenantUserDetail
import io.mosaicboot.core.user.dto.TenantUserInput

interface TenantUserCustomRepository {
    fun save(tenantUser: TenantUserInput): TenantUser
    fun findByTenantIdAndUserId(tenantId: String, userId: String): TenantUserDetail?
    fun findAllByUserId(userId: String): List<TenantUser>
    fun findCurrentActiveUserById(tenantUserId: String): CurrentActiveUser?
}