package com.easyssh.ssh

import java.io.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session

class SshTerminalSession(
    private val client: SSHClient,
    private val session: Session,
    private val shell: Session.Shell,
    private val scope: CoroutineScope,
    private val outputJob: Job,
    private val errorJob: Job
) : Closeable {
    suspend fun send(data: String) = withContext(Dispatchers.IO) {
        shell.outputStream.write(data.toByteArray(Charsets.UTF_8))
        shell.outputStream.flush()
    }

    suspend fun resize(columns: Int, rows: Int) = withContext(Dispatchers.IO) {
        runCatching {
            shell.changeWindowDimensions(columns, rows, columns * 8, rows * 16)
        }
    }

    override fun close() {
        outputJob.cancel()
        errorJob.cancel()
        scope.cancel()
        runCatching { shell.close() }
        runCatching { session.close() }
        runCatching { client.disconnect() }
        runCatching { client.close() }
    }
}

