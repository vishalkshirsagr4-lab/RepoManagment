package com.example.data.remote.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CloudflareDeployment(
    val id: String = "",
    val project: String = "",
    val environment: String = "production",
    val status: String = "SUCCESS", // "SUCCESS", "PENDING", "FAILED"
    @Json(name = "created_on") val createdOn: String = "",
    val url: String? = null,
    val branch: String = "main",
    @Json(name = "short_hash") val shortHash: String = "c89b21"
)

@JsonClass(generateAdapter = true)
data class HuggingFaceSpace(
    val id: String = "",
    val author: String = "",
    val name: String = "",
    val title: String = "",
    val sdk: String = "gradio", // "gradio", "streamlit", "docker", "static"
    val stage: String = "RUNNING", // "RUNNING", "BUILDING", "PAUSED", "STOPPED", "ERROR"
    @Json(name = "last_modified") val lastModified: String = "",
    val likes: Int = 0
)

enum class DeploymentProvider {
    GITHUB_ACTIONS,
    CLOUDFLARE,
    HUGGING_FACE
}

data class UnifiedDeployment(
    val id: String,
    val provider: DeploymentProvider,
    val projectName: String,
    val status: String, // "RUNNING", "SUCCESS", "BUILDING", "FAILED", "QUEUED"
    val branch: String,
    val commitSha: String,
    val commitMessage: String,
    val deployedUrl: String?,
    val updatedAt: String,
    val logsExcerpt: String?
)
