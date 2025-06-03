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

package io.mosaicboot.core.tenant.config

import io.mosaicboot.core.auth.controller.AuthControllerHelper
import io.mosaicboot.core.tenant.repository.TenantRepositoryBase
import io.mosaicboot.core.user.repository.TenantUserRepositoryBase
import io.mosaicboot.core.tenant.controller.TenantsController
import io.mosaicboot.core.tenant.service.TenantService
import io.mosaicboot.core.tenant.service.TenantUserService
import io.mosaicboot.core.user.repository.TenantRoleRepositoryBase
import io.mosaicboot.core.user.service.AuditService
import io.mosaicboot.core.user.service.UserService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MosaicTenantProperties::class)
class MosaicTenantConfig {
    @Bean
    fun tenantService(
        mosaicTenantProperties: MosaicTenantProperties,
        userService: UserService,
        auditService: AuditService,
        tenantRepository: TenantRepositoryBase<*>,
        tenantUserRepository: TenantUserRepositoryBase<*>,
        tenantRoleRepository: TenantRoleRepositoryBase<*>,
    ): TenantService {
        return TenantService(
            mosaicTenantProperties = mosaicTenantProperties,
            userService = userService,
            auditService = auditService,
            tenantRepository = tenantRepository,
            tenantUserRepository = tenantUserRepository,
            tenantRoleRepository = tenantRoleRepository,
        )
    }

    @Bean
    fun tenantUserService(
        mosaicTenantProperties: MosaicTenantProperties,
        userService: UserService,
        auditService: AuditService,
        tenantRepository: TenantRepositoryBase<*>,
        tenantUserRepository: TenantUserRepositoryBase<*>,
        tenantRoleRepository: TenantRoleRepositoryBase<*>,
    ): TenantUserService {
        return TenantUserService(
            mosaicTenantProperties = mosaicTenantProperties,
            userService = userService,
            auditService = auditService,
            tenantRepository = tenantRepository,
            tenantUserRepository = tenantUserRepository,
            tenantRoleRepository = tenantRoleRepository,
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "mosaic.tenant.api", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun tenantController(
        mosaicTenantProperties: MosaicTenantProperties,
        tenantService: TenantService,
        tenantUserService: TenantUserService,
        authControllerHelper: AuthControllerHelper,
    ): TenantsController {
        return TenantsController(
            mosaicTenantProperties,
            tenantService,
            tenantUserService,
            authControllerHelper,
        )
    }
}