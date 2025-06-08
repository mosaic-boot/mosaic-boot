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
data class V1PaymentResponseBody(
    /** 결제결과코드 (0000: 성공, 그외 실패) */
    @field:JsonProperty("resultCode")
    override val resultCode: String,

    /** 결제결과메시지 */
    @field:JsonProperty("resultMsg")
    override val resultMsg: String,

    /** 결제 승인 키 */
    @field:JsonProperty("tid")
    val tid: String,

    /** 취소 거래 키 */
    @field:JsonProperty("cancelledTid")
    val cancelledTid: String? = null,

    /** 상점 거래 고유번호 */
    @field:JsonProperty("orderId")
    val orderId: String,

    /** 응답전문생성일시 ISO 8601 형식 */
    @field:JsonProperty("ediDate")
    val ediDate: String? = null,

    /** 위변조 검증 데이터 */
    @field:JsonProperty("signature")
    val signature: String? = null,

    /** 결제 처리상태 */
    @field:JsonProperty("status")
    override val status: String,

    /** 결제완료시점 ISO 8601 형식 */
    @field:JsonProperty("paidAt")
    val paidAt: String,

    /** 결제실패시점 ISO 8601 형식 */
    @field:JsonProperty("failedAt")
    val failedAt: String,

    /** 결제취소시점 ISO 8601 형식 */
    @field:JsonProperty("cancelledAt")
    val cancelledAt: String,

    /** 결제수단 */
    @field:JsonProperty("payMethod")
    val payMethod: String,

    /** 결제 금액 */
    @field:JsonProperty("amount")
    val amount: Int,

    /** 취소 가능 잔액 */
    @field:JsonProperty("balanceAmt")
    val balanceAmt: Int,

    /** 상품명 */
    @field:JsonProperty("goodsName")
    val goodsName: String,

    /** 상점 정보 전달용 예비필드 */
    @field:JsonProperty("mallReserved")
    val mallReserved: String? = null,

    /** 에스크로 거래 여부 */
    @field:JsonProperty("useEscrow")
    val useEscrow: Boolean,

    /** 결제승인화폐단위 */
    @field:JsonProperty("currency")
    val currency: String,

    /** 결제 채널 */
    @field:JsonProperty("channel")
    val channel: String? = null,

    /** 제휴사 승인 번호 */
    @field:JsonProperty("approveNo")
    val approveNo: String? = null,

    /** 구매자 명 */
    @field:JsonProperty("buyerName")
    val buyerName: String? = null,

    /** 구매자 전화번호 */
    @field:JsonProperty("buyerTel")
    val buyerTel: String? = null,

    /** 구매자 이메일 */
    @field:JsonProperty("buyerEmail")
    val buyerEmail: String? = null,

    /** 현금영수증 발급여부 */
    @field:JsonProperty("issuedCashReceipt")
    val issuedCashReceipt: Boolean? = null,

    /** 매출전표 확인 URL */
    @field:JsonProperty("receiptUrl")
    val receiptUrl: String? = null,

    /** 상점에서 관리하는 사용자 아이디 */
    @field:JsonProperty("mallUserId")
    val mallUserId: String? = null,


    /** 할인 정보 */
    @field:JsonProperty("coupon")
    val coupon: PaymentDetails.Coupon? = null,

    /** 카드 정보 */
    @field:JsonProperty("card")
    val card: PaymentDetails.Card? = null,

    /** 은행 정보 */
    @field:JsonProperty("bank")
    val bank: PaymentDetails.Bank? = null,

    /** 가상계좌 정보 */
    @field:JsonProperty("vbank")
    override val vbank: PaymentDetails.VirtualBank? = null,
) : V1PaymentResultBase