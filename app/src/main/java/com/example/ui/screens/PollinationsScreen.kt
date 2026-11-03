package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.remote.PollinationsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class AiMode { TEXT, IMAGE }

@Composable
fun PollinationsScreen() {
    var mode by remember { mutableStateOf(AiMode.TEXT) }
    var prompt by remember { mutableStateOf("") }
    var systemPrompt by remember {
        mutableStateOf("You are a helpful assistant. Give clear and accurate answers.")
    }
    var model by remember { mutableStateOf("openai") }
    var requestMethod by remember { mutableStateOf("POST") }
    var seed by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var width by remember { mutableStateOf("1024") }
    var height by remember { mutableStateOf("1024") }
    var imageModel by remember { mutableStateOf("flux") }
    var noLogo by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val chat = remember { mutableStateListOf<PollinationsService.ChatMessage>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Pollinations AI", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Free AI text and image generation. No API key required. Availability and speed may vary.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mode == AiMode.TEXT,
                onClick = { mode = AiMode.TEXT; error = null },
                label = { Text("Text AI") },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Default.AutoAwesome, contentDescription = null) }
            )
            FilterChip(
                selected = mode == AiMode.IMAGE,
                onClick = { mode = AiMode.IMAGE; error = null },
                label = { Text("Image AI") },
                leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Image, contentDescription = null) }
            )
        }

        if (mode == AiMode.TEXT) {
            Text("Text generation", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = requestMethod == "GET", onClick = { requestMethod = "GET" }, label = { Text("Simple GET") })
                FilterChip(selected = requestMethod == "POST", onClick = { requestMethod = "POST" }, label = { Text("Chat POST") })
            }
            if (requestMethod == "POST") {
                OutlinedTextField(
                    value = model, onValueChange = { model = it },
                    label = { Text("Text model (e.g. openai, mistral, llama, search)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = systemPrompt, onValueChange = { systemPrompt = it },
                    label = { Text("System instructions") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2
                )
                OutlinedTextField(
                    value = seed, onValueChange = { seed = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text("Seed (optional integer)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                if (chat.isNotEmpty()) {
                    Text("Conversation", style = MaterialTheme.typography.titleMedium)
                    chat.forEach { message ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(Modifier.fillMaxWidth().padding(10.dp)) {
                                Text(if (message.role == "assistant") "AI" else "You", fontWeight = FontWeight.Bold)
                                Text(message.content)
                            }
                        }
                    }
                    TextButton(onClick = { chat.clear(); answer = ""; error = null }) { Text("Clear conversation") }
                }
            }
            OutlinedTextField(
                value = prompt, onValueChange = { prompt = it },
                label = { Text("Ask anything...") },
                modifier = Modifier.fillMaxWidth(), minLines = 3
            )
            Button(
                onClick = {
                    if (prompt.isBlank() || loading) return@Button
                    loading = true
                    error = null
                    answer = ""
                    val userPrompt = prompt.trim()
                    prompt = ""
                    scope.launch {
                        try {
                            val result = withContext(Dispatchers.IO) {
                                if (requestMethod == "GET") {
                                    PollinationsService.generateTextGet(userPrompt)
                                } else {
                                    val messages = buildList {
                                        add(PollinationsService.ChatMessage("system", systemPrompt))
                                        addAll(chat)
                                        add(PollinationsService.ChatMessage("user", userPrompt))
                                    }
                                    PollinationsService.generateTextPost(
                                        messages, model, seed.toLongOrNull()
                                    )
                                }
                            }
                            answer = result
                            if (requestMethod == "POST") {
                                chat.add(PollinationsService.ChatMessage("user", userPrompt))
                                chat.add(PollinationsService.ChatMessage("assistant", result))
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Could not generate text. Check your connection."
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading && prompt.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
                else androidx.compose.material3.Icon(Icons.Default.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generate text")
            }
            if (answer.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text("Response", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(answer)
                        TextButton(onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Pollinations response", answer))
                        }) { Text("Copy response") }
                    }
                }
            }
        } else {
            Text("Image generation", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = prompt, onValueChange = { prompt = it },
                label = { Text("Describe the image you want") },
                modifier = Modifier.fillMaxWidth(), minLines = 3
            )
            OutlinedTextField(
                value = imageModel, onValueChange = { imageModel = it },
                label = { Text("Image model (e.g. flux, turbo)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = width, onValueChange = { width = it.filter(Char::isDigit).take(4) },
                    label = { Text("Width") }, modifier = Modifier.weight(1f), singleLine = true
                )
                OutlinedTextField(
                    value = height, onValueChange = { height = it.filter(Char::isDigit).take(4) },
                    label = { Text("Height") }, modifier = Modifier.weight(1f), singleLine = true
                )
            }
            OutlinedTextField(
                value = seed, onValueChange = { seed = it.filter { c -> c.isDigit() || c == '-' } },
                label = { Text("Seed (optional integer)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Remove Pollinations logo", modifier = Modifier.weight(1f))
                Switch(checked = noLogo, onCheckedChange = { noLogo = it })
            }
            Button(
                onClick = {
                    val w = width.toIntOrNull()
                    val h = height.toIntOrNull()
                    if (prompt.isBlank() || w == null || h == null || w !in 128..2048 || h !in 128..2048 || loading) {
                        error = "Enter a prompt and valid width/height between 128 and 2048."
                        return@Button
                    }
                    loading = true
                    error = null
                    imageUrl = null
                    scope.launch {
                        try {
                            imageUrl = withContext(Dispatchers.IO) {
                                PollinationsService.buildImageUrl(
                                    prompt.trim(), w, h, imageModel, seed.toLongOrNull(), noLogo
                                )
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Could not prepare image request."
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading && prompt.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
                else androidx.compose.material3.Icon(Icons.Default.Image, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generate image")
            }
            imageUrl?.let { url ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(10.dp)) {
                        AsyncImage(
                            model = url,
                            contentDescription = "AI generated image",
                            modifier = Modifier.fillMaxWidth().height(280.dp)
                        )
                        TextButton(onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Generated image URL", url))
                        }) { Text("Copy image link") }
                    }
                }
            }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Privacy: prompts are sent to Pollinations.ai, a public third-party service. Do not submit passwords, private tokens, or sensitive personal information. Free service limits and model availability can change.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
