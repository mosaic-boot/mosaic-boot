package io.mosaicboot.core.tenant.dto

import io.mosaicboot.core.tenant.enums.TenantStatus

data class TenantInput(
    val name: String,
    val status: TenantStatus,
    val timeZone: String,
)