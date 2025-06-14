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

package io.mosaicboot.data.repository

import io.mosaicboot.common.user.dto.CurrentActiveUser
import io.mosaicboot.common.user.dto.TenantUserInput
import io.mosaicboot.data.entity.TenantUser

interface TenantUserMosaicRepository<T : TenantUser> {
    fun save(input: TenantUserInput): T
    fun findByTenantIdAndId(tenantId: String, id: String): TenantUser?
    fun findWithUser(
        userId: String,
        tenantId: String,
        tenantUserId: String,
    ): CurrentActiveUser?
}