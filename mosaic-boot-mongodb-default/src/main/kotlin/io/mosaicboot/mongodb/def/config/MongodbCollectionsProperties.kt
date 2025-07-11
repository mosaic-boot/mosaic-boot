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

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mosaic.datasource.mongodb.collections")
data class MongodbCollectionsProperties(
    var tenant: Collection = Collection("tenants"),
    var user: Collection = Collection("users"),
    var permission: Collection = Collection("permissions"),
    var globalRoles: Collection = Collection("globalRoles"),
    var tenantRoles: Collection = Collection("tenantRoles"),
    var tenantUser: Collection = Collection("tenantUsers"),
    var authentication: Collection = Collection("authentications"),
    var userAuditLog: Collection = Collection("userAuditLog"),

    var userOauth2AccessToken: Collection = Collection("userOauth2Tokens"),
) {
    data class Collection(
        var collection: String,
        var customized: Boolean = false,
    )
}
