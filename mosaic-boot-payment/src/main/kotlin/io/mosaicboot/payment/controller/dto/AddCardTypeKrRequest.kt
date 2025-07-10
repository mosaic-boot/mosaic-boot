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

package io.mosaicboot.payment.controller.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class AddCardTypeKrRequest(
    @JsonProperty("primary")
    val primary: Boolean,

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
