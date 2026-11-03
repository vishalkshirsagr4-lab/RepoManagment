package com.example.updates

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val versionName: String,
    val apkUrl: String
)

object UpdateChecker {
    private const val RELEASE_API =
        "https://api.github.com/repos/vishalkshirsagr4-lab/RepoManagment/releases/latest"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun checkForUpdate(currentVersion: String): UpdateInfo? {
        return try {
            val request = Request.Builder()
                .url(RELEASE_API)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val json = JSONObject(response.body?.string() ?: return null)
                val tag = json.optString("tag_name")
                val latestVersion = tag.removePrefix("v")
                if (latestVersion.isBlank() ||
                    compareVersions(latestVersion, currentVersion) <= 0
                ) {
                    return null
                }

                val assets = json.optJSONArray("assets") ?: return null
                for (index in 0 until assets.length()) {
                    val asset = assets.optJSONObject(index) ?: continue
                    val name = asset.optString("name")
                    val downloadUrl = asset.optString("browser_download_url")
                    if (name.endsWith(".apk", ignoreCase = true) &&
                        downloadUrl.startsWith("https://")
                    ) {
                        return UpdateInfo(latestVersion, downloadUrl)
                    }
                }
            }
            null
        } catch (_: Exception) {
            // Update checks are optional; network/API errors must not block app startup.
            null
        }
    }

    private fun compareVersions(first: String, second: String): Int {
        val firstParts = first.split(".").map { it.toIntOrNull() ?: 0 }
        val secondParts = second.split(".").map { it.toIntOrNull() ?: 0 }
        val size = maxOf(firstParts.size, secondParts.size)

        for (index in 0 until size) {
            val a = firstParts.getOrElse(index) { 0 }
            val b = secondParts.getOrElse(index) { 0 }
            if (a != b) return a.compareTo(b)
        }
        return 0
    }
}
