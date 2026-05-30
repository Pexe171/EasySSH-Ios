package com.easyssh.core.crypto

interface ByteEncryptor {
    fun encrypt(plainBytes: ByteArray): ByteArray
    fun decrypt(encryptedBytes: ByteArray): ByteArray
}

