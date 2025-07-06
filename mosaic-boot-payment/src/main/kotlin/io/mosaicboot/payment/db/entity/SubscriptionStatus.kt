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

package io.mosaicboot.payment.db.entity

enum class SubscriptionStatus {
    /**
     * Active and in good standing.
     */
    ACTIVE,

    /**
     * The subscription has been canceled and is no longer active.
     */
    CANCELED,

    /**
     * Payment has failed, and the subscription is temporarily inactive.
     */
    PAST_DUE,

    /**
     * A plan change is scheduled for the end of the current billing cycle.
     */
    PENDING_CHANGE,

    /**
     * The subscription is scheduled to be canceled at the end of the current billing cycle.
     */
    PENDING_CANCEL,
}
