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

import com.fasterxml.jackson.annotation.JsonProperty

data class V1SubscribeExpireRequestBody(
    /**
     * 상점 거래 고유번호
     * 가맹점에서 관리하는 Unique한 주문번호 또는 결제번호
     * 결제된 orderId로 재호출 불가
     * 필수 | 64 Byte
     */
    @field:JsonProperty("orderId")
    val orderId: String,

    /**
     * 전문생성일시
     * ISO 8601 형식
     * 선택
     */
    @field:JsonProperty("ediDate")
    val ediDate: String? = null,

    /**
     * 위변조 검증 Data
     * 생성규칙 : hex(sha256(orderId + bid + ediDate + SecretKey))
     * - SecretKey는 가맹점관리자에 로그인 하여 확인 가능합니다.
     * 선택 | 256 Byte
     */
    @field:JsonProperty("signData")
    val signData: String? = null,

    /**
     * 응답파라메터 인코딩 방식
     * 가맹점 서버의 encoding 방식 전달
     * 예시) utf-8(Default) / euc-kr
     * 선택 | 10 Byte
     */
    @field:JsonProperty("returnCharSet")
    val returnCharSet: String = "utf-8"
)