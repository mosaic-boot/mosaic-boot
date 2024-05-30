package io.mosaicboot.account.mongodb.authentication.jwt.config

import io.mosaicboot.account.mongodb.authentication.jwt.MongoJwtAuthenticationKeyRepository
import io.mosaicboot.account.mongodb.authentication.jwt.repository.JwkRepository
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@EnableMongoRepositories(
    basePackageClasses = [
        JwkRepository::class,
    ]
)
class MosaicMongoJwkKeyStoreConfig {
    @Bean
    fun mongoJwtAuthenticationKeyRepository(
        jwkRepository: JwkRepository,
    ): MongoJwtAuthenticationKeyRepository {
        return MongoJwtAuthenticationKeyRepository(jwkRepository)
    }
}