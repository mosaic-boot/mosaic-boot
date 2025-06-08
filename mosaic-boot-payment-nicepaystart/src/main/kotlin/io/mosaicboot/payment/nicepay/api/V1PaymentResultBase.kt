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

interface V1PaymentResultBase {
    /** 결제결과코드 (0000: 성공, 그외 실패) */
    val resultCode: String

    /** 결제결과메시지 */
    val resultMsg: String

    /** 결제 처리상태 */
    val status: String

    /** 가상계좌 정보 */
    val vbank: PaymentDetails.VirtualBank?
}