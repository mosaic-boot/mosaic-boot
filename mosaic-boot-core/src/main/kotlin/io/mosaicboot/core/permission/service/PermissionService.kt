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

package io.mosaicboot.core.permission.service

import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.tenant.service.TenantUserService
import io.mosaicboot.core.user.enums.UserStatus
import io.mosaicboot.core.user.repository.UserRepositoryBase
import org.springframework.stereotype.Service

@Service
class PermissionService(
    private val userRepositoryBase: UserRepositoryBase<*>,
    private val tenantUserService: TenantUserService,
) {
    fun checkPermission(
        authentication: MosaicAuthenticatedToken,
        permission: String,
        tenantId: String?,
    ): Boolean {
        val permissions = if (tenantId == null) {
            val user = userRepositoryBase.getUserById(authentication.userId)
            if (user.status != UserStatus.ACTIVE) {
                return false
            }
            user.roles.flatMap { it.permissions }
        } else {
            val tenantLogon = authentication.tenants[tenantId] ?: return false
            val tenantUser = tenantUserService.findTenantUser(
                tenantLogon.id,
                tenantLogon.userId,
            )!!
            if (tenantUser.status != UserStatus.ACTIVE) {
                return false
            }
            tenantUser.roles.flatMap { it.permissions }
        }
        return permission == "" || permissions.contains(permission)
    }
}