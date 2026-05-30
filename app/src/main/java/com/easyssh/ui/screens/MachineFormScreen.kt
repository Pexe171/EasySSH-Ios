package com.easyssh.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.easyssh.domain.IpMode
import com.easyssh.domain.MachineDraft
import com.easyssh.domain.MachineProfile

@Composable
fun MachineFormScreen(
    paddingValues: PaddingValues,
    profile: MachineProfile?,
    onBack: () -> Unit,
    onSave: (MachineDraft, Uri?) -> Unit,
    onDelete: (MachineProfile) -> Unit
) {
    var alias by rememberSaveable(profile?.id) { mutableStateOf(profile?.alias.orEmpty()) }
    var username by rememberSaveable(profile?.id) { mutableStateOf(profile?.username ?: "ec2-user") }
    var host by rememberSaveable(profile?.id) { mutableStateOf(profile?.host.orEmpty()) }
    var port by rememberSaveable(profile?.id) { mutableStateOf(profile?.port?.toString() ?: "22") }
    var ipMode by rememberSaveable(profile?.id) {
        mutableStateOf(profile?.ipMode ?: IpMode.STATIC)
    }
    var selectedKeyUri by remember(profile?.id) { mutableStateOf<Uri?>(null) }
    var selectedKeyName by rememberSaveable(profile?.id) {
        mutableStateOf(profile?.keyDisplayName.orEmpty())
    }

    val keyPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedKeyUri = uri
            selectedKeyName = uri.lastPathSegment?.substringAfterLast('/') ?: "private-key.pem"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (profile == null) "Nova VPS" else "Editar VPS",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SSH por chave PEM/OpenSSH",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onBack) {
                Text("Voltar")
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = alias,
            onValueChange = { alias = it },
            singleLine = true,
            label = { Text("Apelido da maquina") }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = username,
            onValueChange = { username = it },
            singleLine = true,
            label = { Text("Usuario SSH") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            USER_SUGGESTIONS.forEach { suggestion ->
                FilterChip(
                    selected = username == suggestion,
                    onClick = { username = suggestion },
                    label = { Text(suggestion) }
                )
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = port,
            onValueChange = { port = it.filter(Char::isDigit).take(5) },
            singleLine = true,
            label = { Text("Porta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("IP rotativo", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Perguntar IP ao conectar",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = ipMode == IpMode.ROTATING,
                onCheckedChange = { checked ->
                    ipMode = if (checked) IpMode.ROTATING else IpMode.STATIC
                }
            )
        }

        if (ipMode == IpMode.STATIC) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = host,
                onValueChange = { host = it.trim() },
                singleLine = true,
                label = { Text("IP publico ou DNS") }
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { keyPicker.launch(arrayOf("*/*")) }
        ) {
            Text(
                text = selectedKeyName.ifBlank { "Escolher chave PEM" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onSave(
                    MachineDraft(
                        id = profile?.id ?: 0,
                        alias = alias,
                        username = username,
                        host = host,
                        port = port,
                        ipMode = ipMode,
                        encryptedKeyFileName = profile?.encryptedKeyFileName,
                        keyDisplayName = profile?.keyDisplayName
                    ),
                    selectedKeyUri
                )
            }
        ) {
            Text("Salvar")
        }

        if (profile != null) {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onDelete(profile) }
            ) {
                Text("Remover maquina")
            }
        }
    }
}

private val USER_SUGGESTIONS = listOf("ec2-user", "ubuntu", "admin", "root")

