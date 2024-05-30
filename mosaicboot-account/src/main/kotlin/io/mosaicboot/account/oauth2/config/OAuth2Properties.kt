package io.mosaicboot.account.oauth2.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mosaic.account.oauth2")
data class OAuth2Properties(
    var authorizationRequestBaseUri: String,
    var redirectionEndpoint: String,
)
