package com.easyssh

import android.app.Application
import com.easyssh.core.AppContainer
import net.schmizz.sshj.common.SecurityUtils

class EasySshApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        SecurityUtils.setRegisterBouncyCastle(false)
        container = AppContainer(this)
    }
}
