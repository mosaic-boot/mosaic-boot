package io.mosaicboot.core.tenant.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.mosaicboot.core.tenant.enums.TenantStatus

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

    @field:JsonProperty("role")
    val role: String
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

    @field:JsonProperty("role")
    val role: String
)

data class InviteResponse(
    @field:JsonProperty("inviteCode")
    val inviteCode: String,

    @field:JsonProperty("email")
    val email: String
)
