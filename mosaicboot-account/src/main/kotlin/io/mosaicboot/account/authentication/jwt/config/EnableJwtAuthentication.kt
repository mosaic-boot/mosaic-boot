package io.mosaicboot.account.authentication.jwt.config

import io.mosaicboot.account.EnableAccountModule
import org.springframework.context.annotation.Import
import java.lang.annotation.*
import java.lang.annotation.Retention
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(
    MosaicJwtAuthenticationConfig::class,
)
@EnableAccountModule
annotation class EnableJwtAuthentication