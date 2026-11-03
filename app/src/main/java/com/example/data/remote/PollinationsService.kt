package com.example.data.remote

import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object PollinationsService {
    private const val TEXT_URL = "https://text.pollinations.ai/"
    private const val IMAGE_URL = "https://image.pollinations.ai/prompt/"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    data class ChatMessage(val role: String, val content: String)

    fun generateTextGet(prompt: String): String {
        val url = TEXT_URL + Uri.encode(prompt)
        val request = Request.Builder().url(url).get().build()
        return executeTextRequest(request)
    }

    fun generateTextPost(
        messages: List<ChatMessage>,
        model: String,
        seed: Long?
    ): String {
        val jsonMessages = JSONArray()
        messages.forEach { message ->
            jsonMessages.put(
                JSONObject()
                    .put("role", message.role)
                    .put("content", message.content)
            )
        }

        val payload = JSONObject()
            .put("messages", jsonMessages)
            .put("model", model.ifBlank { "openai" })
        seed?.let { payload.put("seed", it) }

        val body = payload.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(TEXT_URL)
            .post(body)
            .header("Content-Type", "application/json")
            .build()
        return executeTextRequest(request)
    }

    fun buildImageUrl(
        prompt: String,
        width: Int,
        height: Int,
        model: String,
        seed: Long?,
        noLogo: Boolean
    ): String {
        val query = mutableListOf(
            "width=$width",
            "height=$height",
            "model=${Uri.encode(model.ifBlank { "flux" })}",
            "nologo=$noLogo"
        )
        seed?.let { query.add("seed=$it") }
        return IMAGE_URL + Uri.encode(prompt) + "?" + query.joinToString("&")
    }

    private fun executeTextRequest(request: Request): String {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    when (response.code) {
                        429 -> "Pollinations rate limit reached. Please wait and try again."
                        in 500..599 -> "Pollinations service is temporarily unavailable. Try again later."
                        else -> "Pollinations request failed (${response.code}): ${body.take(300)}"
                    }
                )
            }

            // Most text responses are plain text. Also support OpenAI-compatible JSON responses.
            return try {
                val json = JSONObject(body)
                json.optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")
                    ?.takeIf { it.isNotBlank() }
                    ?: body
            } catch (_: Exception) {
                body
            }
        }
    }
}
