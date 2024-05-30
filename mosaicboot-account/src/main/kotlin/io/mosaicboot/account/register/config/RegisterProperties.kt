package io.mosaicboot.account.register.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mosaicboot.account.register")
data class RegisterProperties(
    var enabled: Boolean = true,
    var apiBaseUrl: String = "/api/register"
)