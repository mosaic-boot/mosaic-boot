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

package io.mosaicboot.core.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.util.*

class JacksonByteArrayConverter {
    class Serializer : JsonSerializer<ByteArray>() {
        override fun serialize(value: ByteArray, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeString(Base64.getEncoder().encodeToString(value))
        }
    }

    class Deserializer : JsonDeserializer<ByteArray>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ByteArray {
            return Base64.getDecoder().decode(p.text)
        }
    }
}