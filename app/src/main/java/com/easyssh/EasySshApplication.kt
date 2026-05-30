package com.easyssh

import android.app.Application
import com.easyssh.core.AppContainer

class EasySshApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

