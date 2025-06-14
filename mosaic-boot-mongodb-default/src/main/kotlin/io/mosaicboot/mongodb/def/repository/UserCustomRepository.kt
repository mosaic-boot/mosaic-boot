package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.data.repository.UserMosaicRepository
import io.mosaicboot.mongodb.def.entity.UserEntity

interface UserCustomRepository :
    UserMosaicRepository<UserEntity>