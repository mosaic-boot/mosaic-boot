package io.mosaicboot.core.tenant.controller

import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.core.tenant.config.MosaicTenantProperties
import io.mosaicboot.core.tenant.controller.dto.*
import io.mosaicboot.core.tenant.service.TenantService
import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.permission.annotation.RequirePermission
import io.mosaicboot.core.permission.aspect.PermissionInterceptor
import io.swagger.v3.oas.annotations.Operation
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@MosaicController
class TenantsController(
    private val mosaicTenantProperties: MosaicTenantProperties,
    private val tenantService: TenantService,
) : BaseMosaicController {
    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        return mosaicTenantProperties.api.path
    }

    @PostMapping("/")
    @RequirePermission(
        permission = "tenant.create",
        tenantSpecific = false,
    )
    @Operation(summary = "Create new tenant")
    fun createTenant(
        authentication: Authentication,
        @RequestBody request: CreateTenantRequest
    ): ResponseEntity<TenantResponse> {
        if (authentication !is MosaicAuthenticatedToken) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build()
        }

        val tenant = tenantService.createTenant(
            userId = authentication.userId,
            name = request.name,
            timeZone = request.timeZone,
        )
        return ResponseEntity.ok(
            TenantResponse(
                id = tenant.id,
                name = tenant.name,
                status = tenant.status,
                timeZone = tenant.timeZone,
            )
        )
    }

    @GetMapping("/{tenantId}")
    @RequirePermission(
        permission = "global.admin",
        tenantSpecific = false,
    )
    @RequirePermission(
        permission = "",
        tenantSpecific = true,
    )
    @Operation(summary = "Get tenant details")
    fun getTenant(
        authentication: Authentication,
        @PathVariable("tenantId") tenantId: String,
    ): ResponseEntity<TenantResponse> {
        PermissionInterceptor.mustAuthorized()

        val tenant = tenantService.getTenant(tenantId)

        return ResponseEntity.ok(
            TenantResponse(
            id = tenant.id,
            name = tenant.name,
            timeZone = tenant.timeZone,
            status = tenant.status,
        )
        )
    }

    @PutMapping("/{tenantId}")
    @RequirePermission(
        permission = "tenant.admin",
        tenantSpecific = true,
    )
    @Operation(summary = "Update tenant")
    fun updateTenant(
        authentication: Authentication,
        @PathVariable tenantId: String,
        @RequestBody request: UpdateTenantRequest,
    ): ResponseEntity<TenantResponse> {
        PermissionInterceptor.mustAuthorized()

        var tenant = tenantService.getTenant(tenantId)
        tenant.name = request.name
        tenant.timeZone = request.timeZone

        tenant = tenantService.update(tenant)

        return ResponseEntity.ok(
            TenantResponse(
            id = tenant.id,
            name = tenant.name,
            status = tenant.status,
            timeZone = tenant.timeZone,
        )
        )
    }

    @PostMapping("/{tenantId}/invite")
    @RequirePermission(
        permission = "tenant.invite",
        tenantSpecific = true,
    )
    @Operation(summary = "Invite user to tenant")
    fun inviteUser(
        authentication: Authentication,
        @PathVariable tenantId: String,
        @RequestBody request: InviteUserRequest,
    ): ResponseEntity<InviteResponse> {
        PermissionInterceptor.mustAuthorized()

//        tenantService.inviteUser()

        // 여기에 초대 로직 구현
        // 1. 초대 코드 생성
        // 2. 이메일 발송
        // 3. 초대 정보 저장

        return ResponseEntity.ok(
            InviteResponse(
            inviteCode = "generated-code",
            email = request.email
        )
        )
    }
//
//    private fun hasAccess(authentication: Authentication, tenantId: String): Boolean {
//        if (authentication !is MosaicAuthenticatedToken) return false
//        return tenantUserRepository.findByUserIdAndTenantId(
//            authentication.userId,
//            tenantId
//        ) != null
//    }
//
//    private fun hasAdminAccess(authentication: Authentication, tenantId: String): Boolean {
//        if (authentication !is MosaicAuthenticatedToken) return false
//        val tenantUser = tenantUserRepository.findByUserIdAndTenantId(
//            authentication.userId,
//            tenantId
//        ) ?: return false
//        return tenantUser.role == "ADMIN"
//    }
}