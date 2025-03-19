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