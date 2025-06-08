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

import org.bouncycastle.util.encoders.Hex
import java.security.MessageDigest

object ApiUtil {
    fun verifySignature(
        signatureData: String,
        signature: String?,
        secretKey: String
    ): Boolean {
        val sha256Hash = MessageDigest.getInstance("SHA-256")
            .digest("${signatureData}${secretKey}".toByteArray())
        val computedSignature = Hex.toHexString(sha256Hash).lowercase()
        return signature != null && computedSignature == signature.lowercase()
    }
}