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

class PaymentDetails {
    // 할인 정보 데이터 클래스
    data class Coupon(
        /** 즉시할인 적용된 금액 */
        @field:JsonProperty("couponAmt")
        val couponAmt: Int? = null
    )

    // 카드 정보 데이터 클래스
    data class Card(
        /** 신용카드사 코드 */
        @field:JsonProperty("cardCode")
        val cardCode: String,

        /** 결제 카드사 이름 */
        @field:JsonProperty("cardName")
        val cardName: String,

        /** 카드번호 */
        @field:JsonProperty("cardNum")
        val cardNum: String? = null,

        /** 할부개월 */
        @field:JsonProperty("cardQuota")
        val cardQuota: String,

        /** 상점분담무이자 여부 */
        @field:JsonProperty("isInterestFree")
        val isInterestFree: Boolean,

        /** 카드 구분 */
        @field:JsonProperty("cardType")
        val cardType: String? = null,

        /** 부분취소 가능 여부 */
        @field:JsonProperty("canPartCancel")
        val canPartCancel: Boolean,

        /** 매입카드사코드 */
        @field:JsonProperty("acquCardCode")
        val acquCardCode: String,

        /** 매입카드사명 */
        @field:JsonProperty("acquCardName")
        val acquCardName: String
    )

    // 현금영수증 데이터 클래스
    data class CashReceipt(
        /** 현금영수증 TID */
        @field:JsonProperty("receiptTid")
        val receiptTid: String,

        /** 원 승인/취소 거래 TID */
        @field:JsonProperty("orgTid")
        val orgTid: String,

        /** 발급진행 상태 */
        @field:JsonProperty("status")
        val status: String,

        /** 현금영수증 발행 총금액 */
        @field:JsonProperty("amount")
        val amount: Int,

        /** 현금영수증 전체 금액중에서 면세금액 */
        @field:JsonProperty("taxFreeAmt")
        val taxFreeAmt: Int,

        /** 현금영수증 타입 */
        @field:JsonProperty("receiptType")
        val receiptType: String,

        /** 현금영수증 국세청 발행번호 */
        @field:JsonProperty("issueNo")
        val issueNo: String,

        /** 현금영수증 매출전표 확인 URL */
        @field:JsonProperty("receiptUrl")
        val receiptUrl: String
    )

    // 은행 정보 데이터 클래스
    data class Bank(
        /** 결제은행코드 */
        @field:JsonProperty("bankCode")
        val bankCode: String,

        /** 결제은행명 */
        @field:JsonProperty("bankName")
        val bankName: String
    )

    // 가상계좌 정보 데이터 클래스
    data class VirtualBank(
        /** 가상계좌 은행코드 */
        @field:JsonProperty("vbankCode")
        val vbankCode: String,

        /** 가상계좌 은행명 */
        @field:JsonProperty("vbankName")
        val vbankName: String,

        /** 가상계좌 번호 */
        @field:JsonProperty("vbankNumber")
        val vbankNumber: String,

        /** 가상계좌 입금 만료일 */
        @field:JsonProperty("vbankExpDate")
        val vbankExpDate: String,

        /** 가상계좌 예금주명 */
        @field:JsonProperty("vbankHolder")
        val vbankHolder: String
    )

    // 취소 내역 데이터 클래스
    data class CancelDetail(
        /** 승인 취소 거래번호 */
        @field:JsonProperty("tid")
        val tid: String,

        /** 취소금액 */
        @field:JsonProperty("amount")
        val amount: Int,

        /** 취소된 시각 */
        @field:JsonProperty("cancelledAt")
        val cancelledAt: String,

        /** 취소사유 */
        @field:JsonProperty("reason")
        val reason: String,

        /** 취소에 대한 매출전표 확인 URL */
        @field:JsonProperty("receiptUrl")
        val receiptUrl: String,

        /** 쿠폰 취소금액 */
        @field:JsonProperty("couponAmt")
        val couponAmt: Int? = null
    )
}