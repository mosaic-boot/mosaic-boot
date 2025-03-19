package io.mosaicboot.core.tenant.service

import io.mosaicboot.core.tenant.config.MosaicTenantProperties
import io.mosaicboot.core.tenant.dto.InviteResult
import io.mosaicboot.core.tenant.repository.TenantRepositoryBase
import io.mosaicboot.core.user.dto.TenantUserInput
import io.mosaicboot.core.user.entity.TenantRole
import io.mosaicboot.core.user.entity.TenantUser
import io.mosaicboot.core.user.enums.UserStatus
import io.mosaicboot.core.user.repository.TenantRoleRepositoryBase
import io.mosaicboot.core.user.repository.TenantUserRepositoryBase
import io.mosaicboot.core.user.service.AuditService
import io.mosaicboot.core.user.service.UserService
import io.mosaicboot.core.util.PagedResult
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class TenantUserService(
    private val mosaicTenantProperties: MosaicTenantProperties,
    private val userService: UserService,
    private val auditService: AuditService,
    private val tenantRepository: TenantRepositoryBase<*>,
    private val tenantUserRepository: TenantUserRepositoryBase<*>,
    private val tenantRoleRepository: TenantRoleRepositoryBase<*>,
) {
    fun getRoleById(id: String): TenantRole {
        return tenantRoleRepository.findById(id).get()
    }

    fun findTenantUser(
        tenantId: String,
        tenantUserId: String,
    ): TenantUser? {
        return tenantUserRepository.findByTenantIdAndId(tenantId, tenantUserId)
    }

    fun inviteUser(
        performedBy: TenantUser,
        email: String,
        roles: List<TenantRole>,
    ): InviteResult {
        val targetUser = userService.findUserByEmail(
            email
        ) ?: return InviteResult.UserNotExists

        tenantUserRepository.findByTenantIdAndUserId(
            performedBy.tenantId,
            targetUser.id,
        )?.let {
            return InviteResult.AlreadyExists(it)
        }

        val tenantUser = tenantUserRepository.save(
            TenantUserInput(
                tenantId = performedBy.tenantId,
                userId = targetUser.id,
                nickname = targetUser.name,
                email = email,
                status = UserStatus.ACTIVE,
                roles = roles,
            )
        )
        return InviteResult.Success(tenantUser)
    }

    fun getTenantUsers(
        tenantId: String,
        page: Int,
        size: Int
    ): PagedResult<TenantUser> {
        return tenantUserRepository.findAllByTenantId(
            tenantId,
            PageRequest.of(page, size)
        ).let {
            PagedResult(
                items = it.content,
                total = it.totalElements,
            )
        }
    }
}