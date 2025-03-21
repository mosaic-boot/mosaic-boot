package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.core.user.entity.TenantUser
import io.mosaicboot.core.user.repository.TenantUserMosaicRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TenantUserCustomRepository : TenantUserMosaicRepository {
    fun findAllByTenantId(tenantId: String, pageable: Pageable): Page<out TenantUser>
    fun findAllByUserId(userId: String): List<TenantUser>
}