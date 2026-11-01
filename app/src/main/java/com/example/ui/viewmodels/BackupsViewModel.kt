package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BackupRecordEntity
import com.example.data.repository.BackupRepository
import com.example.data.repository.RepoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupsUiState(
    val backups: List<BackupRecordEntity> = emptyList(),
    val isBackingUp: Boolean = false,
    val backupProgress: Float = 0f,
    val activeRepo: String? = null,
    val statusMessage: String? = null
)

class BackupsViewModel(application: Application) : AndroidViewModel(application) {
    private val repoRepository = RepoRepository(application)
    private val backupRepository = BackupRepository(application, repoRepository)

    private val _uiState = MutableStateFlow(BackupsUiState())
    val uiState: StateFlow<BackupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            backupRepository.getAllBackups().collect { list ->
                _uiState.value = _uiState.value.copy(backups = list)
            }
        }
    }

    fun startBackup(repoFullName: String, branch: String = "main") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBackingUp = true,
                backupProgress = 0f,
                activeRepo = repoFullName,
                statusMessage = "Downloading archive for $repoFullName..."
            )

            val result = backupRepository.createRepositoryBackup(
                repoFullName = repoFullName,
                branch = branch,
                onProgress = { progress ->
                    _uiState.value = _uiState.value.copy(backupProgress = progress)
                }
            )

            if (result.isSuccess) {
                val record = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isBackingUp = false,
                    backupProgress = 1f,
                    activeRepo = null,
                    statusMessage = "Archive saved: ${record.fileName} (${formatBytes(record.fileSizeBytes)})"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isBackingUp = false,
                    backupProgress = 0f,
                    activeRepo = null,
                    statusMessage = "Backup failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun deleteBackup(record: BackupRecordEntity) {
        viewModelScope.launch {
            backupRepository.deleteBackup(record)
            _uiState.value = _uiState.value.copy(statusMessage = "Backup deleted: ${record.fileName}")
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format("%.1f MB", mb)
    }
}
