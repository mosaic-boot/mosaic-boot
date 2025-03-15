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

package io.mosaicboot.core.user.auth

import io.mosaicboot.core.auth.MosaicSha256PasswordEncoder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

class MosaicSha256PasswordEncoderTest {
    private val encoder = MosaicSha256PasswordEncoder()

    @Test
    @DisplayName("Encoded password should have correct format")
    fun encodedPasswordShouldHaveCorrectFormat() {
        // given
        val rawPassword = "testPassword123"

        // when
        val encodedPassword = encoder.encode(rawPassword.toByteArray())

        // then
        assertTrue(encodedPassword.startsWith("\$pbkdf2-sha256\$"))
        assertTrue(encodedPassword.contains("\$"))

        val parts = encodedPassword.split("\$")
        assertEquals(5, parts.size)
        assertEquals("pbkdf2-sha256", parts[1])
        assertTrue(parts[2].toIntOrNull() != null)
    }

    @Test
    @DisplayName("Encoding same password twice should produce different results")
    fun encodingSamePasswordTwiceShouldProduceDifferentResults() {
        // given
        val password = "testPassword123"

        // when
        val firstEncoded = encoder.encode(password.toByteArray())
        val secondEncoded = encoder.encode(password.toByteArray())

        // then
        assertNotEquals(firstEncoded, secondEncoded)
    }

    @Test
    @DisplayName("Password verification should work correctly")
    fun passwordVerificationShouldWork() {
        // given
        val rawPassword = "testPassword123"
        val encodedPassword = encoder.encode(rawPassword.toByteArray())

        // when & then
        assertTrue(encoder.verify(rawPassword.toByteArray(), encodedPassword))
        assertFalse(encoder.verify("wrongPassword".toByteArray(), encodedPassword))
    }

    @Test
    @DisplayName("Should accept custom rounds parameter")
    fun shouldAcceptCustomRounds() {
        // given
        val password = "testPassword"
        val customRounds = 80000

        // when
        val encoded = encoder.encode(password.toByteArray(), customRounds)

        // then
        assertTrue(encoded.contains("\$80000\$"))
        assertTrue(encoder.verify(password.toByteArray(), encoded))
    }

    @Test
    @DisplayName("Should return false when verifying invalid hash formats")
    fun shouldHandleInvalidHashFormat() {
        // given
        val invalidFormats = listOf(
            "invalidHash",
            "\$pbkdf2-sha256\$invalidRounds\$salt",
            "\$pbkdf-sha256\$1000\$salt", // wrong algorithm identifier
            "\$pbkdf2-sha256\$invalid\$salt",
            "\$pbkdf2-sha256\$1000\$" // empty salt/hash
        )

        // when & then
        invalidFormats.forEach { invalidHash ->
            assertFalse(encoder.verify("anyPassword".toByteArray(), invalidHash))
        }
    }

    @Test
    @DisplayName("Should verify passwords with real examples")
    fun shouldVerifyWithRealExample() {
        // given
        val password = "password"
        val encoded = "\$pbkdf2-sha256\$6400\$0ZrzXitFSGltTQnBWOsdAw\$Y11AchqV4b0sUisdZd0Xr97KWoymNE0LNNrnEgY4H9M"

        // when & then
        assertTrue(encoder.verify(password.toByteArray(), encoded))
    }
}
