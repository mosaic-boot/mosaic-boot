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

package io.mosaicboot.core.auth

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.KeyParameter
import java.security.SecureRandom
import java.util.*

/**
 * PBKDF2-SHA256 password encoder.
 */
class MosaicSha256PasswordEncoder {
    companion object {
        private const val DEFAULT_ROUNDS = 535000
        private const val SALT_LENGTH = 16
        private const val SHA256_DIGEST_LENGTH = 32
    }

    private val secureRandom = SecureRandom()
    private val b64Encoder = Base64.getUrlEncoder()
    private val b64Decoder = Base64.getUrlDecoder()

    fun encode(rawPassword: ByteArray, rounds: Int = DEFAULT_ROUNDS): String {
        val salt = ByteArray(SALT_LENGTH).apply { secureRandom.nextBytes(this) }
        val hash = hashPassword(rawPassword, salt, rounds)

        return formatHashString(hash, salt, rounds)
    }

    fun verify(rawPassword: ByteArray, encodedPassword: String): Boolean {
        val parts = encodedPassword.split("$")
        if (parts.size != 5 || parts[1] != "pbkdf2-sha256") {
            return false
        }

        val roundsPart = parts[2]

        val rounds = roundsPart.toIntOrNull() ?: return false
        val salt = try {
            b64Decoder.decode(parts[3])
        } catch (e: IllegalArgumentException) {
            return false
        }
        val storedHash = try {
            b64Decoder.decode(parts[4])
        } catch (e: IllegalArgumentException) {
            return false
        }

        val computedHash = hashPassword(rawPassword, salt, rounds)

        return computedHash.contentEquals(storedHash)
    }

    private fun hashPassword(password: ByteArray, salt: ByteArray, rounds: Int): ByteArray {
        val generator = PKCS5S2ParametersGenerator(SHA256Digest())
        generator.init(password, salt, rounds)
        val key = generator.generateDerivedParameters(SHA256_DIGEST_LENGTH * 8) as KeyParameter
        return key.key
    }

    private fun formatHashString(hash: ByteArray, salt: ByteArray, rounds: Int): String {
        return "\$pbkdf2-sha256\$${rounds}\$${b64Encoder.encodeToString(salt)}\$${b64Encoder.encodeToString(hash)}"
    }
}
