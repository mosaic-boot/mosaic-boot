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

package io.mosaicboot.core.jwt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jwt.JWTClaimsSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.BadCredentialsException

class JwtHelperTest {
    @JwtContentType("test-jwt")
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TestJwtClaim(
        val username: String,
        val roles: List<String>
    )

    private lateinit var helper: JwtHelper
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        objectMapper = jacksonObjectMapper()
        helper = JwtHelper(
            algorithm = JWSAlgorithm.HS256,
            jwkSecret = OctetSequenceKey.Builder("test-secret-key-must-be-32-bytes".toByteArray())
                .keyIDFromThumbprint()
                .build(),
            objectMapper = objectMapper,
            expirationSeconds = 3600,
        )
    }

    @Test
    fun `should successfully sign and verify JWT token`() {
        // given
        val testClaim = TestJwtClaim(
            username = "testuser",
            roles = listOf("ROLE_USER", "ROLE_ADMIN")
        )

        // when
        val token = helper.encode(JWTClaimsSet.Builder(), testClaim)
        val decoded = helper.decode(token, TestJwtClaim::class.java)

        // then
        assertThat(decoded).isNotNull
        assertThat(decoded.username).isEqualTo(testClaim.username)
        assertThat(decoded.roles).containsExactlyElementsOf(testClaim.roles)
    }

    @Test
    fun `should fail verification with invalid token`() {
        // given
        val invalidToken = "invalid.jwt.token"

        // when & then
        assertThrows<BadCredentialsException> {
            helper.decode(invalidToken, TestJwtClaim::class.java)
        }
    }

//    @Test
//    fun `should fail verification with expired token`() {
//        // given
//        val testClaim = TestJwtClaim(
//            username = "testuser",
//            roles = listOf("ROLE_USER")
//        )
//        val token = helper.sign(JWTClaimsSet.Builder(), "test-subject", testClaim)
//
//        // when
//        Thread.sleep(1100) // Wait for token to expire
//
//        // then
//        org.junit.jupiter.api.assertThrows<BadCredentialsException> {
//            helper.verify(token, TestJwtClaim::class.java)
//        }
//    }

    @Test
    fun `should fail verification with wrong content type`() {
        // given
        val testClaim = TestJwtClaim(
            username = "testuser",
            roles = listOf("ROLE_USER")
        )
        val token = helper.sign(JWTClaimsSet.Builder(), "test-subject", testClaim)

        // then
        assertThrows<BadCredentialsException> {
            helper.decode(token, TestJwtClaim::class.java)
        }
    }
}