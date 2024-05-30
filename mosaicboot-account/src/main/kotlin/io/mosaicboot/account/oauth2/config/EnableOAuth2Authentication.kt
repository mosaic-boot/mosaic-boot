package io.mosaicboot.account.oauth2.config

import io.mosaicboot.account.EnableAccountModule
import org.springframework.context.annotation.Import
import java.lang.annotation.*
import java.lang.annotation.Retention
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(
    OAuth2AuthenticationConfig::class,
)
@EnableAccountModule
annotation class EnableOAuth2Authentication
