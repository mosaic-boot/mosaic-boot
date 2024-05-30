package io.mosaicboot.account.authentication.jwt.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mosaic.account.jwt-authentication")
data class MosaicJwtAuthenticationProperties(
    var cookie: Cookie = Cookie(),
    var key: Key = Key(),
) {
    data class Cookie(
        var name: String = "auth_token",
        var domain: String = "",
        var maxAge: Int = 0,
        var path: String = "",
    )

    data class Key(
        var autoGenerate: Boolean = false,
        var keyFile: String = "",
    )
}
