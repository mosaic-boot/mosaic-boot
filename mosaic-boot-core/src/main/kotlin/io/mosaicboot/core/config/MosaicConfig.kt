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

package io.mosaicboot.core.config

import io.mosaicboot.core.http.MosaicOpenAPIService
import io.mosaicboot.core.provision.config.ProvisionConfig
import io.mosaicboot.core.user.config.MosaicUserConfig
import io.mosaicboot.core.util.WebClientInfo
import io.mosaicboot.core.util.WebClientInfoResolver
import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.customizers.OpenApiBuilderCustomizer
import org.springdoc.core.customizers.ServerBaseUrlCustomizer
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.JavadocProvider
import org.springdoc.core.service.OpenAPIService
import org.springdoc.core.service.SecurityService
import org.springdoc.core.utils.PropertyResolverUtils
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*

@Configuration(proxyBeanMethods = true)
@EnableConfigurationProperties(MosaicComponentsProperties::class)
@EnableTransactionManagement
@Import(value = [
    MosaicUserConfig::class,
    ProvisionConfig::class,
])
class MosaicConfig {
    companion object {
        init {
            SpringDocUtils.getConfig().addJavaTypeToIgnore(WebClientInfo::class.java)
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(-1)
    @Lazy(false)
    fun openAPIBuilder(
        openAPI: Optional<OpenAPI>,
        securityParser: SecurityService?,
        springDocConfigProperties: SpringDocConfigProperties,
        propertyResolverUtils: PropertyResolverUtils?,
        openApiBuilderCustomisers: Optional<List<OpenApiBuilderCustomizer>>,
        serverBaseUrlCustomisers: Optional<List<ServerBaseUrlCustomizer>>,
        javadocProvider: Optional<JavadocProvider>
    ): OpenAPIService {
        return MosaicOpenAPIService(
            openAPI,
            securityParser,
            springDocConfigProperties,
            propertyResolverUtils,
            openApiBuilderCustomisers,
            serverBaseUrlCustomisers,
            javadocProvider
        )
    }

    @Configuration(proxyBeanMethods = true)
    class WebConfig : WebMvcConfigurer {
        override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
            resolvers.add(WebClientInfoResolver())
        }
    }
}
