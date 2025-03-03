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

package io.mosaicboot.core.util

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPublicKeySpec
import java.security.KeyFactory

object BCHelper {
    val PROVIDER = BouncyCastleProvider()

    fun convertECPrivateToPublic(privateKey: BCECPrivateKey): BCECPublicKey {
        val spec = privateKey.parameters
        val g = spec.g

        val publicPoint = g.multiply(privateKey.d)

        val keyFactory = KeyFactory.getInstance("EC", BCHelper.PROVIDER)
        val publicKeySpec = ECPublicKeySpec(publicPoint, spec)

        return keyFactory.generatePublic(publicKeySpec) as BCECPublicKey
    }
}