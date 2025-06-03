/**
 * Copyright 2025 JC-Lab (mosaicboot.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mosaicboot.core.tenant.service

import io.mosaicboot.core.tenant.config.MosaicTenantProperties
import io.mosaicboot.core.tenant.entity.Tenant
import io.mosaicboot.core.tenant.enums.TenantStatus
import io.mosaicboot.core.user.entity.TenantUser
import io.mosaicboot.core.user.enums.UserStatus
import io.mosaicboot.core.user.dto.TenantUserInput
import io.mosaicboot.core.tenant.dto.TenantInput
import io.mosaicboot.core.tenant.repository.TenantRepositoryBase
import io.mosaicboot.core.user.dto.UserAuditLogInput
import io.mosaicboot.core.user.enums.UserAuditAction
import io.mosaicboot.core.user.enums.UserAuditLogStatus
import io.mosaicboot.core.user.repository.TenantRoleRepositoryBase
import io.mosaicboot.core.user.repository.TenantUserRepositoryBase
import io.mosaicboot.core.user.service.AuditService
import io.mosaicboot.core.user.service.UserService
import io.mosaicboot.core.util.WebClientInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TenantService(
    private val mosaicTenantProperties: MosaicTenantProperties,
    private val userService: UserService,
    private val auditService: AuditService,
    private val tenantRepository: TenantRepositoryBase<*>,
    private val tenantUserRepository: TenantUserRepositoryBase<*>,
    private val tenantRoleRepository: TenantRoleRepositoryBase<*>,
) {
    @Transactional
    fun createTenant(
        webClientInfo: WebClientInfo,
        userId: String,
        name: String,
        timeZone: String?,
    ): Pair<Tenant, TenantUser> {
        val user = userService.getUser(userId)

        val tenant = tenantRepository.save(
            TenantInput(
                name = name,
                timeZone = timeZone ?: user.timeZone,
                status = TenantStatus.ACTIVE
            )
        )

        // Add creator as admin
        val defaultRoles = tenantRoleRepository.findAllById(mosaicTenantProperties.adminRoles)
            .toList()
        if (mosaicTenantProperties.adminRoles.size != defaultRoles.size) {
            throw RuntimeException("could not find tenant admin roles")
        }

        val tenantUser = tenantUserRepository.save(
            TenantUserInput(
                tenantId = tenant.id,
                userId = user.id,
                nickname = user.name,
                email = user.email,
                status = UserStatus.ACTIVE,
                roles = defaultRoles,
            )
        )

        auditService.addLog(
            UserAuditLogInput(
                tenantId = tenant.id,
                userId = user.id,
                performedBy = user.id,
                action = UserAuditAction.TENANT_CREATED,
                status = UserAuditLogStatus.SUCCESS,
                ipAddress = webClientInfo.ipAddress,
                userAgent = webClientInfo.userAgent,
            )
        )

        return Pair(tenant, tenantUser)
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
