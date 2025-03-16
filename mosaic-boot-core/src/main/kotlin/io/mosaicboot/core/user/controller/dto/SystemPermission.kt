package io.mosaicboot.core.user.controller.dto

enum class SystemPermission(
    val id: String,
) {
    TENANT_OWNER("tenant.owner"),
    TENANT_INVITE("tenant.invite")
    ;
}