package io.mosaicboot.account.mongodb.authentication.jwt.repository

import io.mosaicboot.account.mongodb.authentication.jwt.entity.JwkEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface JwkRepository : MongoRepository<JwkEntity, String>