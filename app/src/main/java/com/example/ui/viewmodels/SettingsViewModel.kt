package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val isDemoMode: Boolean = true,
    val gitHubUsername: String? = null,
    val hasGitHubToken: Boolean = false,
    val hasCloudflareToken: Boolean = false,
    val hasHuggingFaceToken: Boolean = false,
    val cloudflareAccountId: String? = null,
    val editorFontSize: Float = 13f,
    val isWordWrap: Boolean = false,
    val showLineNumbers: Boolean = true,
    val cacheClearedMessage: String? = null,
    val statusMessage: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val secureStorage = SecureStorage.getInstance(application)
    private val database = AppDatabase.getInstance(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        _uiState.value = _uiState.value.copy(
            isDemoMode = secureStorage.isDemoMode(),
            gitHubUsername = secureStorage.getGitHubUsername(),
            hasGitHubToken = !secureStorage.getGitHubToken().isNullOrBlank(),
            hasCloudflareToken = !secureStorage.getCloudflareToken().isNullOrBlank(),
            hasHuggingFaceToken = !secureStorage.getHuggingFaceToken().isNullOrBlank(),
            cloudflareAccountId = secureStorage.getCloudflareAccountId(),
            editorFontSize = secureStorage.getEditorFontSize(),
            isWordWrap = secureStorage.isWordWrapEnabled(),
            showLineNumbers = secureStorage.isLineNumbersEnabled()
        )
    }

    fun setDemoMode(enabled: Boolean) {
        secureStorage.setDemoMode(enabled)
        _uiState.value = _uiState.value.copy(
            isDemoMode = enabled,
            statusMessage = if (enabled) "Switched to Demo Mode" else "Switched to Live Provider Mode"
        )
    }

    fun saveGitHubToken(token: String?, username: String?) {
        secureStorage.setGitHubToken(token?.trim())
        if (!username.isNullOrBlank()) {
            secureStorage.setGitHubUsername(username.trim())
        }
        loadSettings()
        _uiState.value = _uiState.value.copy(
            statusMessage = if (token.isNullOrBlank()) "GitHub disconnected" else "GitHub credentials saved securely"
        )
    }

    fun saveCloudflareCredentials(token: String?, accountId: String?) {
        secureStorage.setCloudflareToken(token?.trim())
        secureStorage.setCloudflareAccountId(accountId?.trim())
        loadSettings()
        _uiState.value = _uiState.value.copy(
            statusMessage = if (token.isNullOrBlank()) "Cloudflare credentials removed" else "Cloudflare credentials saved securely"
        )
    }

    fun saveHuggingFaceToken(token: String?) {
        secureStorage.setHuggingFaceToken(token?.trim())
        loadSettings()
        _uiState.value = _uiState.value.copy(
            statusMessage = if (token.isNullOrBlank()) "Hugging Face credentials removed" else "Hugging Face credentials saved securely"
        )
    }

    fun setEditorFontSize(size: Float) {
        secureStorage.setEditorFontSize(size)
        _uiState.value = _uiState.value.copy(editorFontSize = size)
    }

    fun setWordWrap(wrap: Boolean) {
        secureStorage.setWordWrapEnabled(wrap)
        _uiState.value = _uiState.value.copy(isWordWrap = wrap)
    }

    fun setLineNumbers(lines: Boolean) {
        secureStorage.setLineNumbersEnabled(lines)
        _uiState.value = _uiState.value.copy(showLineNumbers = lines)
    }

    fun clearLocalCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.recentRepoDao().clearAll()
                database.recentFileDao().clearAll()
            }
            _uiState.value = _uiState.value.copy(statusMessage = "Local cached repositories and files cleared.")
        }
    }

    fun disconnectAllAccounts() {
        secureStorage.clearAllCredentials()
        loadSettings()
        _uiState.value = _uiState.value.copy(statusMessage = "All integration credentials disconnected.")
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null, cacheClearedMessage = null)
    }
}
