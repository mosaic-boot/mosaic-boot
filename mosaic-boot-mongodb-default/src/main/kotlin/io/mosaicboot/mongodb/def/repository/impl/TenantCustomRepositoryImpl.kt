package io.mosaicboot.mongodb.def.repository.impl

import com.fasterxml.uuid.Generators
import io.mosaicboot.core.tenant.dto.TenantInput
import io.mosaicboot.mongodb.def.entity.TenantEntity
import io.mosaicboot.mongodb.def.repository.TenantCustomRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class TenantCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : TenantCustomRepository {
    override fun save(tenant: TenantInput): TenantEntity {
        val now = Instant.now()
        return mongoTemplate.save(
            TenantEntity(
                id = UUID.randomUUID().toString(),
                createdAt = now,
                updatedAt = now,
                timeZone = tenant.timeZone,
                name = tenant.name,
                status = tenant.status,
            )
        )
    }
}