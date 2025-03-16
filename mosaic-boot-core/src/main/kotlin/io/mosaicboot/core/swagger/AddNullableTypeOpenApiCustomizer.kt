package io.mosaicboot.core.swagger

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.customizers.OpenApiCustomizer

class AddNullableTypeOpenApiCustomizer : OpenApiCustomizer {
    override fun customise(openApi: OpenAPI) {
        for (schema in openApi.components.schemas.values) {
            if (schema.properties == null) {
                continue
            }

            (schema.properties as Map<String?, Schema<*>>).forEach { (name: String?, value: Schema<*>) ->
                if (schema.required == null || !schema.required.contains(name) || value.nullable == true) {
                    value.nullable = true
                    value.types.add("null")
                }
            }
        }
    }
}