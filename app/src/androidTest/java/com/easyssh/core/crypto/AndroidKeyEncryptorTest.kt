package com.easyssh.core.crypto

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidKeyEncryptorTest {
    @Test
    fun encryptDecryptRoundTrip() {
        val encryptor = AndroidKeyEncryptor()
        val plain = "-----BEGIN OPENSSH PRIVATE KEY-----".toByteArray()

        val encrypted = encryptor.encrypt(plain)
        val decrypted = encryptor.decrypt(encrypted)

        assertFalse(plain.contentEquals(encrypted))
        assertArrayEquals(plain, decrypted)
    }
}

