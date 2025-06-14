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
import io.mosaicboot.common.tenant.dto.InviteResult
import io.mosaicboot.core.tenant.repository.TenantRepositoryBase
import io.mosaicboot.common.user.dto.TenantUserInput
import io.mosaicboot.data.entity.TenantRole
import io.mosaicboot.data.entity.TenantUser
import io.mosaicboot.common.user.enums.UserStatus
import io.mosaicboot.data.repository.TenantRoleRepositoryBase
import io.mosaicboot.data.repository.TenantUserRepositoryBase
import io.mosaicboot.core.user.service.AuditService
import io.mosaicboot.core.user.service.UserService
import io.mosaicboot.core.util.PagedResult
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class TenantUserService(
    private val mosaicTenantProperties: MosaicTenantProperties,
    private val userService: UserService,
    private val auditService: AuditService,
    private val tenantRepository: TenantRepositoryBase<*>,
    private val tenantUserRepository: TenantUserRepositoryBase<*>,
    private val tenantRoleRepository: TenantRoleRepositoryBase<*>,
) {
    fun getRoleById(id: String): TenantRole {
        return tenantRoleRepository.findById(id).get()
    }

    fun findTenantUser(
        tenantId: String,
        tenantUserId: String,
    ): TenantUser? {
        return tenantUserRepository.findByTenantIdAndId(tenantId, tenantUserId)
    }

    fun inviteUser(
        performedBy: TenantUser,
        email: String,
        roles: List<TenantRole>,
    ): InviteResult {
        val targetUser = userService.findUserByEmail(
            email
        ) ?: return InviteResult.UserNotExists

        performedBy.tenantId?.let { tenantId ->
            tenantUserRepository.findByTenantIdAndUserId(
                tenantId,
                targetUser.id,
            )?.let {
                return InviteResult.AlreadyExists(it)
            }

            val tenantUser = tenantUserRepository.save(
                TenantUserInput(
                    tenantId = tenantId,
                    userId = targetUser.id,
                    nickname = targetUser.name,
                    email = email,
                    status = UserStatus.ACTIVE,
                    roles = roles,
                )
            )
            return InviteResult.Success(tenantUser)
        } ?: return InviteResult.UserNotExists
    }

    fun getTenantUsers(
        tenantId: String,
        page: Int,
        size: Int
    ): PagedResult<TenantUser> {
        return tenantUserRepository.findAllByTenantId(
            tenantId,
            PageRequest.of(page, size)
        ).let {
            PagedResult(
                items = it.content,
                total = it.totalElements,
            )
        }
    }
}
