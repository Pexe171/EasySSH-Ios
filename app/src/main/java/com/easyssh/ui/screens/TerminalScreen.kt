package com.easyssh.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easyssh.terminal.TerminalBridge
import com.easyssh.terminal.TerminalInputSink
import com.easyssh.terminal.TerminalWebView
import com.easyssh.terminal.TerminalWebViewHandle
import com.easyssh.ui.ConnectionState
import com.easyssh.ui.EasySshUiState
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun TerminalScreen(
    paddingValues: PaddingValues,
    state: EasySshUiState,
    terminalOutput: SharedFlow<ByteArray>,
    onInput: (String) -> Unit,
    onResize: (Int, Int) -> Unit,
    onDisconnect: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val terminalHandle = remember { TerminalWebViewHandle() }
    var showDisplayDialog by rememberSaveable { mutableStateOf(false) }
    var fontSize by rememberSaveable { mutableFloatStateOf(11f) }
    var lineHeight by rememberSaveable { mutableFloatStateOf(1.08f) }
    val bridge = remember {
        TerminalBridge(
            object : TerminalInputSink {
                override fun onTerminalInput(data: String) {
                    onInput(data)
                }

                override fun onTerminalResize(columns: Int, rows: Int) {
                    onResize(columns, rows)
                }
            }
        )
    }

    LaunchedEffect(fontSize, lineHeight) {
        terminalHandle.configureDisplay(fontSize.toInt(), lineHeight)
    }

    LaunchedEffect(terminalOutput) {
        terminalOutput.collect { bytes ->
            terminalHandle.write(bytes)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color.Black)
    ) {
        TerminalTopBar(
            state = state,
            onDisplaySettings = { showDisplayDialog = true },
            onDisconnect = onDisconnect,
            onBack = onBack
        )
        if (state.connectionState == ConnectionState.CONNECTING) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            TerminalWebView(
                bridge = bridge,
                handle = terminalHandle,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showDisplayDialog) {
        TerminalDisplayDialog(
            fontSize = fontSize,
            lineHeight = lineHeight,
            onFontSizeChange = { fontSize = it },
            onLineHeightChange = { lineHeight = it },
            onDismiss = { showDisplayDialog = false }
        )
    }
}

@Composable
private fun TerminalTopBar(
    state: EasySshUiState,
    onDisplaySettings: () -> Unit,
    onDisconnect: () -> Unit,
    onBack: () -> Unit
) {
    val profile = state.activeProfile
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile?.alias ?: "Terminal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${state.activeHost ?: ""} - ${state.connectionState.name.lowercase()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        OutlinedButton(onClick = onDisplaySettings) {
            Text("Aa")
        }
        OutlinedButton(onClick = onDisconnect) {
            Text("Desconectar")
        }
        Button(onClick = onBack) {
            Text("Sair")
        }
    }
}

@Composable
private fun TerminalDisplayDialog(
    fontSize: Float,
    lineHeight: Float,
    onFontSizeChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dimensoes") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Column {
                    Text("Fonte ${fontSize.toInt()}")
                    Slider(
                        value = fontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 9f..18f,
                        steps = 8
                    )
                }
                Column {
                    Text("Linhas ${"%.2f".format(lineHeight)}")
                    Slider(
                        value = lineHeight,
                        onValueChange = onLineHeightChange,
                        valueRange = 1f..1.5f,
                        steps = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Pronto")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onFontSizeChange(11f)
                    onLineHeightChange(1.08f)
                }
            ) {
                Text("Padrao")
            }
        }
    )
}
