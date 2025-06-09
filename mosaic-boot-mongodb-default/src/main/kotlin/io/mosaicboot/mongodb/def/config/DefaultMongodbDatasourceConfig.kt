/**
 * Copyright 2025 JC-Lab (mosaicboot.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mosaicboot.mongodb.def.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.connection.ClusterSettings
import io.mosaicboot.mongodb.def.payment.converter.OrderStatusConverters
import io.mosaicboot.mongodb.def.payment.converter.TransactionTypeConverters
import io.mosaicboot.mongodb.def.payment.repository.PaymentBillingRepository
import io.mosaicboot.mongodb.def.payment.repository.PaymentLogRepository
import io.mosaicboot.mongodb.def.payment.repository.PaymentTransactionRepository
import io.mosaicboot.mongodb.def.repository.TenantRepository
import io.mosaicboot.mongodb.def.repository.UserRepository
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.util.UriComponentsBuilder

@Configuration(proxyBeanMethods = false)
@EnableMongoRepositories(
    basePackageClasses = [
        TenantRepository::class,
        UserRepository::class,
        PaymentLogRepository::class,
        PaymentTransactionRepository::class,
        PaymentBillingRepository::class,
    ],
)
@EnableConfigurationProperties(MongodbCollectionsProperties::class)
class DefaultMongodbDatasourceConfig {
    @Bean("mongoTransactionManager")
    fun mongoTransactionManager(
        properties: MongoProperties,
        dbFactory: MongoDatabaseFactory
    ): PlatformTransactionManager {
        return MongoTransactionManager(dbFactory)
    }

    @Bean
    fun mosaicMongoConversions(): MongoCustomConversions {
        return MongoCustomConversions.create { configure ->
            configure.registerConverter(TransactionTypeConverters.ToString())
            configure.registerConverter(TransactionTypeConverters.FromString())
            configure.registerConverter(OrderStatusConverters.ToString())
            configure.registerConverter(OrderStatusConverters.FromString())
        }
    }


//    @Bean
//    fun mongoClientSettings(): MongoClientSettings {
//        return MongoClientSettings.builder().build()
//    }

    @Bean
    fun mongoPropertiesClientSettingsBuilderCustomizer(
        properties: MongoProperties,
        environment: Environment
    ): CredentialToUriMongoClientSettingsBuilderCustomizer {
        return CredentialToUriMongoClientSettingsBuilderCustomizer(properties, environment)
    }

    class CredentialToUriMongoClientSettingsBuilderCustomizer(
        private val properties: MongoProperties,
        private val environment: Environment?,
    ) : MongoClientSettingsBuilderCustomizer
    {
        override fun customize(settingsBuilder: MongoClientSettings.Builder) {
            applyUuidRepresentation(settingsBuilder)
            applyHostAndPort(settingsBuilder)
            applyCredentials(settingsBuilder)
            applyReplicaSet(settingsBuilder)
        }

        private fun applyUuidRepresentation(builder: MongoClientSettings.Builder) {
            builder.uuidRepresentation(this.properties.uuidRepresentation)
        }

        private fun applyHostAndPort(builder: MongoClientSettings.Builder) {
            if (getEmbeddedPort() != null) {
                builder.applyConnectionString(ConnectionString("mongodb://localhost:" + getEmbeddedPort()))
                return
            }
            if (this.properties.uri != null) {
                builder.applyConnectionString(ConnectionString(this.properties.uri))
                return
            }
            if (this.properties.host != null || this.properties.port != null) {
                val host = getOrDefault(this.properties.host, "localhost")
                val port = getOrDefault(this.properties.port, MongoProperties.DEFAULT_PORT)
                val serverAddress = ServerAddress(host, port)
                builder.applyToClusterSettings { cluster: ClusterSettings.Builder ->
                    cluster.hosts(
                        listOf(serverAddress)
                    )
                }
                return
            }
            builder.applyConnectionString(ConnectionString(MongoProperties.DEFAULT_URI))
        }

        private fun applyCredentials(builder: MongoClientSettings.Builder) {
            if ((this.properties.username != null) && (this.properties.password != null)) {
                val database = this.properties.uri
                    ?.let { UriComponentsBuilder.fromUriString(it).build().queryParams["authSource"]?.first() }
                    ?:this.properties.authenticationDatabase
                    ?:this.properties.mongoClientDatabase
                builder.credential(
                    MongoCredential.createCredential(
                        this.properties.username,
                        database,
                        this.properties.password
                    )
                )
            }
        }

        private fun applyReplicaSet(builder: MongoClientSettings.Builder) {
            if (this.properties.replicaSetName != null) {
                builder.applyToClusterSettings { cluster: ClusterSettings.Builder ->
                    cluster.requiredReplicaSetName(
                        this.properties.replicaSetName
                    )
                }
            }
        }

        private fun <V> getOrDefault(value: V?, defaultValue: V): V {
            return value ?: defaultValue
        }

        private fun getEmbeddedPort(): Int? {
            return this.environment?.getProperty("local.mongo.port")?.toInt()
        }
    }
}