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

import io.mosaicboot.core.permission.exception.PermissionDeniedException
import io.mosaicboot.core.auth.MosaicAuthenticatedToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

class AuthorizationContext {
    var authorized: Boolean = false
    val authorizeCache = HashSet<AuthorizeCache>()

    private var cachedAuthentication: MosaicAuthenticatedToken? = null

    fun mustAuthorized() {
        if (!authorized) {
            throw PermissionDeniedException("no authorized")
        }
    }

    fun getAuthentication(): MosaicAuthenticatedToken {
        return cachedAuthentication ?: let {
            val authentication = SecurityContextHolder.getContext().authentication as? MosaicAuthenticatedToken
                ?: throw PermissionDeniedException("unauthorized")
            this.cachedAuthentication = authentication
            authentication
        }
    }

    class AuthorizeCache(
        val permission: String,
        val tenantSpecific: Boolean,
        val tenantId: String?,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AuthorizeCache

            if (tenantSpecific != other.tenantSpecific) return false
            if (permission != other.permission) return false
            if (tenantId != other.tenantId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = tenantSpecific.hashCode()
            result = 31 * result + permission.hashCode()
            result = 31 * result + (tenantId?.hashCode() ?: 0)
            return result
        }
    }

    companion object {
        private val KEY = AuthorizationContext::class.java.name

        fun get(): AuthorizationContext {
            val attributes = RequestContextHolder.getRequestAttributes()
                ?: throw IllegalStateException("not in request context")
            var context = attributes.getAttribute(KEY, RequestAttributes.SCOPE_REQUEST) as? AuthorizationContext
            if (context != null) {
                return context
            }
            context = AuthorizationContext()
            attributes.setAttribute(KEY, context, RequestAttributes.SCOPE_REQUEST)
            return context
        }

        fun find(): AuthorizationContext? {
            return RequestContextHolder.getRequestAttributes()
                ?.getAttribute(KEY, RequestAttributes.SCOPE_REQUEST) as? AuthorizationContext
                ?: throw IllegalStateException("not in request context")
        }
    }
}