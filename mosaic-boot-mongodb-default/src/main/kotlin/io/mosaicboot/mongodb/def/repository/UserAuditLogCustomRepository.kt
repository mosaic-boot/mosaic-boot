package io.mosaicboot.mongodb.def.repository

import io.mosaicboot.core.user.repository.UserAuditLogMosaicRepository
import io.mosaicboot.mongodb.def.entity.UserAuditLogEntity
import org.bson.types.ObjectId

interface UserAuditLogCustomRepository : UserAuditLogMosaicRepository<UserAuditLogEntity, ObjectId>