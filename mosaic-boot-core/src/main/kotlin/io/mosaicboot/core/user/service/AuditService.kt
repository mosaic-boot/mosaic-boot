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

package io.mosaicboot.core.user.service

import io.mosaicboot.data.entity.UserAuditLog
import io.mosaicboot.common.user.dto.UserAuditLogInput
import io.mosaicboot.data.repository.UserAuditLogRepositoryBase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class AuditService(
    private val userAuditLogRepository: UserAuditLogRepositoryBase<*, *>,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun addLog(log: UserAuditLogInput): UserAuditLog<*> {
        return userAuditLogRepository.save(log)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun addLogs(logs: List<UserAuditLogInput>): List<UserAuditLog<*>> {
        return userAuditLogRepository.saveAll(logs)
    }
}
