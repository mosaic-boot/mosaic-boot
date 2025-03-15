package io.mosaicboot.core.tenant.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.security.SecureRandom
import java.util.*

@ConfigurationProperties(prefix = "mosaic.tenant")
data class MosaicTenantProperties(
    var enabled: Boolean = true,
    var api: Api = Api(),
) {
    data class Api(
        var enabled: Boolean = true,
        var path: String = "/api/tenants"
    )
}
