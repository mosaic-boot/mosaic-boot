package io.mosaicboot.mongodb.def.payment.converter

import io.mosaicboot.payment.db.dto.OrderStatus
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

object OrderStatusConverters {
    private val mapByValue = OrderStatus.entries.associateBy(OrderStatus::value)

    @WritingConverter
    class ToString : Converter<OrderStatus, String> {
        override fun convert(source: OrderStatus): String {
            return source.value
        }
    }

    @ReadingConverter
    class FromString : Converter<String, OrderStatus> {
        override fun convert(source: String): OrderStatus {
            return mapByValue[source]
                ?: throw IllegalArgumentException("unknown value for OrderStatus: '${source}'")
        }
    }
}