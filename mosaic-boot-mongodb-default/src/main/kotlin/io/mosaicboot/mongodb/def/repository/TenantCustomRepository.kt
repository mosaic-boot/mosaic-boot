package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.data.repository.TenantMosaicRepository
import io.mosaicboot.mongodb.def.entity.TenantEntity

interface TenantCustomRepository :
    TenantMosaicRepository<TenantEntity>