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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.mosaicboot.core.auth.config.MosaicAuthConfig
import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicOpenAPIService
import io.mosaicboot.core.http.MosaicRequestMappingHandlerMapping
import io.mosaicboot.core.permission.aspect.AuthorizationContext
import io.mosaicboot.core.provision.config.ProvisionConfig
import io.mosaicboot.core.swagger.AddNullableTypeOpenApiCustomizer
import io.mosaicboot.core.tenant.config.MosaicTenantConfig
import io.mosaicboot.core.user.config.MosaicUserConfig
import io.mosaicboot.core.util.AuthorizationContextResolver
import io.mosaicboot.core.encryption.JweServerSideCryptoProvider
import io.mosaicboot.core.encryption.ServerSideCrypto
import io.mosaicboot.core.encryption.ServerSideCryptoProvider
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.util.pattern.PathPatternParser
import java.util.*
import java.util.function.Predicate

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
    MosaicComponentsProperties::class,
    MosaicEncryptionProperties::class,
)
@EnableTransactionManagement
@EnableAspectJAutoProxy
@Import(value = [
    MosaicUserConfig::class,
    MosaicAuthConfig::class,
    MosaicTenantConfig::class,
    ProvisionConfig::class,
])
class MosaicConfig {
    init {
        SpringDocUtils.getConfig()
            .addRequestWrapperToIgnore(
                WebClientInfo::class.java,
                AuthorizationContext::class.java,
            )
            .addFluxWrapperToIgnore(WebClientInfo::class.java)
            .addFluxWrapperToIgnore(AuthorizationContext::class.java)
            .addJavaTypeToIgnore(WebClientInfo::class.java)
            .addJavaTypeToIgnore(AuthorizationContext::class.java)
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper::class)
    fun objectMapper(): ObjectMapper {
        return jsonMapper {
            addModule(kotlinModule())
            addModule(JavaTimeModule())
        }
    }

    @Bean
    fun addNullableTypeOpenApiCustomizer(): AddNullableTypeOpenApiCustomizer {
        return AddNullableTypeOpenApiCustomizer()
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

    @Bean
    fun mosaicControllerMapping(
        applicationContext: ApplicationContext,
        controllers: List<BaseMosaicController>,
    ): MosaicRequestMappingHandlerMapping {
        val requestMappingHandlerMapping = MosaicRequestMappingHandlerMapping()
        val urlMap = HashMap<String, Predicate<Class<*>>>()
        controllers.forEach { controller ->
            urlMap[controller.getBaseUrl(applicationContext)] = Predicate<Class<*>> { it: Class<*> ->
                it.isAssignableFrom(controller.javaClass)
            }
        }
        requestMappingHandlerMapping.order = -1
        requestMappingHandlerMapping.pathPrefixes = urlMap
        requestMappingHandlerMapping.patternParser = PathPatternParser()
        return requestMappingHandlerMapping
    }

    @Configuration(proxyBeanMethods = false)
    class WebConfig : WebMvcConfigurer {
        override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
            resolvers.add(WebClientInfoResolver())
            resolvers.add(AuthorizationContextResolver())
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "mosaic.provision", name = ["type"], havingValue = "jwe", matchIfMissing = true)
    fun jweServerSideCryptoProvider(
        encryptionProperties: MosaicEncryptionProperties,
        objectMapper: ObjectMapper,
    ): JweServerSideCryptoProvider {
        return JweServerSideCryptoProvider(
            encryptionProperties.jwe,
            objectMapper,
        )
    }

    @Bean
    fun serverSideCrypto(
        providers: List<ServerSideCryptoProvider>
    ): ServerSideCrypto {
        return ServerSideCrypto(providers)
    }
}
