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

package io.mosaicboot.core.user.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.security.SecureRandom
import java.util.*

@ConfigurationProperties(prefix = "mosaic.user")
data class MosaicUserProperties(
    var enabled: Boolean = true,
    var api: Api = Api(),
    var cookie: Cookie = Cookie(),
    var jwt: Jwt = Jwt(),
    var jwe: Jwe = Jwe(),
    val oauth2: OAuth2 = OAuth2(),
) {
    data class Api(
        var enabled: Boolean = true,
        var path: String = "/api/user"
    )

    data class Cookie(
        var prefix: String = "",
        var path: String = "/",
        var domain: String = "",
        /**
         * seconds
         */
        var expiration: Int = -1,
    )

    data class Jwt(
        var algorithm: String = "HS256",
        var issuer: String = "",
        /**
         * raw 32byte secret or PEM Private Key (PKCS8)
         */
        var secret: String = generateRandomSecret(32),
        /**
         * when secret is encrypted pkcs8
         */
        var password: String = "",
        /**
         * seconds
         */
        var expiration: Int = 86400,
    )


    data class Jwe(
        var algorithm: String = "A256KW",
        var issuer: String = "",
        /**
         * raw 32byte secret or PEM Private Key (PKCS8)
         */
        var secret: String = generateRandomSecret(32),
        /**
         * when secret is encrypted pkcs8
         */
        var password: String = "",
        /**
         * seconds
         */
        var expiration: Int = 86400,
    )

    data class OAuth2(
        var registerUrl: String = "/register/oauth2",
        var successUrl: String = "/",
    )

    companion object {
        private fun generateRandomSecret(length: Int): String {
            val random = SecureRandom()
            val bytes = ByteArray(length)
            random.nextBytes(bytes)
            return Base64.getEncoder().encodeToString(bytes).substring(0, length)
        }
    }
}
