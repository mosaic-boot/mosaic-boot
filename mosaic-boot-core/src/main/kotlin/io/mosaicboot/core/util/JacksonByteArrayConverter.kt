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