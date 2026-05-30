package com.easyssh.core.crypto

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StoredPrivateKey(
    val encryptedFileName: String,
    val displayName: String
)

class PrivateKeyStore(
    private val context: Context,
    private val encryptor: ByteEncryptor
) {
    private val keyDirectory: File
        get() = File(context.filesDir, KEY_DIRECTORY).also { it.mkdirs() }

    suspend fun saveFromUri(uri: Uri): StoredPrivateKey = withContext(Dispatchers.IO) {
        val plainBytes = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes()
        } ?: error("Não foi possível ler a chave selecionada.")

        require(plainBytes.isNotEmpty()) { "A chave selecionada está vazia." }

        val encryptedFileName = "${UUID.randomUUID()}.key"
        val encryptedFile = File(keyDirectory, encryptedFileName)
        encryptedFile.writeBytes(encryptor.encrypt(plainBytes))

        StoredPrivateKey(
            encryptedFileName = encryptedFileName,
            displayName = displayName(uri)
        )
    }

    suspend fun read(encryptedFileName: String): ByteArray = withContext(Dispatchers.IO) {
        val file = File(keyDirectory, encryptedFileName)
        require(file.exists()) { "Chave privada não encontrada no aparelho." }
        encryptor.decrypt(file.readBytes())
    }

    suspend fun delete(encryptedFileName: String) = withContext(Dispatchers.IO) {
        File(keyDirectory, encryptedFileName).delete()
    }

    private fun displayName(uri: Uri): String {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) {
                    return cursor.getString(index)
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "private-key.pem"
    }

    private companion object {
        const val KEY_DIRECTORY = "private_keys"
    }
}
