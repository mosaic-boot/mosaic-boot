package io.mosaicboot.core.user.controller

import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.auth.controller.AuthControllerHelper
import io.mosaicboot.core.tenant.service.TenantService
import io.mosaicboot.core.user.config.MosaicUserProperties
import io.mosaicboot.core.user.controller.dto.ActiveTenantUser
import io.mosaicboot.core.user.controller.dto.CurrentUserResponse
import io.mosaicboot.core.user.controller.dto.MyTenant
import io.mosaicboot.core.user.controller.dto.SwitchActiveTenantRequest
import io.mosaicboot.core.user.controller.dto.UpdateUserRequest
import io.mosaicboot.core.user.service.UserService
import io.mosaicboot.core.util.WebClientInfo
import io.mosaicboot.core.auth.MosaicAuthenticationHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@MosaicController
class UserController(
    private val userService: UserService,
    private val tenantService: TenantService,
    private val authControllerHelper: AuthControllerHelper,
    private val mosaicAuthenticationHandler: MosaicAuthenticationHandler,
) : BaseMosaicController {
    companion object {
        private val log = LoggerFactory.getLogger(UserController::class.java)
    }

    @Operation(
        summary = "Get current user information",
        description = "Retrieve information about the currently authenticated user"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Current user information retrieved successfully",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = CurrentUserResponse::class))]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        )
    ])
    @GetMapping("/current")
    fun getCurrentUser(
        authentication: Authentication,
    ): ResponseEntity<CurrentUserResponse> {
        if (authentication !is MosaicAuthenticatedToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val currentActiveUser = userService.getCurrentActiveUser(
            authentication.userId,
            authentication.activeTenantId?.let { activeTenantId ->
                ActiveTenantUser(
                    tenantId = activeTenantId,
                    tenantUserId = authentication.tenants[activeTenantId]!!.userId,
                )
            },
        )
        return ResponseEntity.ok(
            CurrentUserResponse(
                userId = authentication.userId,
                activeTenantId = authentication.activeTenantId,
                name = currentActiveUser.user.name,
                email = currentActiveUser.user.email,
                permissions = emptyList(),
            )
        )
    }

    @Operation(
        summary = "Get user tenants",
        description = "Retrieve list of tenants for the current user"
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Tenant list retrieved successfully",
            content = [
                Content(mediaType = "application/json", array = ArraySchema(
                    schema = Schema(implementation = MyTenant::class)
                ))
            ]
        )
    ])
    @GetMapping("/current/tenants")
    fun getCurrentTenants(
        authentication: Authentication,
    ): ResponseEntity<List<MyTenant>> {
        if (authentication !is MosaicAuthenticatedToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val tenants = tenantService.getTenants(authentication.tenants.values.map { it.id })
            .associateBy { it.id }

        return ResponseEntity.ok(
            authentication.tenants.values.mapNotNull {
                runCatching {
                    MyTenant(
                        tenantId = it.id,
                        tenantUserId = it.userId,
                        tenantName = tenants[it.id]!!.name,
                        status = it.status,
                    )
                }
                    .onFailure { ex ->
                        log.error("error", ex)
                    }
                    .getOrNull()
            }
        )
    }

    @PostMapping("/current/tenant")
    @Operation(
        summary = "switch active tenant"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "successfully",
            ),
        ]
    )
    fun switchActiveTenant(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @Parameter(hidden = true) webClientInfo: WebClientInfo,
        authentication: Authentication,
        @RequestBody requestBody: SwitchActiveTenantRequest,
    ): ResponseEntity<Any> {
        if (authentication !is MosaicAuthenticatedToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        if (authentication.tenants[requestBody.tenantId] == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        authentication.activeTenantId = requestBody.tenantId
        authControllerHelper.refresh(
            request, response, webClientInfo, authentication
        )
        return ResponseEntity.ok().build()
    }

    @PutMapping("/current")
    @Operation(
        summary = "Update current user information",
        description = "Update name, timeZone, and email of the current user"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User information updated successfully",
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized access"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid input"
            )
        ]
    )
    fun updateCurrentUser(
        @Parameter(hidden = true) webClientInfo: WebClientInfo,
        authentication: Authentication,
        @RequestBody requestBody: UpdateUserRequest,
    ): ResponseEntity<Any> {
        if (authentication !is MosaicAuthenticatedToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        
        userService.updateUser(authentication.userId, requestBody, webClientInfo)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/current")
    @Operation(
        summary = "Delete current user account",
        description = "Mark current user account as deleted"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User account deleted successfully",
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized access"
            )
        ]
    )
    fun deleteCurrentUser(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @Parameter(hidden = true) webClientInfo: WebClientInfo,
        authentication: Authentication,
    ): ResponseEntity<Any> {
        if (authentication !is MosaicAuthenticatedToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        
        userService.deleteUser(authentication.userId, webClientInfo)
        
        // 회원 탈퇴 후 로그아웃 처리
        mosaicAuthenticationHandler.logout(request, response, authentication)
        
        return ResponseEntity.ok().build()
    }

    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        val mosaicUserProperties = applicationContext.getBean(MosaicUserProperties::class.java)
        return mosaicUserProperties.api.path
    }
}
