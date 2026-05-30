package com.easyssh.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.easyssh.ui.screens.MachineFormScreen
import com.easyssh.ui.screens.MachineListScreen
import com.easyssh.ui.screens.RotatingIpDialog
import com.easyssh.ui.screens.TerminalScreen
import com.easyssh.ui.theme.EasySshTheme

@Composable
fun EasySshApp(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    EasySshTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            when (state.screen) {
                AppScreen.HOME -> MachineListScreen(
                    paddingValues = paddingValues,
                    profiles = state.profiles,
                    onAdd = viewModel::openCreate,
                    onEdit = viewModel::openEdit,
                    onConnect = viewModel::requestConnect
                )

                AppScreen.EDITOR -> MachineFormScreen(
                    paddingValues = paddingValues,
                    profile = state.editingProfile,
                    onBack = viewModel::goHome,
                    onSave = viewModel::saveMachine,
                    onDelete = viewModel::deleteMachine
                )

                AppScreen.TERMINAL -> TerminalScreen(
                    paddingValues = paddingValues,
                    state = state,
                    terminalOutput = viewModel.terminalOutput,
                    onInput = viewModel::sendTerminalInput,
                    onResize = viewModel::resizeTerminal,
                    onDisconnect = {
                        viewModel.disconnect()
                        viewModel.goHome()
                    }
                )
            }
        }

        state.pendingRotatingIp?.let { request ->
            RotatingIpDialog(
                alias = request.alias,
                onConnect = viewModel::connectWithRotatingHost,
                onDismiss = viewModel::cancelRotatingHost
            )
        }

        state.hostKeyPrompt?.let { prompt ->
            AlertDialog(
                onDismissRequest = { viewModel.answerHostKey(false) },
                title = { Text("Confirmar servidor") },
                text = {
                    Text(
                        "Primeiro acesso a ${prompt.alias} em ${prompt.host}.\n\n" +
                            "Fingerprint:\n${prompt.fingerprint}"
                    )
                },
                confirmButton = {
                    Button(onClick = { viewModel.answerHostKey(true) }) {
                        Text("Confiar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.answerHostKey(false) }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
