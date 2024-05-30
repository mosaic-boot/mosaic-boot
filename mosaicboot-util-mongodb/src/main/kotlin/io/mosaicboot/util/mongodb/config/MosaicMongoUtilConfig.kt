package io.mosaicboot.util.mongodb.config

import com.mongodb.TransactionOptions
import io.mosaicboot.util.mongodb.CredentialToUriMongoClientSettingsBuilderCustomizer
import io.mosaicboot.util.mongodb.DummyMongoTransactionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import java.net.URI

class MosaicMongoUtilConfig {
    @Bean("mongoTransactionManager")
    fun mongoTransactionManager(
        properties: MongoProperties,
        dbFactory: MongoDatabaseFactory,
        @Autowired(required = false) mongoTransactionOptions: TransactionOptions?,
    ): MongoTransactionManager {
        var isReplicaSet = properties.replicaSetName?.isNotEmpty() ?: false
        if (!isReplicaSet && properties.uri != null) {
            val uri = URI.create(properties.uri)
            if (uri.query.contains(Regex("replicaSet", RegexOption.IGNORE_CASE))) {
                isReplicaSet = true
            }
        }
        if (isReplicaSet) {
            return MongoTransactionManager(dbFactory, mongoTransactionOptions)
        } else {
            return DummyMongoTransactionManager(dbFactory)
        }
    }

    @Bean
    fun mongoPropertiesClientSettingsBuilderCustomizer(
        properties: MongoProperties,
        environment: Environment?,
    ): CredentialToUriMongoClientSettingsBuilderCustomizer {
        return CredentialToUriMongoClientSettingsBuilderCustomizer(
            properties,
            environment
        )
    }
}