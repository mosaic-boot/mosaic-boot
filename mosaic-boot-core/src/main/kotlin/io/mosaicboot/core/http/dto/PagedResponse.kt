package io.mosaicboot.core.http.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class PagedResponse<T>(
    @field:JsonProperty("items")
    val items: List<T>,
    @field:JsonProperty("total")
    val total: Long,
    /**
     * 0 ... (total/size)
     */
    @field:JsonProperty("page")
    val page: Int,
    @field:JsonProperty("size")
    val size: Int,
)