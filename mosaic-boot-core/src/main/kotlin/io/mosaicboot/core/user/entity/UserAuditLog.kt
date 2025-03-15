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

package io.mosaicboot.core.user.entity

import io.mosaicboot.core.user.enums.UserAuditAction
import io.mosaicboot.core.user.enums.UserAuditLogStatus
import io.mosaicboot.core.entity.BaseEntity

interface UserAuditLog<ID : Any> : BaseEntity<ID> {
    /**
     * Login 로그의 경우 모든 Tenant 에 대해 여러 Row 을 저장해야 한다.
     */
    val tenantId: String?
    val userId: String?
    val performedBy: String? // 작업을 수행한 사용자의 ID
    val action: UserAuditAction

    /**
     * [UserAuditLoginActionDetail]
     */
    val actionDetail: Map<String, Any?>?
    val status: UserAuditLogStatus

    val ipAddress: String
    val userAgent: String
    val errorMessage: String?
}
