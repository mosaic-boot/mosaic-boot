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
 * 서버 승인 모델 응답 데이터 클래스
 * 결제 인증 결과에 대한 상세 정보를 담고 있습니다.
 *
 * AuthResponse(authResultCode=0000, authResultMsg=인증 성공, tid=UT0018460m03012501281431234567, clientId=S2_adb6c1dfeaff12293d466b78860b2a53, orderId=adef37a4-7c2a-44c7-8e4c-613c3f59dd10, amount=9900, mallReserved=null, authToken=NICEUNTT647566595E285F8230D2B38192E097CE, signature=40bf2f707f66115dd5366b9590df0ca44747a8536f61813394564d2c07e5c6a3)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthResponse(
    /**
     * 인증결과 코드
     * - 0000: 인증 성공
     * - 그 외: 인증 실패
     * 길이: 4 Byte
     */
    @field:JsonProperty("authResultCode")
    val authResultCode: String? = null,

    /**
     * 인증결과 메시지
     * 길이: 500 Byte
     */
    @field:JsonProperty("authResultMsg")
    val authResultMsg: String? = null,

    /**
     * 결제 인증 키 (결제 승인을 위한 키값)
     * - 인증 성공 시에만 리턴
     * 길이: 30 Byte
     */
    @field:JsonProperty("tid")
    val tid: String? = null,

    /**
     * 가맹점 식별코드 (NICEPAY 발급)
     * 길이: 50 Byte
     */
    @field:JsonProperty("clientId")
    val clientId: String? = null,

    /**
     * 상점 거래 고유번호
     * 길이: 64 Byte
     */
    @field:JsonProperty("orderId")
    val orderId: String,

    /**
     * 결제 금액
     * 길이: 12 Byte
     */
    @field:JsonProperty("amount")
    val amount: Long? = null,

    /**
     * 상점 예약필드 (인증 요청 시 전달된 값 리턴)
     * 길이: 500 Byte
     */
    @field:JsonProperty("mallReserved")
    val mallReserved: String? = null,

    /**
     * 인증 토큰
     * 길이: 40 Byte
     */
    @field:JsonProperty("authToken")
    val authToken: String? = null,

    /**
     * 위변조 검증 데이터
     * - 생성규칙: hex(sha256(authToken + clientId + amount + SecretKey))
     * 길이: 256 Byte
     */
    @field:JsonProperty("signature")
    val signature: String? = null
) {
    fun verifySignature(
        secretKey: String
    ): Boolean {
        val signatureData = "$authToken$clientId$amount"
        return ApiUtil.verifySignature(signatureData, signature, secretKey)
    }
}