package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.models.GitHubRepo
import com.example.data.remote.models.GitHubUser
import com.example.data.remote.models.UnifiedDeployment
import com.example.data.repository.BackupRepository
import com.example.data.repository.DeploymentRepository
import com.example.data.repository.RepoRepository
import com.example.data.security.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val user: GitHubUser? = null,
    val repos: List<GitHubRepo> = emptyList(),
    val deployments: List<UnifiedDeployment> = emptyList(),
    val backupCount: Int = 0,
    val isDemoMode: Boolean = false,
    val isGitHubConnected: Boolean = false,
    val isCloudflareConnected: Boolean = false,
    val isHuggingFaceConnected: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repoRepository = RepoRepository(application)
    private val deploymentRepository = DeploymentRepository(application)
    private val backupRepository = BackupRepository(application, repoRepository)
    private val secureStorage = SecureStorage.getInstance(application)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val isDemo = secureStorage.isDemoMode()
            val ghConnected = !secureStorage.getGitHubToken().isNullOrBlank()
            val cfConnected = !secureStorage.getCloudflareToken().isNullOrBlank()
            val hfConnected = !secureStorage.getHuggingFaceToken().isNullOrBlank()

            val userResult = repoRepository.getCurrentUser()
            val reposResult = repoRepository.getRepositories()

            val user = userResult.getOrNull()
            val repos = reposResult.getOrDefault(emptyList())

            deploymentRepository.deployments.collect { deps ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    repos = repos,
                    deployments = deps,
                    isDemoMode = isDemo,
                    isGitHubConnected = ghConnected,
                    isCloudflareConnected = cfConnected,
                    isHuggingFaceConnected = hfConnected
                )
            }
        }
    }

    fun connectGitHub(token: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repoRepository.validateTokenAndFetchUser(token.trim())
            if (result.isSuccess) {
                val user = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isGitHubConnected = true,
                    isDemoMode = false,
                    statusMessage = "Connected to GitHub as @${user.login}"
                )
                loadDashboardData()
                onDone(true, user.login)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Invalid GitHub token"
                )
                onDone(false, result.exceptionOrNull()?.message ?: "Verification failed")
            }
        }
    }

    fun toggleDemoMode(enabled: Boolean) {
        secureStorage.setDemoMode(enabled)
        loadDashboardData()
    }
}
