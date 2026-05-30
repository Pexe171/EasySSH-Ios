package com.easyssh.ssh

import net.schmizz.sshj.DefaultConfig

fun androidSshConfig(): DefaultConfig {
    return DefaultConfig().apply {
        setKeyExchangeFactories(
            keyExchangeFactories.filterNot { factory ->
                factory.name.contains("curve25519", ignoreCase = true)
            }
        )
    }
}
