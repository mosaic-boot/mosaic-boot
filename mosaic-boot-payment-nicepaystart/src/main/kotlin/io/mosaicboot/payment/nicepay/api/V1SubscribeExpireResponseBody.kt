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

package io.mosaicboot.payment.nicepay.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class V1SubscribeExpireResponseBody(
    /** 결제결과코드 (0000: 성공, 그외 실패) */
    @JsonProperty("resultCode")
    val resultCode: String,

    /** 결제결과메시지 */
    @JsonProperty("resultMsg")
    val resultMsg: String,

    /** 결제 승인 키 */
    @JsonProperty("tid")
    val tid: String,

    /** 상점 거래 고유번호 */
    @JsonProperty("orderId")
    val orderId: String,

    /** 빌키 NICEPAY가 발급한 빌링 아이디*/
    @JsonProperty("bid")
    val bid: String? = null,

    /** ISO 8601 형식. 처리에 성공한경우 리턴됩니다. */
    @JsonProperty("authDate")
    val authDate: String? = null,
)