package com.easyssh.ssh

import java.io.File
import java.io.InputStream
import java.security.PublicKey
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.HostKeyVerifier

data class SshConnectResult(
    val session: SshTerminalSession,
    val observedHostFingerprint: String?
)

class SshSessionManager {
    suspend fun connect(
        config: SshConnectionConfig,
        privateKeyBytes: ByteArray,
        onOutput: (ByteArray) -> Unit,
        confirmUnknownHost: suspend (String) -> Boolean
    ): SshConnectResult = withContext(Dispatchers.IO) {
        val tempKey = File.createTempFile("easyssh-", ".pem")
        val observedFingerprint = AtomicReference<String?>()
        var client: SSHClient? = null
        var session: net.schmizz.sshj.connection.channel.direct.Session? = null
        var shell: net.schmizz.sshj.connection.channel.direct.Session.Shell? = null

        try {
            tempKey.writeBytes(privateKeyBytes)

            val ssh = SSHClient(androidSshConfig()).also {
                it.connectTimeout = CONNECT_TIMEOUT_MS
                it.timeout = SOCKET_TIMEOUT_MS
                it.addHostKeyVerifier(object : HostKeyVerifier {
                    override fun verify(hostname: String, port: Int, key: PublicKey): Boolean {
                        val fingerprint = HostFingerprint.sha256(key)
                        observedFingerprint.set(fingerprint)
                        val known = config.knownHostFingerprint
                        return if (known == null) {
                            runBlocking { confirmUnknownHost(fingerprint) }
                        } else {
                            known == fingerprint
                        }
                    }

                    override fun findExistingAlgorithms(hostname: String, port: Int): List<String> {
                        return emptyList()
                    }
                })
            }
            client = ssh

            ssh.connect(config.host, config.port)
            val keyProvider = ssh.loadKeys(tempKey.absolutePath)
            ssh.authPublickey(config.username, keyProvider)

            val sshSession = ssh.startSession()
            session = sshSession
            sshSession.allocateDefaultPTY()
            val sshShell = sshSession.startShell()
            shell = sshShell

            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val outputJob = scope.launch {
                readLoop(sshShell.inputStream, onOutput)
            }
            val errorJob = scope.launch {
                readLoop(sshShell.errorStream, onOutput)
            }

            SshConnectResult(
                session = SshTerminalSession(
                    client = ssh,
                    session = sshSession,
                    shell = sshShell,
                    scope = scope,
                    outputJob = outputJob,
                    errorJob = errorJob
                ),
                observedHostFingerprint = observedFingerprint.get()
            )
        } catch (error: Throwable) {
            runCatching { shell?.close() }
            runCatching { session?.close() }
            runCatching { client?.disconnect() }
            runCatching { client?.close() }
            throw error
        } finally {
            tempKey.writeText("")
            tempKey.delete()
        }
    }

    private suspend fun readLoop(
        stream: InputStream,
        onOutput: (ByteArray) -> Unit
    ) {
        val buffer = ByteArray(BUFFER_SIZE)
        while (kotlin.coroutines.coroutineContext.isActive) {
            val read = stream.read(buffer)
            if (read < 0) break
            if (read > 0) {
                onOutput(buffer.copyOf(read))
            }
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000
        const val SOCKET_TIMEOUT_MS = 30_000
        const val BUFFER_SIZE = 8192
    }
}
