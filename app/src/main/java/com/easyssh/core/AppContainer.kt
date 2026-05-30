package com.easyssh.core

import android.content.Context
import com.easyssh.core.crypto.AndroidKeyEncryptor
import com.easyssh.core.crypto.PrivateKeyStore
import com.easyssh.data.AppDatabase
import com.easyssh.data.MachineRepository
import com.easyssh.ssh.SshSessionManager

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.create(appContext)

    val machineRepository = MachineRepository(database.machineDao())
    val privateKeyStore = PrivateKeyStore(
        context = appContext,
        encryptor = AndroidKeyEncryptor()
    )
    val sshSessionManager = SshSessionManager()
}

