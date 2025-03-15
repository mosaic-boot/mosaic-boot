package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.core.tenant.entity.Tenant
import io.mosaicboot.core.tenant.dto.TenantInput
import io.mosaicboot.mongodb.def.entity.TenantEntity

interface TenantCustomRepository {
    fun save(tenant: TenantInput): Tenant
    fun saveEntity(tenant: Tenant): TenantEntity
}