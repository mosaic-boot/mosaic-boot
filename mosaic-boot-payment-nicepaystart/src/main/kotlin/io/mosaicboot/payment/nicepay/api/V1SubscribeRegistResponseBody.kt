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

/**
 * 빌키 발급 응답 데이터 클래스
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class V1SubscribeRegistResponseBody(
    /**
     * 결제결과코드
     * 0000 : 성공 / 그외 실패
     * 필수 | 4 Byte
     */
    @JsonProperty("resultCode")
    val resultCode: String,

    /**
     * 결제결과메시지
     * 예시) 빌키가 정상적으로 생성되었습니다.
     * 필수 | 100 Byte
     */
    @JsonProperty("resultMsg")
    val resultMsg: String,

    /**
     * 거래번호
     * 거래를 구분하는 transaction ID
     * 예시) nictest00m01011104191651325596
     * 필수 | 30 Byte
     */
    @JsonProperty("tid")
    val tid: String,

    /**
     * 상점 거래 고유번호
     * 필수 | 64 Byte
     */
    @JsonProperty("orderId")
    val orderId: String,

    /**
     * 빌키
     * NICEPAY가 발급하는 빌링 아이디
     * 가맹점 DB 저장하여, 빌링승인 API 요청시 전달
     * - 회원 카드 정보와 매핑되어 관리되는 Key 값으로, 빌키승인 API 호출시 전달
     * 예시) BIKYnictest00m1104191651325596
     * 선택 | 30 Byte
     */
    @JsonProperty("bid")
    var bid: String? = null,

    /**
     * 인증일자
     * ISO 8601 형식
     * 선택
     */
    @JsonProperty("authDate")
    val authDate: String? = null,

    /**
     * 카드사 코드
     * 신용카드사별 코드
     * 선택 | 3 Byte
     */
    @JsonProperty("cardCode")
    val cardCode: String? = null,

    /**
     * 카드사 이름
     * 예시) "삼성"
     * 선택 | 20 Byte
     */
    @JsonProperty("cardName")
    val cardName: String? = null
)