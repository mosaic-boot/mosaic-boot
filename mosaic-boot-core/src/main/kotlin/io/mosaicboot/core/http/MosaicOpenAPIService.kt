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

package io.mosaicboot.core.http

import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.customizers.OpenApiBuilderCustomizer
import org.springdoc.core.customizers.ServerBaseUrlCustomizer
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.JavadocProvider
import org.springdoc.core.service.OpenAPIService
import org.springdoc.core.service.SecurityService
import org.springdoc.core.utils.PropertyResolverUtils
import java.util.*

class MosaicOpenAPIService(
    openAPI: Optional<OpenAPI>,
    securityParser: SecurityService?,
    springDocConfigProperties: SpringDocConfigProperties?,
    propertyResolverUtils: PropertyResolverUtils?,
    openApiBuilderCustomizers: Optional<List<OpenApiBuilderCustomizer>>,
    serverBaseUrlCustomizers: Optional<List<ServerBaseUrlCustomizer>>,
    javadocProvider: Optional<JavadocProvider>
) : OpenAPIService(
    openAPI,
    securityParser,
    springDocConfigProperties,
    propertyResolverUtils,
    openApiBuilderCustomizers,
    serverBaseUrlCustomizers,
    javadocProvider
) {
    override fun build(locale: Locale?): OpenAPI {
        mappingsMap.putAll(context.getBeansWithAnnotation(MosaicController::class.java))
        return super.build(locale)
    }
}