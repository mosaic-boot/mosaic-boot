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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.user.entity.User
import io.mosaicboot.core.auth.repository.AuthenticationRepositoryBase
import io.mosaicboot.core.user.dto.CurrentActiveUser
import io.mosaicboot.core.user.repository.TenantUserRepositoryBase
import io.mosaicboot.core.user.repository.UserRepositoryBase
import io.mosaicboot.core.user.controller.dto.ActiveTenantUser
import io.mosaicboot.core.user.controller.dto.UpdateUserRequest
import io.mosaicboot.core.user.controller.dto.LinkedOAuth2Provider
import io.mosaicboot.core.user.dto.UserAuditLogInput
import io.mosaicboot.core.user.enums.UserAuditAction
import io.mosaicboot.core.user.enums.UserAuditLogStatus
import io.mosaicboot.core.user.enums.UserStatus
import io.mosaicboot.core.util.WebClientInfo
import io.mosaicboot.core.auth.enums.AuthMethod
import io.mosaicboot.core.user.dto.UserAuditOAuth2LinkActionDetail
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class UserService(
    private val userRepository: UserRepositoryBase<*>,
    private val tenantUserRepository: TenantUserRepositoryBase<*>,
    private val authenticationRepository: AuthenticationRepositoryBase<*>,
    private val auditService: AuditService,
    private val objectMapper: ObjectMapper,
) {
    fun getCurrentUser(authentication: Authentication): User {
        if (authentication !is MosaicAuthenticatedToken) {
            throw InsufficientAuthenticationException("unauthorized")
        }
        return getUser(authentication.userId)
    }

    fun getUser(userId: String): User {
        return userRepository.findById(userId).get()
    }

    fun findUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    /**
     * TODO: caching
     */
    fun getCurrentActiveUser(userId: String, tenantUser: ActiveTenantUser?): CurrentActiveUser {
        return if (tenantUser != null) {
            tenantUserRepository.findWithUser(
                userId = userId,
                tenantId = tenantUser.tenantId,
                tenantUserId = tenantUser.tenantUserId,
            )
                ?.takeIf { it.user.id == userId }
        } else {
            userRepository.findById(userId).getOrNull()?.let { user ->
                CurrentActiveUser(user = user, tenantUser = null)
            }
        } ?: throw IllegalArgumentException("No current active user")
    }

    @Transactional
    fun updateUser(userId: String, updateRequest: UpdateUserRequest, webClientInfo: WebClientInfo) {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User not found")
        }

        val before = mapOf(
            "name" to user.name,
            "timeZone" to user.timeZone,
            "email" to user.email
        )
        
        val after = mutableMapOf<String, Any?>()
        updateRequest.name?.let { 
            user.name = it
            after["name"] = it
        }
        updateRequest.timeZone?.let { 
            user.timeZone = it
            after["timeZone"] = it
        }
        updateRequest.email?.let { 
            user.email = it
            after["email"] = it
        }

        userRepository.saveEntity(user)
        
        auditService.addLog(
            UserAuditLogInput(
                userId = userId,
                performedBy = userId,
                action = UserAuditAction.PROFILE_UPDATED,
                actionDetail = mapOf(
                    "before" to before,
                    "after" to after
                ),
                status = UserAuditLogStatus.SUCCESS,
                ipAddress = webClientInfo.ipAddress,
                userAgent = webClientInfo.userAgent
            )
        )
    }

    @Transactional
    fun deleteUser(userId: String, webClientInfo: WebClientInfo) {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User not found")
        }
        
        val originalStatus = user.status
        user.status = UserStatus.DELETED
        userRepository.saveEntity(user)
        
        auditService.addLog(
            UserAuditLogInput(
                userId = userId,
                performedBy = userId,
                action = UserAuditAction.ACCOUNT_DELETED,
                actionDetail = mapOf(
                    "beforeStatus" to originalStatus,
                    "afterStatus" to UserStatus.DELETED
                ),
                status = UserAuditLogStatus.SUCCESS,
                ipAddress = webClientInfo.ipAddress,
                userAgent = webClientInfo.userAgent,
            )
        )
    }
    
    fun getLinkedOAuth2Providers(userId: String): List<LinkedOAuth2Provider> {
        return authenticationRepository.findAllByUserId(userId)
            .filter { it.method.startsWith("${AuthMethod.PREFIX_OAUTH2}:") && !it.deleted }
            .map { auth ->
                LinkedOAuth2Provider(
                    provider = auth.method.removePrefix("${AuthMethod.PREFIX_OAUTH2}:"),
                    username = auth.username,
                    linkedAt = auth.createdAt.epochSecond,
                )
            }
    }

    @Transactional
    fun unlinkOAuth2Provider(userId: String, provider: String, webClientInfo: WebClientInfo) {
        val method = "${AuthMethod.PREFIX_OAUTH2}:${provider}"
        val authentication = authenticationRepository.findByUserIdAndMethod(userId, method)
            ?: throw IllegalArgumentException("OAuth2 provider not linked")

        val actionDetail = UserAuditOAuth2LinkActionDetail(
            method = authentication.method,
            username = authentication.username,
            authenticationId = authentication.id,
        )

        authenticationRepository.softDelete(authentication)
        
        auditService.addLog(
            UserAuditLogInput(
                userId = userId,
                performedBy = userId,
                action = UserAuditAction.ACCOUNT_OAUTH2_UNLINK,
                actionDetail = objectMapper.convertValue(
                    actionDetail,
                    object: TypeReference<Map<String, Any?>>(){},
                ),
                status = UserAuditLogStatus.SUCCESS,
                ipAddress = webClientInfo.ipAddress,
                userAgent = webClientInfo.userAgent
            )
        )
    }
}
