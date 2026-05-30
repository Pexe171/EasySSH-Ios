package com.easyssh

import android.app.Application
import com.easyssh.core.AppContainer
import java.security.Security
import net.schmizz.sshj.common.SecurityUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider

class EasySshApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        installBundledBouncyCastle()
        container = AppContainer(this)
    }

    private fun installBundledBouncyCastle() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        SecurityUtils.setRegisterBouncyCastle(false)
        SecurityUtils.setSecurityProvider(BouncyCastleProvider.PROVIDER_NAME)
    }
}
