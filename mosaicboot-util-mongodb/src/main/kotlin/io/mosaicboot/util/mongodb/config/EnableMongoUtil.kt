package io.mosaicboot.util.mongodb.config

import org.springframework.context.annotation.Import
import java.lang.annotation.*
import java.lang.annotation.Retention
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(
    MosaicMongoUtilConfig::class,
)
annotation class EnableMongoUtil