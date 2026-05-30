package com.easyssh.domain

data class MachineDraft(
    val id: Long = 0,
    val alias: String = "",
    val username: String = "ec2-user",
    val host: String = "",
    val port: String = "22",
    val ipMode: IpMode = IpMode.STATIC,
    val encryptedKeyFileName: String? = null,
    val keyDisplayName: String? = null
)

