package io.mosaicboot.core.tenant.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TenantUserListResponse(
    @field:JsonProperty("items")
    val items: List<TenantUserResponse>,
    @field:JsonProperty("total")
    val total: Int,
    /**
     * 0 ~ total/size
     */
    @field:JsonProperty("page")
    val page: Int,
    @field:JsonProperty("size")
    val size: Int,
)