package io.mosaicboot.core.tenant.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.core.tenant.enums.TenantStatus
import io.mosaicboot.core.user.enums.UserStatus

data class TenantResponse(
    @field:JsonProperty("id")
    val id: String,

    @field:JsonProperty("name")
    val name: String,

    @field:JsonProperty("timeZone")
    val timeZone: String,

    @field:JsonProperty("status")
    val status: TenantStatus,
)

data class TenantUserResponse(
    @field:JsonProperty("userId")
    val userId: String,

    @field:JsonProperty("nickname")
    val nickname: String,

    @field:JsonProperty("email")
    val email: String,

    @field:JsonProperty("status")
    val status: UserStatus,

    @field:JsonProperty("roles")
    val roles: List<String>,
)

data class CreateTenantRequest(
    @field:JsonProperty("name")
    val name: String,

    @field:JsonProperty("timeZone")
    val timeZone: String?
)

data class UpdateTenantRequest(
    @field:JsonProperty("name")
    val name: String,

    @field:JsonProperty("timeZone")
    val timeZone: String
)

data class InviteUserRequest(
    @field:JsonProperty("email")
    val email: String,
)

enum class InviteResultCode {
    SUCCESS,
    ALREADY_EXISTS,
    USER_NOT_EXISTS,
}

data class InviteResponse(
    @field:JsonProperty("result")
    val result: InviteResultCode,
)
