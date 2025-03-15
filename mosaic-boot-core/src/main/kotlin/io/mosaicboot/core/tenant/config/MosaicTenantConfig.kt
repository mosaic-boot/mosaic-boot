package io.mosaicboot.core.tenant.config

import io.mosaicboot.core.tenant.repository.TenantRepositoryBase
import io.mosaicboot.core.user.repository.TenantUserRepositoryBase
import io.mosaicboot.core.tenant.controller.TenantsController
import io.mosaicboot.core.tenant.service.TenantService
import io.mosaicboot.core.user.service.UserService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MosaicTenantProperties::class)
class MosaicTenantConfig(
    private val mosaicTenantProperties: MosaicTenantProperties,
) {
    @Bean
    fun tenantService(
        userService: UserService,
        tenantRepository: TenantRepositoryBase<*>,
        tenantUserRepository: TenantUserRepositoryBase<*>,
    ): TenantService {
        return TenantService(
            userService = userService,
            tenantRepository = tenantRepository,
            tenantUserRepository = tenantUserRepository,
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "mosaic.tenant.api", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun tenantController(
        mosaicTenantProperties: MosaicTenantProperties,
        tenantService: TenantService,
    ): TenantsController {
        return TenantsController(
            mosaicTenantProperties = mosaicTenantProperties,
            tenantService = tenantService,
        )
    }
}