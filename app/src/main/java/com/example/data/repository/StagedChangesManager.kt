package com.example.data.repository

import android.content.Context
import android.util.Base64
import com.example.data.remote.models.StagedChange
import com.example.data.remote.models.StagedChangeType
import com.example.data.security.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * Encrypted, process-persistent staging area.
 *
 * Staged file contents can contain private source code, so the JSON snapshot is
 * encrypted by SecureStorage with an Android Keystore AES-GCM key before it is
 * written to SharedPreferences.
 */
object StagedChangesManager {
    private const val JSON_VERSION = 1

    private val lock = Any()
    private var secureStorage: SecureStorage? = null
    private val _stagedMap = MutableStateFlow<Map<String, List<StagedChange>>>(emptyMap())
    val stagedMap: StateFlow<Map<String, List<StagedChange>>> = _stagedMap.asStateFlow()

    /** Call once from Application/Activity before the editor is opened. */
    fun initialize(context: Context) {
        synchronized(lock) {
            if (secureStorage != null) return
            val storage = SecureStorage.getInstance(context.applicationContext)
            secureStorage = storage
            _stagedMap.value = decode(storage.getStagedChangesRaw())
        }
    }

    private fun requireStorage(): SecureStorage =
        checkNotNull(secureStorage) {
            "Staging storage is not initialized. Initialize StagedChangesManager from MainActivity first."
        }

    private fun updateRepo(repoFullName: String, transform: (MutableList<StagedChange>) -> Unit) {
        synchronized(lock) {
            val next = _stagedMap.value.toMutableMap()
            val items = next[repoFullName]?.toMutableList() ?: mutableListOf()
            transform(items)
            if (items.isEmpty()) next.remove(repoFullName) else next[repoFullName] = items
            _stagedMap.value = next
            persistLocked()
        }
    }

    fun stageFile(
        repoFullName: String,
        filePath: String,
        branch: String = "main",
        content: String,
        isNewFile: Boolean = false,
        originalSha: String? = null
    ) = updateRepo(repoFullName) { currentList ->
        val cleanPath = filePath.trim('/')
        val change = StagedChange(
            repoFullName = repoFullName,
            filePath = cleanPath,
            branch = branch,
            type = if (isNewFile) StagedChangeType.CREATED else StagedChangeType.MODIFIED,
            textContent = content,
            base64Content = null,
            isBinary = false,
            originalSha = originalSha,
            fileSizeFormatted = formatFileSize(content.toByteArray(Charsets.UTF_8).size.toLong()),
            timestamp = System.currentTimeMillis()
        )
        val index = currentList.indexOfFirst { it.filePath == cleanPath }
        if (index >= 0) currentList[index] = change else currentList.add(change)
    }

    fun stageBinaryFile(
        repoFullName: String,
        filePath: String,
        branch: String = "main",
        fileBytes: ByteArray
    ) = updateRepo(repoFullName) { currentList ->
        val cleanPath = filePath.trim('/')
        val change = StagedChange(
            repoFullName = repoFullName,
            filePath = cleanPath,
            branch = branch,
            type = StagedChangeType.UPLOADED,
            textContent = null,
            base64Content = Base64.encodeToString(fileBytes, Base64.NO_WRAP),
            isBinary = true,
            originalSha = null,
            fileSizeFormatted = formatFileSize(fileBytes.size.toLong()),
            timestamp = System.currentTimeMillis()
        )
        val index = currentList.indexOfFirst { it.filePath == cleanPath }
        if (index >= 0) currentList[index] = change else currentList.add(change)
    }

    fun stageDeletion(
        repoFullName: String,
        filePath: String,
        branch: String = "main",
        sha: String? = null
    ) = updateRepo(repoFullName) { currentList ->
        val cleanPath = filePath.trim('/')
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
        val index = currentList.indexOfFirst { it.filePath == cleanPath }
        if (index >= 0) currentList[index] = change else currentList.add(change)
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
        updateRepo(repoFullName) { it.removeAll { change -> change.filePath == cleanPath } }
    }

    fun clearStagedChanges(repoFullName: String) {
        synchronized(lock) {
            val next = _stagedMap.value.toMutableMap()
            next.remove(repoFullName)
            _stagedMap.value = next
            persistLocked()
        }
    }

    fun getStagedChanges(repoFullName: String): List<StagedChange> =
        _stagedMap.value[repoFullName] ?: emptyList()

    fun getStagedCount(repoFullName: String): Int = getStagedChanges(repoFullName).size

    private fun persistLocked() {
        val root = JSONObject().put("version", JSON_VERSION)
        val repos = JSONArray()
        _stagedMap.value.forEach { (repo, changes) ->
            val entries = JSONArray()
            changes.forEach { change ->
                entries.put(
                    JSONObject()
                        .put("repoFullName", change.repoFullName)
                        .put("filePath", change.filePath)
                        .put("branch", change.branch)
                        .put("type", change.type.name)
                        .put("textContent", change.textContent)
                        .put("base64Content", change.base64Content)
                        .put("isBinary", change.isBinary)
                        .put("originalSha", change.originalSha)
                        .put("fileSizeFormatted", change.fileSizeFormatted)
                        .put("timestamp", change.timestamp)
                )
            }
            repos.put(JSONObject().put("repo", repo).put("changes", entries))
        }
        root.put("repositories", repos)
        requireStorage().saveStagedChangesRaw(root.toString())
    }

    private fun decode(raw: String?): Map<String, List<StagedChange>> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val root = JSONObject(raw)
            if (root.optInt("version") != JSON_VERSION) return emptyMap()
            val result = mutableMapOf<String, List<StagedChange>>()
            val repos = root.optJSONArray("repositories") ?: return emptyMap()
            for (i in 0 until repos.length()) {
                val repoObject = repos.optJSONObject(i) ?: continue
                val repo = repoObject.optString("repo")
                if (repo.isBlank()) continue
                val entries = repoObject.optJSONArray("changes") ?: JSONArray()
                val changes = mutableListOf<StagedChange>()
                for (j in 0 until entries.length()) {
                    val item = entries.optJSONObject(j) ?: continue
                    val type = runCatching {
                        StagedChangeType.valueOf(item.optString("type"))
                    }.getOrNull() ?: continue
                    changes.add(
                        StagedChange(
                            repoFullName = item.optString("repoFullName", repo),
                            filePath = item.optString("filePath"),
                            branch = item.optString("branch", "main"),
                            type = type,
                            textContent = item.optString("textContent").takeUnless { it == "null" },
                            base64Content = item.optString("base64Content").takeUnless { it == "null" },
                            isBinary = item.optBoolean("isBinary"),
                            originalSha = item.optString("originalSha").takeUnless { it == "null" || it.isBlank() },
                            fileSizeFormatted = item.optString("fileSizeFormatted"),
                            timestamp = item.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                if (changes.isNotEmpty()) result[repo] = changes
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun formatFileSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}
