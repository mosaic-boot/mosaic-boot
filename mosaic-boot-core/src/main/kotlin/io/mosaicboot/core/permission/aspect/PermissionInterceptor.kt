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

package io.mosaicboot.core.permission.aspect

import io.mosaicboot.core.permission.annotation.RequirePermission
import io.mosaicboot.core.permission.exception.PermissionDeniedException
import io.mosaicboot.core.permission.service.PermissionService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.HandlerMapping

@Aspect
@Component
class PermissionInterceptor(
    private val permissionService: PermissionService,
) {
    @Before("@annotation(io.mosaicboot.core.permission.annotation.RequirePermission)")
    fun checkPermission(joinPoint: ProceedingJoinPoint) {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val annotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(method, RequirePermission::class.java)
        if (annotations.isEmpty()) {
            throw IllegalStateException("RequirePermission annotation not found")
        }

        val authorizationContext = AuthorizationContext.get()
        val authentication = authorizationContext.getAuthentication()

        var hasPermission: Boolean = false
        for (annotation in annotations) {
            val tenantId = if (annotation.tenantSpecific) {
                extractTenantId() ?: throw PermissionDeniedException("could not find tenantId")
            } else null

            val cacheKey = AuthorizationContext.AuthorizeCache(
                permission = annotation.permission,
                tenantSpecific = annotation.tenantSpecific,
                tenantId = tenantId,
            )
            val alreadyAuthorized = authorizationContext.authorizeCache.contains(cacheKey)
            if (alreadyAuthorized) {
                return
            }

            val hasPermissionCurrent = permissionService.checkPermission(
                authentication = authentication,
                permission = annotation.permission,
                tenantId = tenantId,
            )
            if (hasPermissionCurrent) {
                authorizationContext.authorizeCache.add(cacheKey)
            }
            hasPermission = hasPermission || hasPermissionCurrent
        }

        if (!hasPermission) {
            throw PermissionDeniedException("permission denied")
        }
    }

    /**
     * Extract tenantId from RequestContext
     */
    private fun extractTenantId(): String? {
        val templateVariables = RequestContextHolder.getRequestAttributes()
            ?.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)
            as? Map<String, String>
        return templateVariables?.get("tenantId")
    }


    companion object {
        fun mustAuthorized() {
            val authorizationContext = AuthorizationContext.find()
            if (authorizationContext?.authorized != true) {
                throw PermissionDeniedException("no authorized")
            }
        }
    }
}
