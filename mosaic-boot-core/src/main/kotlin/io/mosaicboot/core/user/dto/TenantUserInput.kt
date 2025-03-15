package io.mosaicboot.core.user.dto

import io.mosaicboot.core.user.entity.SiteRole
import io.mosaicboot.core.user.enums.UserStatus

class TenantUserInput(
    val tenantId: String,
    val userId: String,
    val nickname: String,
    val email: String?,
    val status: UserStatus,
    val roles: Set<SiteRole>,
)