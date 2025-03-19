package io.mosaicboot.core.tenant.controller

import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.core.tenant.config.MosaicTenantProperties
import io.mosaicboot.core.tenant.controller.dto.*
import io.mosaicboot.core.tenant.service.TenantService
import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.auth.controller.AuthControllerHelper
import io.mosaicboot.core.permission.annotation.RequirePermission
import io.mosaicboot.core.permission.aspect.AuthorizationContext
import io.mosaicboot.core.permission.aspect.PermissionInterceptor
import io.mosaicboot.core.tenant.dto.InviteResult
import io.mosaicboot.core.tenant.service.TenantUserService
import io.mosaicboot.core.user.service.UserService
import io.mosaicboot.core.util.WebClientInfo
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@MosaicController
class TenantsController(
    private val mosaicTenantProperties: MosaicTenantProperties,
    private val tenantService: TenantService,
    private val tenantUserService: TenantUserService,
    private val authControllerHelper: AuthControllerHelper,
) : BaseMosaicController {
    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        return mosaicTenantProperties.api.path
    }

    @Operation(summary = "Create new tenant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = TenantResponse::class))]
            ),
        ]
    )
    @RequirePermission(
        permission = "tenant.create",
        tenantSpecific = false,
    )
    @PostMapping("/")
    fun createTenant(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @Parameter(hidden = true) webClientInfo: WebClientInfo,
        authentication: Authentication,
        @RequestBody requestBody: CreateTenantRequest
    ): ResponseEntity<TenantResponse> {
        if (authentication !is MosaicAuthenticatedToken) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build()
        }

        val (tenant, _) = tenantService.createTenant(
            webClientInfo = webClientInfo,
            userId = authentication.userId,
            name = requestBody.name,
            timeZone = requestBody.timeZone,
        )
        authentication.activeTenantId = tenant.id
        authControllerHelper.refresh(
            request, response, webClientInfo, authentication
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
        authorizationContext: AuthorizationContext,
    ): ResponseEntity<InviteResponse> {
        PermissionInterceptor.mustAuthorized()
        authentication as MosaicAuthenticatedToken

        val tenantUser = authentication.tenants[tenantId]?.let {
            tenantUserService.findTenantUser(
                tenantId,
                it.userId,
            )
        } ?: return ResponseEntity.badRequest().build()

        val result = tenantUserService.inviteUser(
            tenantUser,
            request.email,
            listOf(),
        )
        return when (result) {
            is InviteResult.Success -> ResponseEntity.ok(
                InviteResponse(result = InviteResultCode.SUCCESS)
            )
            InviteResult.UserNotExists -> ResponseEntity.ok(
                InviteResponse(result = InviteResultCode.USER_NOT_EXISTS)
            )
            is InviteResult.AlreadyExists -> ResponseEntity.ok(
                InviteResponse(result = InviteResultCode.ALREADY_EXISTS)
            )
        }
    }

    @GetMapping("/{tenantId}/users")
    @RequirePermission(
        permission = "tenant.users.view",
        tenantSpecific = true,
    )
    @Operation(summary = "Get tenant users with pagination")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved tenant users",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TenantUserListResponse::class)
                )]
            ),
        ]
    )
    fun getTenantUsers(
        authentication: Authentication,
        @PathVariable tenantId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<TenantUserListResponse> {
        PermissionInterceptor.mustAuthorized()
        authentication as MosaicAuthenticatedToken

        val tenantUser = authentication.tenants[tenantId]?.let {
            tenantUserService.findTenantUser(
                tenantId,
                it.userId,
            )
        } ?: return ResponseEntity.badRequest().build()

        // 페이지네이션된 테넌트 사용자 목록 조회
        val users = tenantUserService.getTenantUsers(
            tenantId = tenantId,
            page = page,
            size = size
        )

        return ResponseEntity.ok(
            TenantUserListResponse(
                items = users.items.map { user ->
                    TenantUserResponse(
                        userId = user.userId,
                        nickname = user.nickname,
                        email = user.email ?: "",
                        status = user.status,
                        roles = user.roles.map { it.name }
                    )
                },
                total = users.total.toInt(),
                page = page,
                size = size
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