package com.example.data.repository

import android.content.Context
import com.example.data.demo.DemoData
import com.example.data.remote.models.DeploymentProvider
import com.example.data.remote.models.UnifiedDeployment
import com.example.data.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class DeploymentRepository(
    private val context: Context,
    private val secureStorage: SecureStorage = SecureStorage.getInstance(context)
) {
    private val _deployments = MutableStateFlow<List<UnifiedDeployment>>(DemoData.demoDeployments)
    val deployments: Flow<List<UnifiedDeployment>> = _deployments.asStateFlow()

    fun isCloudflareConnected(): Boolean {
        return !secureStorage.getCloudflareToken().isNullOrBlank()
    }

    fun isHuggingFaceConnected(): Boolean {
        return !secureStorage.getHuggingFaceToken().isNullOrBlank()
    }

    fun isGitHubConnected(): Boolean {
        return !secureStorage.getGitHubToken().isNullOrBlank()
    }

    suspend fun refreshDeployments(): Result<List<UnifiedDeployment>> = withContext(Dispatchers.IO) {
        delay(400) // Brief network cycle simulation
        Result.success(_deployments.value)
    }

    suspend fun triggerDeployment(deploymentId: String): Result<UnifiedDeployment> = withContext(Dispatchers.IO) {
        val currentList = _deployments.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == deploymentId }
        if (index != -1) {
            val item = currentList[index]
            val updated = item.copy(
                status = "BUILDING",
                updatedAt = "Just now",
                logsExcerpt = "Rebuilding ${item.projectName}... Starting container image pull."
            )
            currentList[index] = updated
            _deployments.value = currentList
            return@withContext Result.success(updated)
        }
        Result.failure(Exception("Deployment not found"))
    }
}
