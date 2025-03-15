package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.core.user.entity.TenantRole
import io.mosaicboot.core.user.repository.TenantRoleRepositoryBase
import io.mosaicboot.mongodb.def.entity.TenantRoleEntity
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
@ConditionalOnProperty(prefix = "mosaic.datasource.mongodb.collections.tenant-role", name = ["customized"], havingValue = "false", matchIfMissing = true)
interface TenantRoleRepository : MongoRepository<TenantRoleEntity, String>,
    TenantRoleRepositoryBase<TenantRoleEntity>