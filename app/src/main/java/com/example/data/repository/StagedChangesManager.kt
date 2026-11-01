package com.example.data.repository

import android.util.Base64
import com.example.data.remote.models.StagedChange
import com.example.data.remote.models.StagedChangeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object StagedChangesManager {
    // Map of repoFullName to list of staged changes
    private val _stagedMap = MutableStateFlow<Map<String, List<StagedChange>>>(emptyMap())
    val stagedMap: StateFlow<Map<String, List<StagedChange>>> = _stagedMap.asStateFlow()

    fun stageFile(
        repoFullName: String,
        filePath: String,
        branch: String = "main",
        content: String,
        isNewFile: Boolean = false,
        originalSha: String? = null
    ) {
        val cleanPath = filePath.trim('/')
        val currentList = _stagedMap.value[repoFullName]?.toMutableList() ?: mutableListOf()
        val existingIndex = currentList.indexOfFirst { it.filePath == cleanPath }

        val sizeBytes = content.toByteArray(Charsets.UTF_8).size.toLong()
        val change = StagedChange(
            repoFullName = repoFullName,
            filePath = cleanPath,
            branch = branch,
            type = if (isNewFile) StagedChangeType.CREATED else StagedChangeType.MODIFIED,
            textContent = content,
            base64Content = null,
            isBinary = false,
            originalSha = originalSha,
            fileSizeFormatted = formatFileSize(sizeBytes),
            timestamp = System.currentTimeMillis()
        )

        if (existingIndex >= 0) {
            currentList[existingIndex] = change
        } else {
            currentList.add(change)
        }

        _stagedMap.value = _stagedMap.value.toMutableMap().apply {
            put(repoFullName, currentList)
        }
    }

    fun stageBinaryFile(
        repoFullName: String,
        filePath: String,
        branch: String = "main",
        fileBytes: ByteArray
    ) {
        val cleanPath = filePath.trim('/')
        val currentList = _stagedMap.value[repoFullName]?.toMutableList() ?: mutableListOf()
        val existingIndex = currentList.indexOfFirst { it.filePath == cleanPath }

        val base64 = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
        val change = StagedChange(
            repoFullName = repoFullName,
            filePath = cleanPath,
            branch = branch,
            type = StagedChangeType.UPLOADED,
            textContent = null,
            base64Content = base64,
            isBinary = true,
            originalSha = null,
            fileSizeFormatted = formatFileSize(fileBytes.size.toLong()),
            timestamp = System.currentTimeMillis()
        )

        if (existingIndex >= 0) {
            currentList[existingIndex] = change
        } else {
            currentList.add(change)
        }

        _stagedMap.value = _stagedMap.value.toMutableMap().apply {
            put(repoFullName, currentList)
        }
    }

    fun stageDeletion(
        repoFullName: String,
        filePath: String,
        branch: String = "main",
        sha: String? = null
    ) {
        val cleanPath = filePath.trim('/')
        val currentList = _stagedMap.value[repoFullName]?.toMutableList() ?: mutableListOf()
        val existingIndex = currentList.indexOfFirst { it.filePath == cleanPath }

        val change = StagedChange(
            repoFullName = repoFullName,
            filePath = cleanPath,
            branch = branch,
            type = StagedChangeType.DELETED,
            textContent = null,
            base64Content = null,
            isBinary = false,
            originalSha = sha,
            fileSizeFormatted = "Deleted",
            timestamp = System.currentTimeMillis()
        )

        if (existingIndex >= 0) {
            currentList[existingIndex] = change
        } else {
            currentList.add(change)
        }

        _stagedMap.value = _stagedMap.value.toMutableMap().apply {
            put(repoFullName, currentList)
        }
    }

    fun stageRename(
        repoFullName: String,
        oldPath: String,
        newPath: String,
        branch: String = "main",
        content: String? = null,
        isBinary: Boolean = false,
        base64Content: String? = null
    ) {
        stageDeletion(repoFullName, oldPath, branch)
        if (isBinary && base64Content != null) {
            val bytes = Base64.decode(base64Content, Base64.DEFAULT)
            stageBinaryFile(repoFullName, newPath, branch, bytes)
        } else {
            stageFile(repoFullName, newPath, branch, content ?: "", isNewFile = true)
        }
    }

    fun unstageFile(repoFullName: String, filePath: String) {
        val cleanPath = filePath.trim('/')
        val currentList = _stagedMap.value[repoFullName]?.filterNot { it.filePath == cleanPath } ?: emptyList()
        _stagedMap.value = _stagedMap.value.toMutableMap().apply {
            put(repoFullName, currentList)
        }
    }

    fun clearStagedChanges(repoFullName: String) {
        _stagedMap.value = _stagedMap.value.toMutableMap().apply {
            remove(repoFullName)
        }
    }

    fun getStagedChanges(repoFullName: String): List<StagedChange> {
        return _stagedMap.value[repoFullName] ?: emptyList()
    }

    fun getStagedCount(repoFullName: String): Int {
        return _stagedMap.value[repoFullName]?.size ?: 0
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
