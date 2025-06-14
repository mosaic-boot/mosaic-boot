package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.data.entity.TenantUser
import io.mosaicboot.data.repository.TenantUserMosaicRepository
import io.mosaicboot.mongodb.def.entity.TenantUserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TenantUserCustomRepository :
    TenantUserMosaicRepository<TenantUserEntity>
{
    fun findAllByTenantId(tenantId: String, pageable: Pageable): Page<out TenantUser>
    fun findAllByUserId(userId: String): List<out TenantUser>
}