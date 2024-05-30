package io.mosaicboot.account

import io.mosaicboot.account.service.MosaicAuthService
import org.springframework.context.annotation.Import
import java.lang.annotation.*
import java.lang.annotation.Retention
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(
    MosaicAuthService::class,
)
annotation class EnableAccountModule
