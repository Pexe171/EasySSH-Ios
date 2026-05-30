package com.easyssh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.easyssh.ui.EasySshApp
import com.easyssh.ui.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as EasySshApplication).container
        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModel.Factory(container)
            )
            EasySshApp(viewModel)
        }
    }
}

