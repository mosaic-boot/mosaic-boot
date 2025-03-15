package io.mosaicboot.core.user.controller.model

enum class SystemPermission(
    val id: String,
) {
    TENANT_OWNER("tenant.owner"),
    TENANT_INVITE("tenant.invite")
    ;
}