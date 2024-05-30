package io.mosaicboot.account.mongodb.authentication.jwt.config

import org.springframework.context.annotation.Import
import java.lang.annotation.*
import java.lang.annotation.Retention
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(
    MosaicMongoJwkKeyStoreConfig::class,
)
annotation class EnableMongoJwtKeyStore