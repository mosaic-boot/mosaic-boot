package io.mosaicboot.core.util

data class PagedResult<T>(
    val items: List<T>,
    val total: Long,
)