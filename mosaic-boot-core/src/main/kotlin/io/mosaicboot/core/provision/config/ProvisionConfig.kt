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

package io.mosaicboot.core.provision.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.repository.init.Jackson2RepositoryPopulatorFactoryBean

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MosaicProvisionProperties::class)
@ConditionalOnProperty(prefix = "mosaic.provision", name = ["enabled"], havingValue = "true", matchIfMissing = false)
class ProvisionConfig(
    private val mosaicProvisionProperties: MosaicProvisionProperties,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun mosaicRepositoryPopulator(): Jackson2RepositoryPopulatorFactoryBean {
        val factory = Jackson2RepositoryPopulatorFactoryBean()

        val mapper = objectMapper.copy()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        factory.setMapper(mapper)
        factory.setResources(
            mosaicProvisionProperties.resources.map {
                ClassPathResource(it)
            }.toTypedArray()
        )

        return factory
    }
}
