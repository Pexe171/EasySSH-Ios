package com.easyssh.domain

object MachineValidator {
    private val hostRegex = Regex("^[A-Za-z0-9][A-Za-z0-9.-]{0,252}$")

    fun validateDraft(draft: MachineDraft, hasSelectedKey: Boolean): List<String> {
        val errors = mutableListOf<String>()

        if (draft.alias.isBlank()) {
            errors += "Informe o apelido da máquina."
        }
        if (draft.username.isBlank()) {
            errors += "Informe o usuário SSH."
        }

        val port = draft.port.toIntOrNull()
        if (port == null || port !in 1..65535) {
            errors += "A porta SSH deve ficar entre 1 e 65535."
        }

        if (draft.ipMode == IpMode.STATIC && !isValidHost(draft.host)) {
            errors += "Informe um IP ou DNS público válido."
        }

        if (draft.encryptedKeyFileName == null && !hasSelectedKey) {
            errors += "Escolha uma chave PEM/OpenSSH."
        }

        return errors
    }

    fun isValidHost(host: String): Boolean {
        val trimmed = host.trim()
        return trimmed.isNotEmpty() &&
            !trimmed.contains("://") &&
            !trimmed.any(Char::isWhitespace) &&
            trimmed.length <= 253 &&
            hostRegex.matches(trimmed)
    }
}
