package com.easyssh.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyssh.core.AppContainer
import com.easyssh.core.crypto.StoredPrivateKey
import com.easyssh.domain.IpMode
import com.easyssh.domain.MachineDraft
import com.easyssh.domain.MachineProfile
import com.easyssh.domain.MachineValidator
import com.easyssh.ssh.SshConnectionConfig
import com.easyssh.ssh.SshTerminalSession
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    EDITOR,
    TERMINAL
}

enum class ConnectionState {
    IDLE,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    ERROR
}

data class RotatingIpRequest(
    val profileId: Long,
    val alias: String
)

data class HostKeyPrompt(
    val id: String,
    val alias: String,
    val host: String,
    val fingerprint: String
)

data class EasySshUiState(
    val profiles: List<MachineProfile> = emptyList(),
    val screen: AppScreen = AppScreen.HOME,
    val editingProfileId: Long? = null,
    val activeProfileId: Long? = null,
    val activeHost: String? = null,
    val connectionState: ConnectionState = ConnectionState.IDLE,
    val pendingRotatingIp: RotatingIpRequest? = null,
    val hostKeyPrompt: HostKeyPrompt? = null,
    val message: String? = null
) {
    val editingProfile: MachineProfile?
        get() = profiles.firstOrNull { it.id == editingProfileId }

    val activeProfile: MachineProfile?
        get() = profiles.firstOrNull { it.id == activeProfileId }
}

class MainViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val _uiState = MutableStateFlow(EasySshUiState())
    val uiState: StateFlow<EasySshUiState> = _uiState.asStateFlow()

    private val _terminalOutput = MutableSharedFlow<ByteArray>(extraBufferCapacity = 128)
    val terminalOutput: SharedFlow<ByteArray> = _terminalOutput.asSharedFlow()

    private val hostKeyDecisions = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()
    private var activeSession: SshTerminalSession? = null

    init {
        viewModelScope.launch {
            container.machineRepository.profiles.collect { profiles ->
                _uiState.update { state -> state.copy(profiles = profiles) }
            }
        }
    }

    fun openCreate() {
        _uiState.update {
            it.copy(screen = AppScreen.EDITOR, editingProfileId = null, message = null)
        }
    }

    fun openEdit(profile: MachineProfile) {
        _uiState.update {
            it.copy(screen = AppScreen.EDITOR, editingProfileId = profile.id, message = null)
        }
    }

    fun goHome() {
        _uiState.update {
            it.copy(screen = AppScreen.HOME, editingProfileId = null, message = null)
        }
    }

    fun saveMachine(draft: MachineDraft, selectedKeyUri: Uri?) {
        val errors = MachineValidator.validateDraft(
            draft = draft,
            hasSelectedKey = selectedKeyUri != null
        )
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(message = errors.joinToString("\n")) }
            return
        }

        viewModelScope.launch {
            runCatching {
                val storedKey = if (selectedKeyUri != null) {
                    container.privateKeyStore.saveFromUri(selectedKeyUri)
                } else {
                    StoredPrivateKey(
                        encryptedFileName = requireNotNull(draft.encryptedKeyFileName),
                        displayName = requireNotNull(draft.keyDisplayName)
                    )
                }

                container.machineRepository.saveDraft(
                    draft = draft,
                    encryptedKeyFileName = storedKey.encryptedFileName,
                    keyDisplayName = storedKey.displayName
                )

                if (selectedKeyUri != null && draft.encryptedKeyFileName != null) {
                    container.privateKeyStore.delete(draft.encryptedKeyFileName)
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        screen = AppScreen.HOME,
                        editingProfileId = null,
                        message = "Maquina salva."
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(message = error.message ?: "Nao foi possivel salvar a maquina.")
                }
            }
        }
    }

    fun deleteMachine(profile: MachineProfile) {
        viewModelScope.launch {
            runCatching {
                container.machineRepository.delete(profile)
                container.privateKeyStore.delete(profile.encryptedKeyFileName)
            }.onSuccess {
                _uiState.update {
                    it.copy(screen = AppScreen.HOME, editingProfileId = null, message = "Maquina removida.")
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(message = error.message ?: "Nao foi possivel remover a maquina.")
                }
            }
        }
    }

    fun requestConnect(profile: MachineProfile) {
        if (profile.ipMode == IpMode.ROTATING) {
            _uiState.update {
                it.copy(
                    pendingRotatingIp = RotatingIpRequest(profile.id, profile.alias),
                    message = null
                )
            }
            return
        }

        val host = profile.host
        if (host.isNullOrBlank()) {
            _uiState.update { it.copy(message = "IP ou DNS nao configurado.") }
            return
        }
        connect(profile, host)
    }

    fun connectWithRotatingHost(host: String) {
        val request = _uiState.value.pendingRotatingIp ?: return
        if (!MachineValidator.isValidHost(host)) {
            _uiState.update { it.copy(message = "Informe um IP ou DNS publico valido.") }
            return
        }
        val profile = _uiState.value.profiles.firstOrNull { it.id == request.profileId } ?: return
        _uiState.update { it.copy(pendingRotatingIp = null) }
        connect(profile, host.trim())
    }

    fun cancelRotatingHost() {
        _uiState.update { it.copy(pendingRotatingIp = null) }
    }

    fun answerHostKey(accepted: Boolean) {
        val prompt = _uiState.value.hostKeyPrompt ?: return
        hostKeyDecisions.remove(prompt.id)?.complete(accepted)
        _uiState.update { it.copy(hostKeyPrompt = null) }
    }

    fun sendTerminalInput(data: String) {
        viewModelScope.launch {
            activeSession?.send(data)
        }
    }

    fun resizeTerminal(columns: Int, rows: Int) {
        viewModelScope.launch {
            activeSession?.resize(columns, rows)
        }
    }

    fun disconnect() {
        activeSession?.close()
        activeSession = null
        hostKeyDecisions.values.forEach { it.complete(false) }
        hostKeyDecisions.clear()
        _uiState.update {
            it.copy(connectionState = ConnectionState.DISCONNECTED, hostKeyPrompt = null)
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun connect(profile: MachineProfile, host: String) {
        activeSession?.close()
        activeSession = null
        _uiState.update {
            it.copy(
                screen = AppScreen.TERMINAL,
                activeProfileId = profile.id,
                activeHost = host,
                connectionState = ConnectionState.CONNECTING,
                message = null
            )
        }

        viewModelScope.launch {
            emitTerminalLine("Connecting ${profile.username}@$host:${profile.port} ...")
            runCatching {
                val privateKeyBytes = container.privateKeyStore.read(profile.encryptedKeyFileName)
                container.sshSessionManager.connect(
                    config = SshConnectionConfig(
                        host = host,
                        port = profile.port,
                        username = profile.username,
                        knownHostFingerprint = profile.hostKeyFingerprint
                    ),
                    privateKeyBytes = privateKeyBytes,
                    onOutput = { bytes -> _terminalOutput.tryEmit(bytes) },
                    confirmUnknownHost = { fingerprint ->
                        requestHostKeyConfirmation(profile, host, fingerprint)
                    }
                )
            }.onSuccess { result ->
                activeSession = result.session
                result.observedHostFingerprint
                    ?.takeIf { profile.hostKeyFingerprint == null }
                    ?.let { fingerprint ->
                        container.machineRepository.updateHostKeyFingerprint(profile.id, fingerprint)
                    }
                _uiState.update { it.copy(connectionState = ConnectionState.CONNECTED) }
                emitTerminalLine("Connected.")
            }.onFailure { error ->
                activeSession?.close()
                activeSession = null
                _uiState.update {
                    it.copy(
                        connectionState = ConnectionState.ERROR,
                        message = error.message ?: "Falha ao conectar por SSH."
                    )
                }
                emitTerminalLine("Connection failed: ${error.message ?: "unknown error"}")
            }
        }
    }

    private suspend fun requestHostKeyConfirmation(
        profile: MachineProfile,
        host: String,
        fingerprint: String
    ): Boolean {
        val promptId = UUID.randomUUID().toString()
        val decision = CompletableDeferred<Boolean>()
        hostKeyDecisions[promptId] = decision
        _uiState.update {
            it.copy(
                hostKeyPrompt = HostKeyPrompt(
                    id = promptId,
                    alias = profile.alias,
                    host = host,
                    fingerprint = fingerprint
                )
            )
        }
        return decision.await()
    }

    private suspend fun emitTerminalLine(text: String) {
        _terminalOutput.emit("\r\n$text\r\n".toByteArray(Charsets.UTF_8))
    }

    override fun onCleared() {
        disconnect()
        super.onCleared()
    }

    class Factory(
        private val container: AppContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(container) as T
        }
    }
}

