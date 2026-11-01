package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showGitHubDialog by remember { mutableStateOf(false) }
    var showCloudflareDialog by remember { mutableStateOf(false) }
    var showHuggingFaceDialog by remember { mutableStateOf(false) }
    var showClearCacheConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status message banner
        if (!uiState.statusMessage.isNullOrBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryPurple.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(text = uiState.statusMessage!!, color = PrimaryPurple, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // 1. Demo Mode Toggle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive Demo Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (uiState.isDemoMode) "Displaying realistic sample repos & deployments. Edits persist locally in Room."
                            else "Connected to real GitHub API. Authentication token will be used for network requests.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = uiState.isDemoMode,
                        onCheckedChange = { viewModel.setDemoMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = PrimaryPurple,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkElevatedSurface
                        ),
                        modifier = Modifier.testTag("demo_mode_switch")
                    )
                }
            }
        }

        // 2. Integration Accounts Section
        item {
            Text(
                text = "Integrations & API Credentials",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        // GitHub Card
        item {
            IntegrationSettingCard(
                name = "GitHub Account",
                status = if (uiState.hasGitHubToken) "Connected (${uiState.gitHubUsername ?: "active"})"
                else if (uiState.isDemoMode) "Simulated (octodev)" else "Not Configured",
                icon = Icons.Default.Hub,
                accentColor = PrimaryPurple,
                isConnected = uiState.hasGitHubToken,
                onConfigure = { showGitHubDialog = true },
                onDisconnect = { viewModel.saveGitHubToken(null, null) }
            )
        }

        // Cloudflare Card
        item {
            IntegrationSettingCard(
                name = "Cloudflare Workers & Pages",
                status = if (uiState.hasCloudflareToken) "Token Saved"
                else "Unconfigured (Using live demo metrics)",
                icon = Icons.Default.Cloud,
                accentColor = Color(0xFFF97316),
                isConnected = uiState.hasCloudflareToken,
                onConfigure = { showCloudflareDialog = true },
                onDisconnect = { viewModel.saveCloudflareCredentials(null, null) }
            )
        }

        // Hugging Face Card
        item {
            IntegrationSettingCard(
                name = "Hugging Face Spaces",
                status = if (uiState.hasHuggingFaceToken) "Token Saved"
                else "Unconfigured (Using live demo metrics)",
                icon = Icons.Default.Layers,
                accentColor = Color(0xFFFBBF24),
                isConnected = uiState.hasHuggingFaceToken,
                onConfigure = { showHuggingFaceDialog = true },
                onDisconnect = { viewModel.saveHuggingFaceToken(null) }
            )
        }

        // 3. Editor Preferences
        item {
            Text(
                text = "Code Editor Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Font size slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Editor Font Size",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${uiState.editorFontSize.toInt()} sp",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryCyan
                        )
                    }
                    Slider(
                        value = uiState.editorFontSize,
                        onValueChange = { viewModel.setEditorFontSize(it) },
                        valueRange = 10f..24f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryPurple,
                            activeTrackColor = PrimaryPurple,
                            inactiveTrackColor = DarkElevatedSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Word wrap
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Word Wrap",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Switch(
                            checked = uiState.isWordWrap,
                            onCheckedChange = { viewModel.setWordWrap(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = PrimaryPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Line numbers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show Line Numbers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Switch(
                            checked = uiState.showLineNumbers,
                            onCheckedChange = { viewModel.setLineNumbers(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = PrimaryPurple
                            )
                        )
                    }
                }
            }
        }

        // 4. Cache & Data Management
        item {
            Text(
                text = "Cache & Data Management",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Local SQLite (Room) Database",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Caches repositories, branches, and recent files for offline viewing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showClearCacheConfirm = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Cached Repositories")
                    }
                }
            }
        }

        // 5. Security Transparency
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = StatusSuccess,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hardware KeyStore Encryption",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All personal access tokens and secrets are encrypted on-device using AES-256 GCM backed by the Android hardware KeyStore. Tokens are never logged or stored in plain-text.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // App Version
        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Repo Manager Pro v1.0.0 (Native Android Compose)",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }
    }

    // Dialog: Configure GitHub Token
    if (showGitHubDialog) {
        GitHubConfigDialog(
            currentUsername = uiState.gitHubUsername,
            onDismiss = { showGitHubDialog = false },
            onSave = { token, user ->
                viewModel.saveGitHubToken(token, user)
                showGitHubDialog = false
            }
        )
    }

    // Dialog: Configure Cloudflare
    if (showCloudflareDialog) {
        CloudflareConfigDialog(
            currentAccountId = uiState.cloudflareAccountId,
            onDismiss = { showCloudflareDialog = false },
            onSave = { token, accountId ->
                viewModel.saveCloudflareCredentials(token, accountId)
                showCloudflareDialog = false
            }
        )
    }

    // Dialog: Configure Hugging Face
    if (showHuggingFaceDialog) {
        HuggingFaceConfigDialog(
            onDismiss = { showHuggingFaceDialog = false },
            onSave = { token ->
                viewModel.saveHuggingFaceToken(token)
                showHuggingFaceDialog = false
            }
        )
    }

    // Dialog: Clear Cache Confirmation
    if (showClearCacheConfirm) {
        AlertDialog(
            onDismissRequest = { showClearCacheConfirm = false },
            containerColor = DarkSurface,
            title = { Text("Clear Local Cache?", color = TextPrimary) },
            text = {
                Text("This will purge all locally cached repositories from SQLite Room storage. Unsaved changes should be committed first.", color = TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearLocalCache()
                        showClearCacheConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Clear Cache")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun IntegrationSettingCard(
    name: String,
    status: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    isConnected: Boolean,
    onConfigure: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isConnected) StatusSuccess else TextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onConfigure,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkElevatedSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isConnected) "Edit" else "Connect", color = SecondaryCyan, fontSize = 12.sp)
                }
                if (isConnected) {
                    IconButton(
                        onClick = onDisconnect,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Disconnect", tint = StatusError, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GitHubConfigDialog(
    currentUsername: String?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var token by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(currentUsername ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("GitHub Authentication", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter your GitHub Personal Access Token or OAuth Access Token. Token is encrypted in hardware KeyStore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("GitHub Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Personal Access Token (ghp_...)") },
                    modifier = Modifier.fillMaxWidth().testTag("github_token_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
                Text(
                    text = "Recommended scopes: 'repo' (Full control of private repositories) and 'read:user'.",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(token.trim(), username.trim()) },
                enabled = token.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Save Token")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun CloudflareConfigDialog(
    currentAccountId: String?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var token by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(currentAccountId ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Cloudflare API Integration", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Provide your Cloudflare API Token and Account ID to manage Workers and Pages deployments.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = accountId,
                    onValueChange = { accountId = it },
                    label = { Text("Account ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("API Token") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(token.trim(), accountId.trim()) },
                enabled = token.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Save Credentials")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun HuggingFaceConfigDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var token by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Hugging Face Integration", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter your Hugging Face User Access Token (hf_...) to query Space deployment status and metrics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("User Access Token (hf_...)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(token.trim()) },
                enabled = token.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Save Token")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
