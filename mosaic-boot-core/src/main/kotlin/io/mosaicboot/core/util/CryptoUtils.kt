package io.mosaicboot.core.util

import org.bouncycastle.crypto.digests.SHA256Digest
import java.util.Base64

object CryptoUtils {
    fun sha256ForKey(vararg inputs: String): String {
        val digest = SHA256Digest.newInstance()
        inputs.forEach { item ->
            item.toByteArray(Charsets.UTF_8).let {
                digest.update(it, 0, it.size)
            }
        }
        val out = ByteArray(digest.digestSize)
        digest.doFinal(out, 0)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(out)
    }
}