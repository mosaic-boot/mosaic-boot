package io.mosaicboot.core.tenant.dto

import io.mosaicboot.core.user.entity.TenantUser

sealed class InviteResult {
    class Success(val tenantUser: TenantUser) : InviteResult()
    class AlreadyExists(val tenantUser: TenantUser) : InviteResult()
    data object UserNotExists : InviteResult()
}