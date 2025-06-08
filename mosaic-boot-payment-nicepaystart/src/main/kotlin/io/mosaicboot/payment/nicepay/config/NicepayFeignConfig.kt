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

package io.mosaicboot.payment.nicepay.config

import feign.Feign
import feign.RequestInterceptor
import feign.RequestTemplate
import feign.Target
import org.springframework.cloud.openfeign.FeignClientFactory
import org.springframework.cloud.openfeign.FeignClientFactoryBean
import org.springframework.cloud.openfeign.Targeter
import org.springframework.context.annotation.Bean
import java.nio.charset.StandardCharsets
import java.util.Base64

class NicepayFeignConfig(
    private val nicepayProperties: NicepayProperties,
) {
    @Bean
    fun feignTargeter(): Targeter {
        return object: Targeter {
            override fun <T> target(
                factory: FeignClientFactoryBean,
                feign: Feign.Builder,
                context: FeignClientFactory,
                target: Target.HardCodedTarget<T>,
            ): T {
                val fixedUrl = target.url()
                    .replace("https://api.nicepay.co.kr", nicepayProperties.apiUrl)
                return feign.target(Target.HardCodedTarget(target.type(), target.name(), fixedUrl))
            }
        }
    }

    @Bean
    fun authorizationInterceptor(): AuthorizationInterceptor {
        return AuthorizationInterceptor(nicepayProperties)
    }

    class AuthorizationInterceptor(
        private val nicepayProperties: NicepayProperties,
    ) : RequestInterceptor {
        override fun apply(request: RequestTemplate) {
            val authorizationHeader = "Basic " + Base64.getEncoder().encodeToString(
                "${nicepayProperties.clientId}:${nicepayProperties.secretKey}".toByteArray(
                    StandardCharsets.UTF_8
                )
            )
            request.header("Authorization", authorizationHeader)
        }
    }
}