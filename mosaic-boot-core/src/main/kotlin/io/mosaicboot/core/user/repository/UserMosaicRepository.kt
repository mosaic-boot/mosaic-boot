package io.mosaicboot.core.user.repository

import io.mosaicboot.core.user.dto.UserInput
import io.mosaicboot.core.user.entity.User

interface UserMosaicRepository<T : User> {
    fun save(input: UserInput): T
}