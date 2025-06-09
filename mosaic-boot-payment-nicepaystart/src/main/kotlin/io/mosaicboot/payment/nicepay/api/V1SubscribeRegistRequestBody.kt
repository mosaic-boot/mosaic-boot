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
import org.bouncycastle.util.encoders.Hex
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class V1SubscribeRegistRequestBody(
    /**
     * 결제정보 암호화 데이터
     * - 암호화 알고리즘 : AES128
     * - 암호화 상세 : AES/ECB/PKCS5padding
     * - 암호결과 인코딩 : Hex Encoding
     * - 암호 KEY : SecretKey 앞 16자리
     * - Hex(AES(cardNo=value&expYear=YY&expMonth=MM&idNo=value&cardPw=value))
     * 필수 | 512 Byte
     */
    @field:JsonProperty("encData")
    val encData: String,

    /**
     * 상점 거래 고유번호
     * 가맹점에서 관리하는 Unique한 주문번호 또는 결제번호
     * 필수 | 64 Byte
     */
    @field:JsonProperty("orderId")
    val orderId: String,

    /**
     * 구매자
     * 선택 | 30 Byte
     */
    @field:JsonProperty("buyerName")
    val buyerName: String? = null,

    /**
     * 구매자 이메일주소
     * 선택 | 60 Byte
     */
    @field:JsonProperty("buyerEmail")
    val buyerEmail: String? = null,

    /**
     * 구매자 전화번호
     * '-' 없이 숫자만 입력
     * 선택 | 20 Byte
     */
    @field:JsonProperty("buyerTel")
    val buyerTel: String? = null,

    /**
     * 암호화 모드
     * encData 필드의 암호화 알고리즘 정의
     *
     * A2 : AES256
     * • 암호화 알고리즘 : AES256
     * • 암호화 상세 : AES/CBC/PKCS5padding
     * • 암호결과 인코딩 : Hex Encoding
     * • 암호 KEY : SecretKey (32byte)
     * • IV : SecretKey 앞 16자리
     * 선택 | 10 Byte
     */
    @field:JsonProperty("encMode")
    val encMode: String = "A2",

    /**
     * 전문생성일시
     * ISO 8601 형식
     * 선택
     */
    @field:JsonProperty("ediDate")
    val ediDate: String? = null,

    /**
     * 위변조 검증 Data
     * 생성규칙 : hex(sha256(orderId + ediDate + SecretKey))
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
    val returnCharSet: String = "utf-8",
) {
    companion object {
        fun makeSignData(orderId: String, ediDate: String, secretKey: String): String {
            return ApiUtil.makeSignature(orderId + ediDate, secretKey)
        }

        /**
         * encData를 생성합니다.
         * @param secretKey 암호화 키
         * @param cardNo 카드번호
         * @param expYear 만료년도 (YY 형식)
         * @param expMonth 만료월 (MM 형식)
         * @param idNo 주민번호 또는 사업자번호
         * @param cardPw 카드 비밀번호 앞 2자리
         * @param encMode 암호화 모드 ("A2" for AES256/CBC, 기타는 AES128/ECB)
         * @return 암호화된 encData
         */
        fun generateEncData(
            secretKey: String,
            cardNo: String,
            expYear: String,
            expMonth: String,
            idNo: String,
            cardPw: String,
            encMode: String = "A2"
        ): String {
            // 평문 데이터 생성
            val plainData = "cardNo=${cardNo}&expYear=${expYear}&expMonth=${expMonth}&idNo=${idNo}&cardPw=${cardPw}"

            return when (encMode) {
                "A2" -> encryptAes256Cbc(plainData, secretKey)
                else -> encryptAes128Ecb(plainData, secretKey)
            }
        }

        /**
         * AES256/CBC/PKCS5Padding 암호화
         */
        private fun encryptAes256Cbc(plainText: String, secretKey: String): String {
            val keyBytes = secretKey.toByteArray(Charsets.UTF_8).copyOf(32) // 32바이트로 패딩
            val ivBytes = secretKey.toByteArray(Charsets.UTF_8).copyOf(16) // 앞 16자리를 IV로 사용

            val secretKeySpec = SecretKeySpec(keyBytes, "AES")
            val ivParameterSpec = IvParameterSpec(ivBytes)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            return bytesToHex(encryptedBytes)
        }

        /**
         * AES128/ECB/PKCS5Padding 암호화
         */
        private fun encryptAes128Ecb(plainText: String, secretKey: String): String {
            val keyBytes = secretKey.toByteArray(Charsets.UTF_8).copyOf(16) // 앞 16자리만 사용

            val secretKeySpec = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            return bytesToHex(encryptedBytes)
        }

        /**
         * 바이트 배열을 16진수 문자열로 변환
         */
        private fun bytesToHex(bytes: ByteArray): String {
            return Hex.toHexString(bytes)
        }
    }
}