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
                    value.types?.add("null")
                }
            }
        }
    }
}