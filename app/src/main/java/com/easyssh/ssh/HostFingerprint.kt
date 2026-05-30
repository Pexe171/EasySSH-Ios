package com.easyssh.ssh

import android.util.Base64
import java.security.MessageDigest
import java.security.PublicKey

object HostFingerprint {
    fun sha256(publicKey: PublicKey): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(publicKey.encoded)
        val value = Base64.encodeToString(digest, Base64.NO_PADDING or Base64.NO_WRAP)
        return "SHA256:$value"
    }
}

