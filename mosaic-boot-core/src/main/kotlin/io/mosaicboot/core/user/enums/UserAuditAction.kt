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

package io.mosaicboot.core.user.enums

enum class UserAuditAction {
    // 인증 관련

    LOGIN,
    LOGOUT,
    PASSWORD_CHANGE_REQUESTED,
    PASSWORD_CHANGED,

    // 계정 관련
    ACCOUNT_CREATED,
    ACCOUNT_UPDATED,
    ACCOUNT_DELETED,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    ACCOUNT_ENABLED,
    ACCOUNT_DISABLED,

    // 프로필 관련
    PROFILE_UPDATED,
    EMAIL_UPDATED,
    PHONE_UPDATED,

    // 권한 관련
    ROLE_GRANTED,
    ROLE_REVOKED,
    PERMISSION_GRANTED,
    PERMISSION_REVOKED,

    // 기타
    TENANT_CREATED,
    TOKEN_REFRESHED,
    API_ACCESS,
    SETTINGS_UPDATED
}
