package com.easyssh.domain

data class MachineProfile(
    val id: Long,
    val alias: String,
    val username: String,
    val host: String?,
    val port: Int,
    val ipMode: IpMode,
    val encryptedKeyFileName: String,
    val keyDisplayName: String,
    val hostKeyFingerprint: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    val isRotatingIp: Boolean = ipMode == IpMode.ROTATING
}

