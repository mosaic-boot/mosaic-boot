package io.mosaicboot.payment.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class AddCardTypeKrRequest(
    @JsonProperty("alias")
    val alias: String?,

    @JsonProperty("name")
    val name: String?,

    @JsonProperty("email")
    val email: String?,

    @JsonProperty("tel")
    val tel: String?,

    /**
     * 16 글자
     */
    @JsonProperty("cardNo")
    val cardNo: String,
    /**
     * 2글자 (YY)
     */
    @JsonProperty("expYear")
    val expYear: String,
    /**
     * 2글자 (MM)
     */
    @JsonProperty("expMonth")
    val expMonth: String,
    /**
     * 생년월일 (YYMMDD) or 사업자등록번호(10글자)
     */
    @JsonProperty("idNo")
    val idNo: String,
    /**
     * 카드 비밀번호 앞 2자리
     */
    @JsonProperty("cardPw")
    val cardPw: String,
)
