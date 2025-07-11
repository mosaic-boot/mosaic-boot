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

package io.mosaicboot.core.auth.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.mosaicboot.common.auth.dto.*
import io.mosaicboot.common.user.dto.*
import io.mosaicboot.common.user.enums.UserAuditAction
import io.mosaicboot.common.user.enums.UserAuditLogStatus
import io.mosaicboot.common.user.enums.UserStatus
import io.mosaicboot.core.auth.repository.AuthenticationRepositoryBase
import io.mosaicboot.data.repository.TenantUserRepositoryBase
import io.mosaicboot.data.repository.UserRepositoryBase
import io.mosaicboot.data.entity.Authentication
import io.mosaicboot.data.entity.TenantUser
import io.mosaicboot.data.repository.GlobalRoleRepositoryBase
import io.mosaicboot.core.user.service.AuditService
import io.mosaicboot.core.util.UnreachableException
import io.mosaicboot.core.util.WebClientInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthenticationService(
    private val authenticationRepository: AuthenticationRepositoryBase<*>,
    private val userRepository: UserRepositoryBase<*>,
    private val tenantUserRepository: TenantUserRepositoryBase<*>,
    private val globalRoleRepositoryBase: GlobalRoleRepositoryBase<*>,
    private val credentialService: CredentialService,
    private val auditService: AuditService,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun login(
        method: String,
        username: String,
        credential: String?,
        webClientInfo: WebClientInfo,
    ): LoginResult {
        val authDetail = findAuthenticationDetail(method, username)
        val auditActionDetail = UserAuditLoginActionDetail(
            userStatus = authDetail?.user?.status,
            method = method,
            username = username,
        )

        if (authDetail == null || authDetail.user.status == UserStatus.DELETED) {
            logFailedLogin(
                userId = null,
                tenantUsers = null,
                auditActionDetail = auditActionDetail,
                webClientInfo = webClientInfo,
            )
            return LoginResult.Failure(LoginFailureReason.NO_USER)
        }

        val tenantUsers = getTenantUsers(authDetail.userId)
        if (!credentialService.validateCredential(method, username, credential, authDetail)) {
            logFailedLogin(
                userId = authDetail.userId,
                tenantUsers = tenantUsers.map { Pair(it, null) },
                auditActionDetail = auditActionDetail,
                webClientInfo = webClientInfo,
            )
            return LoginResult.Failure(LoginFailureReason.WRONG_CREDENTIAL)
        }

        return processUserLogin(authDetail, tenantUsers, webClientInfo, auditActionDetail)
    }

    private fun processUserLogin(
        authDetail: AuthenticationDetail,
        tenantUsers: List<TenantUser>,
        webClientInfo: WebClientInfo,
        auditActionDetail: UserAuditLoginActionDetail
    ): LoginResult {
        if (authDetail.user.status != UserStatus.ACTIVE) {
            val reason = when (authDetail.user.status) {
                UserStatus.INVALID -> LoginFailureReason.INVALID
                UserStatus.BLOCKED -> LoginFailureReason.BLOCKED_USER
                else -> throw UnreachableException()
            }
            logBlockedOrInvalidLogin(
                userId = authDetail.userId,
                reason = reason,
                auditActionDetail = auditActionDetail,
                webClientInfo = webClientInfo,
            )
            return LoginResult.Failure(reason)
        }

        val tenantUserResults = tenantUsers.map { tenantUser ->
            Pair(tenantUser, checkTenantUser(tenantUser, webClientInfo))
        }
        logSuccessfulLogin(
            userId = authDetail.userId,
            tenantUserResults = tenantUserResults,
            auditActionDetail = auditActionDetail,
            webClientInfo = webClientInfo,
        )

        return LoginResult.Success(
            user = authDetail.user,
            authentication = authDetail,
            tenantUsers = tenantUserResults.map {
                Pair(it.first, when (it.second) {
                    UserAuditLogStatus.SUCCESS -> TenantLoginStatus.SUCCESS
                    UserAuditLogStatus.BLOCKED_USER -> TenantLoginStatus.BLOCKED_USER
                    UserAuditLogStatus.BLOCKED_IP -> TenantLoginStatus.BLOCKED_IP
                    else -> throw UnreachableException()
                })
            },
        )
    }

    fun isRegistrable(method: String): Boolean {
        return credentialService.isRegistrable(method)
    }

    /**
     * Register a new user with authentication credentials
     *
     * @param userTemplate Template for creating a new user with basic information
     * @param method Authentication method (e.g. "email:", "oauth:", etc.)
     * @param username Username for authentication
     * @param credential Raw credential (e.g. password) that will be encoded
     * @param webClientInfo Client information including IP and user agent
     * @return The newly created user
     */
    @Transactional
    fun register(
        userTemplate: UserInput,
        method: String,
        username: String,
        credential: String?,
        webClientInfo: WebClientInfo,
    ): RegisterResult {
        // TODO: register by tenant-invite

        try {
            // Check if authentication already exists
            val existingAuth = findAuthenticationDetail(method, username)
            if (existingAuth != null) {
                val reason = RegisterFailureReason.DUPLICATE_AUTHENTICATION

                logRegister(
                    status = UserAuditLogStatus.FAILURE,
                    detail = UserAuditRegisterActionDetail(
                        method = method,
                        username = username,
                        failureReason = reason,
                    ),
                    webClientInfo = webClientInfo,
                )

                return RegisterResult.Failure(
                    reason = reason,
                )
            }

            // Create new user
            val user = userRepository.save(userTemplate.apply {
                this.roles = setOf(
                    globalRoleRepositoryBase.findById("system.general-user").get(),
                )
            })

            // Create authentication
            val encodedCredential = credential?.let { credentialService.encodeCredential(method, username, it) }

            val authentication = authenticationRepository.save(
                AuthenticationInput(
                    userId = user.id,
                    method = method,
                    username = username,
                    credential = encodedCredential
                )
            )

            logRegister(
                userId = user.id,
                status = UserAuditLogStatus.SUCCESS,
                detail = UserAuditRegisterActionDetail(
                    method = method,
                    username = username,
                ),
                webClientInfo = webClientInfo
            )

            return RegisterResult.Success(
                user = user,
                authentication = authentication,
            )
        } catch (e: Exception) {
            logRegister(
                status = UserAuditLogStatus.FAILURE,
                detail = UserAuditRegisterActionDetail(
                    method = method,
                    username = username,
                    failureReason = null,
                ),
                webClientInfo = webClientInfo,
                errorMessage = e.message,

                )
            throw e
        }
    }

    @Transactional
    fun addLoginMethod(
        userId: String,
        method: String,
        username: String,
        credential: String?,
        webClientInfo: WebClientInfo,
    ): Authentication {
        val user = userRepository.getUserById(userId)
        try {
            // Create authentication
            val encodedCredential = credential?.let { credentialService.encodeCredential(method, username, it) }

            val authentication = authenticationRepository.save(
                AuthenticationInput(
                    userId = user.id,
                    method = method,
                    username = username,
                    credential = encodedCredential
                )
            )

            logOAuth2Link(
                userId = user.id,
                isLinkOrUnlink = true,
                status = UserAuditLogStatus.SUCCESS,
                detail = UserAuditOAuth2LinkActionDetail(
                    method = method,
                    username = username,
                    authenticationId = authentication.id,
                ),
                webClientInfo = webClientInfo
            )

            return authentication
        } catch (e: Exception) {
            logOAuth2Link(
                userId = user.id,
                isLinkOrUnlink = true,
                status = UserAuditLogStatus.FAILURE,
                detail = UserAuditOAuth2LinkActionDetail(
                    method = method,
                    username = username,
                    failureReason = null,
                ),
                webClientInfo = webClientInfo,
                errorMessage = e.message,
            )
            throw e
        }
    }

    fun refresh(
        userId: String,
        authenticationId: String,
        webClientInfo: WebClientInfo,
    ): LoginResult {
        val authDetail = authenticationRepository.findByUserIdAndAuthenticationId(
            userId,
            authenticationId
        ) ?: return LoginResult.Failure(LoginFailureReason.NO_USER)
        val tenantUsers = getTenantUsers(authDetail.userId)
        val tenantUserResults = tenantUsers.map { tenantUser ->
            Pair(tenantUser, checkTenantUser(tenantUser, webClientInfo))
        }
        return LoginResult.Success(
            user = authDetail.user,
            authentication = authDetail,
            tenantUsers = tenantUserResults.map {
                Pair(it.first, when (it.second) {
                    UserAuditLogStatus.SUCCESS -> TenantLoginStatus.SUCCESS
                    UserAuditLogStatus.BLOCKED_USER -> TenantLoginStatus.BLOCKED_USER
                    UserAuditLogStatus.BLOCKED_IP -> TenantLoginStatus.BLOCKED_IP
                    else -> throw UnreachableException()
                })
            },
        )
    }

    private fun getTenantUsers(userId: String): List<TenantUser> =
        tenantUserRepository.findAllByUserId(userId)
            .filter { it.status == UserStatus.ACTIVE }

    private fun checkTenantUser(
        tenantUser: TenantUser,
        webClientInfo: WebClientInfo,
    ): UserAuditLogStatus {
        return when (tenantUser.status) {
            UserStatus.ACTIVE -> {
                // TODO: IP CHECK
                UserAuditLogStatus.SUCCESS
            }
            UserStatus.BLOCKED -> UserAuditLogStatus.BLOCKED_USER
            else -> UserAuditLogStatus.FAILURE
        }
    }

    private fun addAuditLogs(
        userId: String? = null,
        action: UserAuditAction,
        status: UserAuditLogStatus,
        tenantUsers: List<Pair<TenantUser, UserAuditLogStatus?>>,
        auditActionDetail: Any?,
        webClientInfo: WebClientInfo,
    ) {
        auditService.addLogs(buildAuditLogs(
            userId = userId,
            action = action,
            status = status,
            tenantUsers = tenantUsers,
            auditActionDetail = auditActionDetail,
            webClientInfo = webClientInfo,
        ))
    }

    private fun buildAuditLogs(
        userId: String?,
        action: UserAuditAction,
        status: UserAuditLogStatus,
        tenantUsers: List<Pair<TenantUser, UserAuditLogStatus?>>,
        auditActionDetail: Any?,
        webClientInfo: WebClientInfo
    ): List<UserAuditLogInput> {
        val baseLog = UserAuditLogInput(
            userId = userId,
            action = action,
            status = status,
            actionDetail = objectMapper.convertValue(
                auditActionDetail,
                object: TypeReference<Map<String, Any?>>() {},
            ),
            ipAddress = webClientInfo.ipAddress,
            userAgent = webClientInfo.userAgent,
        )

        return buildList {
            add(baseLog)
            tenantUsers.forEach { item ->
                add(
                    UserAuditLogInput(
                    tenantId = item.first.tenantId,
                    action = action,
                    status = item.second ?: status,
                    actionDetail = objectMapper.convertValue(
                        auditActionDetail,
                        object: TypeReference<Map<String, Any?>>() {},
                    ),
                    ipAddress = webClientInfo.ipAddress,
                    userAgent = webClientInfo.userAgent,
                )
                )
            }
        }
    }

    private fun logFailedLogin(
        userId: String?,
        tenantUsers: List<Pair<TenantUser, UserAuditLogStatus?>>?,
        auditActionDetail: UserAuditLoginActionDetail,
        webClientInfo: WebClientInfo,
    ) {
        addAuditLogs(
            userId = userId,
            action = UserAuditAction.LOGIN,
            status = UserAuditLogStatus.FAILURE,
            tenantUsers = tenantUsers ?: emptyList(),
            auditActionDetail = auditActionDetail,
            webClientInfo = webClientInfo
        )
    }

    private fun logBlockedOrInvalidLogin(
        userId: String,
        reason: LoginFailureReason,
        auditActionDetail: UserAuditLoginActionDetail,
        webClientInfo: WebClientInfo
    ) {
        addAuditLogs(
            userId = userId,
            action = UserAuditAction.LOGIN,
            status = when (reason) {
                LoginFailureReason.BLOCKED_USER -> UserAuditLogStatus.BLOCKED_USER
                else -> UserAuditLogStatus.FAILURE
            },
            tenantUsers = emptyList(),
            auditActionDetail = auditActionDetail,
            webClientInfo = webClientInfo
        )
    }

    private fun logSuccessfulLogin(
        userId: String,
        tenantUserResults: List<Pair<TenantUser, UserAuditLogStatus>>,
        auditActionDetail: UserAuditLoginActionDetail,
        webClientInfo: WebClientInfo
    ) {
        addAuditLogs(
            userId = userId,
            action = UserAuditAction.LOGIN,
            status = UserAuditLogStatus.SUCCESS,
            tenantUsers = tenantUserResults,
            auditActionDetail = auditActionDetail,
            webClientInfo = webClientInfo,
        )
    }

    private fun logRegister(
        userId: String? = null,
        status: UserAuditLogStatus,
        detail: UserAuditRegisterActionDetail,
        webClientInfo: WebClientInfo,
        errorMessage: String? = null,
    ) {
        auditService.addLog(
            UserAuditLogInput(
                userId = userId,
                action = UserAuditAction.ACCOUNT_CREATED,
                status = status,
                actionDetail = objectMapper.convertValue(
                    detail,
                    object: TypeReference<Map<String, Any?>>() {},
                ),
                ipAddress = webClientInfo.ipAddress,
                userAgent = webClientInfo.userAgent,
                errorMessage = errorMessage,
            )
        )
    }

    private fun logOAuth2Link(
        userId: String,
        isLinkOrUnlink: Boolean,
        status: UserAuditLogStatus,
        detail: UserAuditOAuth2LinkActionDetail,
        webClientInfo: WebClientInfo,
        errorMessage: String? = null,
    ) {
        auditService.addLog(
            UserAuditLogInput(
                userId = userId,
                action = if (isLinkOrUnlink) UserAuditAction.ACCOUNT_OAUTH2_LINK else UserAuditAction.ACCOUNT_OAUTH2_UNLINK,
                status = status,
                actionDetail = objectMapper.convertValue(
                    detail,
                    object: TypeReference<Map<String, Any?>>() {},
                ),
                ipAddress = webClientInfo.ipAddress,
                userAgent = webClientInfo.userAgent,
                errorMessage = errorMessage,
            )
        )
    }

    fun findAuthenticationDetail(method: String, username: String): AuthenticationDetail? {
        return if (method.startsWith("email:")) {
            authenticationRepository.findByMethodAndEmail(method, username)
        } else {
            authenticationRepository.findByMethodAndUsername(method, username)
        }
    }
}
