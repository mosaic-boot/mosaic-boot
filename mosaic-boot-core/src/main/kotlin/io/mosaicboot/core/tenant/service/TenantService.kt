package io.mosaicboot.core.tenant.service

import io.mosaicboot.core.tenant.entity.Tenant
import io.mosaicboot.core.tenant.enums.TenantStatus
import io.mosaicboot.core.user.entity.TenantUser
import io.mosaicboot.core.user.enums.UserStatus
import io.mosaicboot.core.user.dto.TenantUserInput
import io.mosaicboot.core.tenant.dto.TenantInput
import io.mosaicboot.core.tenant.repository.TenantRepositoryBase
import io.mosaicboot.core.user.repository.TenantUserRepositoryBase
import io.mosaicboot.core.user.service.UserService
import org.springframework.stereotype.Service

@Service
class TenantService(
    private val userService: UserService,
    private val tenantRepository: TenantRepositoryBase<*>,
    private val tenantUserRepository: TenantUserRepositoryBase<*>,
) {
    fun createTenant(
        userId: String,
        name: String,
        timeZone: String?,
    ): Tenant {
        val user = userService.getUser(userId)

        val tenant = tenantRepository.save(
            TenantInput(
                name = name,
                timeZone = timeZone ?: user.timeZone,
                status = TenantStatus.ACTIVE
            )
        )

        // Add creator as admin
        tenantUserRepository.save(
            TenantUserInput(
                tenantId = tenant.id,
                userId = user.id,
                nickname = user.name,
                email = user.email,
                status = UserStatus.ACTIVE,
                roles = emptySet(),
            )
        )

        return tenant
    }

    fun getTenant(tenantId: String): Tenant {
        return tenantRepository.findById(tenantId).get()
    }

    fun update(tenant: Tenant): Tenant {
        return tenantRepository.saveEntity(tenant)
    }

    fun getTenants(tenantIds: Collection<String>): List<Tenant> {
        return tenantRepository.findAllById(tenantIds).toList()
    }
}
