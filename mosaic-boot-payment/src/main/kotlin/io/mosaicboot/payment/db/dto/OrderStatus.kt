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

package io.mosaicboot.payment.db.dto

import com.fasterxml.jackson.annotation.JsonProperty

enum class OrderStatus(
    val value: String,
) {
    /**
     * 결제 대기 중
     */
    @JsonProperty("waiting")
    WAITING("waiting"),

    /**
     * 입금 대기 중
     */
    @JsonProperty("processing")
    PROCESSING("processing"),

    /**
     * 결제 완료
     */
    @JsonProperty("paid")
    PAID("paid"),

    /**
     * 결제 이후 취소됨
     */
    @JsonProperty("cancelled")
    CANCELLED("cancelled"),

    /**
     * 결제 이후 취소 완료됨
     */
    @JsonProperty("refunded")
    REFUNDED("refunded"),

    /**
     * 처리 완료 (non-order)
     */
    @JsonProperty("completed")
    COMPLETED("completed"),

    /**
     * 결제 실패 (order or non-order)
     */
    @JsonProperty("failure")
    FAILURE("failure"),

    /**
     * 시스템 오류 (order or non-order)
     */
    @JsonProperty("error")
    ERROR("error"),
    ;
}