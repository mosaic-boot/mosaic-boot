package io.mosaicboot.core.permission.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RequirePermissions(val value: Array<RequirePermission>)
