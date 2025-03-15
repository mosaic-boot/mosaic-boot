package io.mosaicboot.core.user.service

import io.mosaicboot.core.user.repository.TenantUserRepositoryBase
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val tenantUserRepository: TenantUserRepositoryBase<*>,
) {
    fun hasTenantAccessByUser(
        userId: String,
        tenantId: String,
        permission: String?,
    ): Boolean {
        val tenantUser = tenantUserRepository.findByTenantIdAndUserId(tenantId, userId)
            ?: return false
        tenantUser.roles.find {}
    }
}