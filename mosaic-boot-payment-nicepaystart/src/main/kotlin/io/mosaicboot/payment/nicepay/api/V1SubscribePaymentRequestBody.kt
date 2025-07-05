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

data class V1SubscribePaymentRequestBody(
    /**
     * 상점 거래 고유번호
     * 가맹점에서 관리하는 Unique한 주문번호 또는 결제번호
     * 결제된 orderId로 재호출 불가
     * 필수 | 64 Byte
     */
    @field:JsonProperty("orderId")
    val orderId: String,

    /**
     * 결제 금액
     * 필수 | 12 Byte
     */
    @field:JsonProperty("amount")
    val amount: Int,

    /**
     * 상품명
     * 필수 | 40 Byte
     */
    @field:JsonProperty("goodsName")
    val goodsName: String,

    /**
     * 할부개월
     * 0:일시불, 2:2개월, 3:3개월 …
     * 필수 | 2 Byte
     */
    @field:JsonProperty("cardQuota")
    val cardQuota: String,

    /**
     * 상점분담무이자 사용여부 (현재 false만 사용 가능)
     * false : 유이자
     * 필수
     */
    @field:JsonProperty("useShopInterest")
    val useShopInterest: Boolean,

    /**
     * 구매자 이름
     * 선택 | 30 Byte
     */
    @field:JsonProperty("buyerName")
    val buyerName: String? = null,

    /**
     * 구매자 전화번호
     * 하이픈(-) 없이 숫자만 입력
     * 선택 | 20 Byte
     */
    @field:JsonProperty("buyerTel")
    val buyerTel: String? = null,

    /**
     * 구매자 이메일
     * 선택 | 60 Byte
     */
    @field:JsonProperty("buyerEmail")
    val buyerEmail: String? = null,

    /**
     * 면세공급가액
     * 전체 거래금액(amount)중에서 면세에 해당하는 금액을 설정합니다.
     * 선택 | 12 Byte
     */
    @field:JsonProperty("taxFreeAmt")
    val taxFreeAmt: Int? = null,

    /**
     * 상점 정보 전달용 예비필드
     *
     * 승인응답 또는 webhook 시점에 요청 원문을 전달 합니다.
     * JSON string format으로 이용하시기를 권고 드립니다.
     * 단, 큰따옴표(")는 이용불가
     * 선택 | 500 Byte
     */
    @field:JsonProperty("mallReserved")
    val mallReserved: String? = null,

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
    var signData: String? = null,

    /**
     * 응답파라메터 인코딩 방식
     * 가맹점 서버의 encoding 방식 전달
     * 예시) utf-8(Default) / euc-kr
     * 선택 | 10 Byte
     */
    @field:JsonProperty("returnCharSet")
    val returnCharSet: String = "utf-8"
) {
    fun withSign(bid: String, secretKey: String): V1SubscribePaymentRequestBody {
        this.signData = ApiUtil.makeSignature(
            "${orderId}${bid}${ediDate}",
            secretKey
        )
        return this
    }
}