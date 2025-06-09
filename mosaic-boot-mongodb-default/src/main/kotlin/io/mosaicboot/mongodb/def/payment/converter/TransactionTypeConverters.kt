package io.mosaicboot.mongodb.def.payment.converter

import io.mosaicboot.payment.db.dto.TransactionType
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

object TransactionTypeConverters {
    private val mapByValue = TransactionType.entries.associateBy(TransactionType::value)

    @WritingConverter
    class ToString : Converter<TransactionType, String> {
        override fun convert(source: TransactionType): String {
            return source.value
        }
    }

    @ReadingConverter
    class FromString : Converter<String, TransactionType> {
        override fun convert(source: String): TransactionType {
            return mapByValue[source]
                ?: throw IllegalArgumentException("unknown value for TransactionType: '${source}'")
        }
    }
}