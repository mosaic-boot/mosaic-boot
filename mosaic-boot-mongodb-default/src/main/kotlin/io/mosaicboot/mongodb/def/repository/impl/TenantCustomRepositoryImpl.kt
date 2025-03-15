package io.mosaicboot.mongodb.def.repository.impl

import com.fasterxml.uuid.Generators
import io.mosaicboot.core.tenant.entity.Tenant
import io.mosaicboot.core.tenant.dto.TenantInput
import io.mosaicboot.mongodb.def.entity.TenantEntity
import io.mosaicboot.mongodb.def.repository.TenantCustomRepository
import org.springframework.data.mongodb.core.MongoTemplate
import java.time.Instant

class TenantCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : TenantCustomRepository {
    override fun save(tenant: TenantInput): Tenant {
        val now = Instant.now()
        return TenantEntity(
            id = Generators.timeBasedEpochGenerator().generate().toString(),
            createdAt = now,
            updatedAt = now,
            timeZone = tenant.timeZone,
            name = tenant.name,
            status = tenant.status,
        )
    }

    override fun saveEntity(tenant: Tenant): TenantEntity {
        return mongoTemplate.save(tenant as TenantEntity)
    }
}