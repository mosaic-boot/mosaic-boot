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
 * https://github.com/nicepayments/nicepay-manual/blob/16d1db6abae388dcfabcb11813ab6dd77566451a/api/hook.md
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentNotificationResponse(
    /**
     * 결제결과코드
     * 0000 : 성공 / 그외 실패
     * 필수 | 4 Byte
     */
    @field:JsonProperty("resultCode")
    override val resultCode: String,

    /**
     * 결제결과메시지
     * 필수 | 100 Byte
     */
    @field:JsonProperty("resultMsg")
    override val resultMsg: String,

    /**
     * 결제 승인 키
     * 최초 승인(가상계좌-채번)에 성공한 원거래의 NICEPAY 거래키 입니다.
     * 필수 | 30 Byte
     */
    @field:JsonProperty("tid")
    val tid: String,

    /**
     * 취소 거래 키
     * NICEPAY가 발행하는 취소 응답 TID (부분취소시 tid와 다른 값이 응답됨)
     * - 취소 요청건에 한하여 응답됨
     * - cancels 객체에서 현재 취소된 거래정보를 찾을 때 사용 하시면 됩니다.
     * 선택 | 30 Byte
     */
    @field:JsonProperty("cancelledTid")
    val cancelledTid: String? = null,

    /**
     * 상점 거래 고유번호
     * 필수 | 64 Byte
     */
    @field:JsonProperty("orderId")
    val orderId: String,

    /**
     * 응답전문생성일시 ISO 8601 형식
     * 필수
     */
    @field:JsonProperty("ediDate")
    val ediDate: String,

    /**
     * 위변조 검증 데이터
     * - 유효한 거래건에 한하여 응답
     * - 생성규칙 : hex(sha256(tid + amount + ediDate+ SecretKey))
     * - 데이터 유효성 검증을 위해, 가맹점 수준에서 비교하는 로직 구현 권고
     * - SecretKey는 가맹점관리자에 로그인 하여 확인 가능합니다.
     * 선택 | 256 Byte
     */
    @field:JsonProperty("signature")
    val signature: String? = null,

    /**
     * 결제 처리상태
     * paid:결제완료, ready:준비됨, failed:결제실패, cancelled:취소됨, partialCancelled:부분 취소됨, expired:만료됨
     * ['paid', 'ready', 'failed', 'cancelled', 'partialCancelled', 'expired']
     * 필수 | 20 Byte
     */
    @field:JsonProperty("status")
    override val status: String,

    /**
     * 결제완료시점 ISO 8601 형식
     * 결제완료가 아닐 경우 0
     * 필수
     */
    @field:JsonProperty("paidAt")
    val paidAt: String,

    /**
     * 결제실패시점 ISO 8601 형식
     * 결제실패가 아닐 경우 0
     * 필수
     */
    @field:JsonProperty("failedAt")
    val failedAt: String,

    /**
     * 결제취소시점 ISO 8601 형식
     * 결제취소가 아닐 경우 0
     * 부분취소인경우, 가장 마지막건의 취소 시간
     * 필수
     */
    @field:JsonProperty("cancelledAt")
    val cancelledAt: String,

    /**
     * 결제수단
     * card=신용카드, vbank=가상계좌, bank=계좌이체, cellphone=휴대폰,
     * naverpay=네이버페이, kakaopay=카카오페이, samsungpay=삼성페이
     * 필수 | 10 Byte
     */
    @field:JsonProperty("payMethod")
    val payMethod: String,

    /**
     * 결제 금액
     * 필수 | 12 Byte
     */
    @field:JsonProperty("amount")
    val amount: Int,

    /**
     * 취소 가능 잔액
     * 부분취소 거래인경우, 전체금액에서 현재까지 취소된 금액을 차감한 금액.
     * 필수 | 12 Byte
     */
    @field:JsonProperty("balanceAmt")
    val balanceAmt: Int,

    /**
     * 상품명
     * 필수 | 40 Byte
     */
    @field:JsonProperty("goodsName")
    val goodsName: String,

    /**
     * 상점 정보 전달용 예비필드
     * returnUrl로 redirect되는 시점에 반환 됩니다.
     * JSON string format으로 이용하시기를 권고 드립니다.
     * 단, 큰따옴표(")는 이용불가
     * 선택 | 500 Byte
     */
    @field:JsonProperty("mallReserved")
    val mallReserved: String? = null,

    /**
     * 에스크로 거래 여부
     * false:일반거래 / true:에스크로 거래
     * 필수
     */
    @field:JsonProperty("useEscrow")
    val useEscrow: Boolean,

    /**
     * 결제승인화폐단위
     * KRW:원화, USD:미화달러, CNY:위안화
     * 필수 | 3 Byte
     */
    @field:JsonProperty("currency")
    val currency: String,

    /**
     * 결제 채널
     * pc:PC결제, mobile:모바일결제
     * ['pc', 'mobile', 'null']
     * 선택 | 10 Byte
     */
    @field:JsonProperty("channel")
    val channel: String? = null,

    /**
     * 제휴사 승인 번호
     * 신용카드, 계좌이체, 휴대폰
     * 선택 | 30 Byte
     */
    @field:JsonProperty("approveNo")
    val approveNo: String? = null,

    /**
     * 구매자 명
     * 선택 | 30 Byte
     */
    @field:JsonProperty("buyerName")
    val buyerName: String? = null,

    /**
     * 구매자 전화번호
     * 선택 | 40 Byte
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
     * 현금영수증 발급여부
     * true:발행 / false:미발행
     * 선택
     */
    @field:JsonProperty("issuedCashReceipt")
    val issuedCashReceipt: Boolean? = null,

    /**
     * 매출전표 확인 URL
     * 선택 | 200 Byte
     */
    @field:JsonProperty("receiptUrl")
    val receiptUrl: String? = null,

    /**
     * 상점에서 관리하는 사용자 아이디
     * 선택 | 20 Byte
     */
    @field:JsonProperty("mallUserId")
    val mallUserId: String? = null,

    /** 할인 정보 */
    @field:JsonProperty("coupon")
    val coupon: PaymentDetails.Coupon? = null,

    /** 카드 정보 */
    @field:JsonProperty("card")
    val card: PaymentDetails.Card? = null,

    /** 현금영수증 */
    @field:JsonProperty("cashReceipts")
    val cashReceipts: List<PaymentDetails.CashReceipt>? = null,

    /** 은행 정보 */
    @field:JsonProperty("bank")
    val bank: PaymentDetails.Bank? = null,

    /** 가상계좌 정보 */
    @field:JsonProperty("vbank")
    override val vbank: PaymentDetails.VirtualBank? = null,

    /** 취소 내역 */
    @field:JsonProperty("cancels")
    val cancels: List<PaymentDetails.CancelDetail>? = null
) : V1PaymentResultBase {
    fun verifySignature(
        secretKey: String
    ): Boolean {
        val signatureData = "$tid$amount$ediDate"
        return ApiUtil.verifySignature(signatureData, signature, secretKey)
    }
}
