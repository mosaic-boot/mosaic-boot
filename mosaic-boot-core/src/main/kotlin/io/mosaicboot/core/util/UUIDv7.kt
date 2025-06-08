package io.mosaicboot.core.util

import com.fasterxml.uuid.Generators
import java.util.UUID

object UUIDv7 {
    private val generator = Generators.timeBasedEpochGenerator()

    fun generate(): UUID {
        return generator.generate()
    }
}