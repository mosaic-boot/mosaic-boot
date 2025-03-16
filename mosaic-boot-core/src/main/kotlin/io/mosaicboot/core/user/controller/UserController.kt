package io.mosaicboot.core.user.controller

import io.mosaicboot.core.http.BaseMosaicController
import io.mosaicboot.core.http.MosaicController
import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import io.mosaicboot.core.user.config.MosaicUserProperties
import io.mosaicboot.core.user.controller.model.CurrentUserResponse
import io.mosaicboot.core.user.controller.model.MyTenant
import io.mosaicboot.core.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping

@MosaicController
class UserController(
    private val userService: UserService,
) : BaseMosaicController {
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
            authentication.activeTenantUser,
        )
        return ResponseEntity.ok(
            CurrentUserResponse(
                userId = authentication.userId,
                activeTenantId = authentication.activeTenantUser?.tenantId,
                activeTenantUserId = authentication.activeTenantUser?.tenantUserId,
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
            content = [Content(mediaType = "application/json", schema = Schema(implementation = List::class))]
        )
    ])
    @GetMapping("/current/tenants")
    fun getCurrentTenants(): List<MyTenant> {
        throw RuntimeException("not impl")
    }

    override fun getBaseUrl(applicationContext: ApplicationContext): String {
        val mosaicUserProperties = applicationContext.getBean(MosaicUserProperties::class.java)
        return mosaicUserProperties.api.path
    }
}
