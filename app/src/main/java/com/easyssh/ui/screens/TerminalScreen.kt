package com.easyssh.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
    onDisconnect: () -> Unit
) {
    BackHandler(onBack = onDisconnect)

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
            onDisconnect = onDisconnect
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
        TerminalShortcutKeyboard(onInput = onInput)
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
    onDisconnect: () -> Unit
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
                text = "${state.activeHost ?: ""} - ${state.connectionState.label()}",
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
    }
}

@Composable
private fun TerminalShortcutKeyboard(
    onInput: (String) -> Unit
) {
    var ctrlActive by rememberSaveable { mutableStateOf(false) }
    var shiftActive by rememberSaveable { mutableStateOf(false) }
    var altActive by rememberSaveable { mutableStateOf(false) }

    fun send(sequence: String) {
        onInput(sequence)
        ctrlActive = false
        shiftActive = false
        altActive = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ModifierKey("Ctrl", ctrlActive) { ctrlActive = !ctrlActive }
            ModifierKey("Shift", shiftActive) { shiftActive = !shiftActive }
            ModifierKey("Alt", altActive) { altActive = !altActive }
            ShortcutKey("Esc") { send(withAltPrefix("\u001B", altActive)) }
            ShortcutKey("Tab") {
                send(
                    when {
                        shiftActive -> withAltPrefix("\u001B[Z", altActive)
                        else -> withAltPrefix("\t", altActive)
                    }
                )
            }
            ShortcutKey("Enter") { send("\r") }
            ShortcutKey("Bksp") { send("\u007F") }
            ShortcutKey("Space") { send(withAltPrefix(" ", altActive)) }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShortcutKey("←") { send(cursorSequence("D", ctrlActive, shiftActive, altActive)) }
            ShortcutKey("↓") { send(cursorSequence("B", ctrlActive, shiftActive, altActive)) }
            ShortcutKey("↑") { send(cursorSequence("A", ctrlActive, shiftActive, altActive)) }
            ShortcutKey("→") { send(cursorSequence("C", ctrlActive, shiftActive, altActive)) }
            ShortcutKey("Home") { send(homeEndSequence("H", ctrlActive, shiftActive, altActive)) }
            ShortcutKey("End") { send(homeEndSequence("F", ctrlActive, shiftActive, altActive)) }
            ShortcutKey("PgUp") { send(pageSequence("5", ctrlActive, shiftActive, altActive)) }
            ShortcutKey("PgDn") { send(pageSequence("6", ctrlActive, shiftActive, altActive)) }
            CTRL_LETTERS.forEach { letter ->
                ShortcutKey(letter) {
                    send(letterSequence(letter, ctrlActive, shiftActive, altActive))
                }
            }
            SYMBOL_KEYS.forEach { key ->
                ShortcutKey(key.label) {
                    send(withAltPrefix(if (shiftActive) key.shifted else key.normal, altActive))
                }
            }
        }
    }
}

@Composable
private fun ModifierKey(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val colors = if (active) {
        ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.filledTonalButtonColors()
    }
    FilledTonalButton(
        modifier = Modifier
            .height(36.dp)
            .widthIn(min = 68.dp),
        colors = colors,
        onClick = onClick
    ) {
        Text(label, maxLines = 1)
    }
}

@Composable
private fun ShortcutKey(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier
            .height(36.dp)
            .widthIn(min = 48.dp),
        onClick = onClick
    ) {
        Text(label, maxLines = 1)
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
        title = { Text("Dimensões") },
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
                    Text("Espaçamento ${"%.2f".format(lineHeight)}")
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
                Text("Padrão")
            }
        }
    )
}

private fun ConnectionState.label(): String = when (this) {
    ConnectionState.IDLE -> "pronto"
    ConnectionState.CONNECTING -> "conectando"
    ConnectionState.CONNECTED -> "conectado"
    ConnectionState.DISCONNECTED -> "desconectado"
    ConnectionState.ERROR -> "erro"
}

private data class SymbolKey(
    val label: String,
    val normal: String,
    val shifted: String = normal
)

private val CTRL_LETTERS = listOf("A", "C", "D", "E", "L", "R", "Z")

private val SYMBOL_KEYS = listOf(
    SymbolKey("/", "/", "?"),
    SymbolKey("-", "-", "_"),
    SymbolKey("_", "_"),
    SymbolKey("|", "|"),
    SymbolKey("~", "~"),
    SymbolKey(".", "."),
    SymbolKey(":", ":")
)

private fun letterSequence(
    letter: String,
    ctrl: Boolean,
    shift: Boolean,
    alt: Boolean
): String {
    val value = if (ctrl) {
        controlCode(letter.first())
    } else if (shift) {
        letter.uppercase()
    } else {
        letter.lowercase()
    }
    return withAltPrefix(value, alt)
}

private fun controlCode(letter: Char): String {
    val upper = letter.uppercaseChar()
    return (upper.code - '@'.code).toChar().toString()
}

private fun cursorSequence(
    direction: String,
    ctrl: Boolean,
    shift: Boolean,
    alt: Boolean
): String {
    val modifier = xtermModifier(ctrl, shift, alt)
    return if (modifier == null) "\u001B[$direction" else "\u001B[1;${modifier}$direction"
}

private fun homeEndSequence(
    finalByte: String,
    ctrl: Boolean,
    shift: Boolean,
    alt: Boolean
): String {
    val modifier = xtermModifier(ctrl, shift, alt)
    return if (modifier == null) "\u001B[$finalByte" else "\u001B[1;${modifier}$finalByte"
}

private fun pageSequence(
    pageCode: String,
    ctrl: Boolean,
    shift: Boolean,
    alt: Boolean
): String {
    val modifier = xtermModifier(ctrl, shift, alt)
    return if (modifier == null) "\u001B[${pageCode}~" else "\u001B[${pageCode};${modifier}~"
}

private fun xtermModifier(
    ctrl: Boolean,
    shift: Boolean,
    alt: Boolean
): Int? {
    var value = 1
    if (shift) value += 1
    if (alt) value += 2
    if (ctrl) value += 4
    return value.takeIf { it > 1 }
}

private fun withAltPrefix(
    sequence: String,
    alt: Boolean
): String = if (alt) "\u001B$sequence" else sequence
