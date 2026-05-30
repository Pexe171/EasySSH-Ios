package com.easyssh.ssh

data class SshConnectionConfig(
    val host: String,
    val port: Int,
    val username: String,
    val knownHostFingerprint: String?
)

