package io.mosaicboot.core.user.dto

import io.mosaicboot.core.user.entity.SiteRole
import io.mosaicboot.core.user.entity.TenantUser

interface TenantUserDetail : TenantUser {
    val roles: Set<SiteRole>
}