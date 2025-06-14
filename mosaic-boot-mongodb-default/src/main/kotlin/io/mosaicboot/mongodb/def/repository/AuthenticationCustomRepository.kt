package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.data.repository.AuthenticationMosaicRepository
import io.mosaicboot.mongodb.def.entity.AuthenticationEntity

interface AuthenticationCustomRepository :
    AuthenticationMosaicRepository<AuthenticationEntity>