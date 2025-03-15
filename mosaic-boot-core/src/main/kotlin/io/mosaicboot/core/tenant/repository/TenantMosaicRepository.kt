package io.mosaicboot.core.tenant.repository

import io.mosaicboot.core.tenant.dto.TenantInput

interface TenantMosaicRepository<T> {
    fun save(tenant: TenantInput): T
}