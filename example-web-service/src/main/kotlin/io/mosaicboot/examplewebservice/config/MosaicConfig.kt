package io.mosaicboot.examplewebservice.config

import io.mosaicboot.account.authentication.jwt.config.EnableJwtAuthentication
import io.mosaicboot.account.mongodb.authentication.jwt.config.EnableMongoJwtKeyStore
import io.mosaicboot.util.mongodb.config.EnableMongoUtil
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoUtil
@EnableMongoRepositories
@EnableMongoJwtKeyStore
@EnableJwtAuthentication
class MosaicConfig {
}