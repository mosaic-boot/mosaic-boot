package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.core.tenant.repository.TenantMosaicRepository
import io.mosaicboot.mongodb.def.entity.TenantEntity

interface TenantCustomRepository : TenantMosaicRepository<TenantEntity>