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

package io.mosaicboot.mongodb.def.oauth2.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "\${mosaic.datasource.mongodb.collections.userOauth2AccessToken.collection:userOauth2Tokens}")
data class OAuth2AccessTokenEntity(
    @Id
    val id: String,
    @Field("userId")
    val userId: String,
    @Field("issuedAt")
    var issuedAt: Instant,
    @Field("expireAt")
    var expireAt: Instant,
    @Field("lockId")
    var lockId: String? = null,
    @Field("unlockAt")
    var unlockAt: Instant? = null,
    @Field("data")
    var data: String,
)