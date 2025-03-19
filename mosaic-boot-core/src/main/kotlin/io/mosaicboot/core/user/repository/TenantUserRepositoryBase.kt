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

package io.mosaicboot.core.user.repository

import io.mosaicboot.core.repository.BaseTenantRepository
import io.mosaicboot.core.user.entity.TenantUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TenantUserRepositoryBase<T : TenantUser> : BaseTenantRepository<TenantUser, T, String>,
    TenantUserMosaicRepository
{
    fun findByTenantIdAndUserId(tenantId: String, userId: String): TenantUser?
    fun findAllByTenantId(tenantId: String, pageable: Pageable): Page<out TenantUser>
}