package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.models.GitHubRepo
import com.example.data.remote.models.StagedChange
import com.example.data.repository.RepoRepository
import com.example.data.repository.StagedChangesManager
import com.example.data.security.SecureStorage
import com.example.ui.editor.DiffEngine
import com.example.ui.editor.DiffSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditorTab(
    val id: String, // repoFullName:branch:filePath
    val repoFullName: String,
    val filePath: String,
    val fileName: String,
    val branch: String,
    val sha: String?,
    val language: String,
    val content: String,
    val originalContent: String,
    val isDirty: Boolean
)

data class EditorUiState(
    val isLoading: Boolean = false,
    val tabs: List<EditorTab> = emptyList(),
    val activeTabId: String? = null,
    val availableRepos: List<GitHubRepo> = emptyList(),
    val currentRepo: String = "octodev/repomanager-core",
    val isCompareMode: Boolean = false,
    val diffSummary: DiffSummary? = null,
    val searchQuery: String = "",
    val replaceQuery: String = "",
    val isSearching: Boolean = false,
    val isReplacing: Boolean = false,
    val isMatchCase: Boolean = false,
    val isWholeWord: Boolean = false,
    val isRegex: Boolean = false,
    val isMultilineExpanded: Boolean = false,
    val searchMatchCount: Int = 0,
    val currentMatchIndex: Int = 0,
    val currentMatchLine: Int = 1,
    val cursorLine: Int = 1,
    val cursorColumn: Int = 1,
    val fontSizeSp: Float = 13f,
    val isWordWrap: Boolean = false,
    val showLineNumbers: Boolean = true,
    val stagedChanges: List<StagedChange> = emptyList(),
    val isBatchCommitting: Boolean = false,
    val batchCommitDialogVisible: Boolean = false,
    val isCommitting: Boolean = false,
    val commitDialogVisible: Boolean = false,
    val discardDialogVisible: Boolean = false,
    val repoPickerVisible: Boolean = false,
    val latestPushResult: com.example.data.remote.models.PushResult? = null,
    val pushStatusDialogVisible: Boolean = false,
    val isTrackingBuild: Boolean = false,
    val latestWorkflowRun: com.example.data.remote.models.GitHubWorkflowRun? = null,
    val latestWorkflowJobs: List<com.example.data.remote.models.WorkflowJob> = emptyList(),
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val activeTab: EditorTab?
        get() = tabs.find { it.id == activeTabId }
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repoRepository = RepoRepository(application)
    private val secureStorage = SecureStorage.getInstance(application)

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStackMap = mutableMapOf<String, MutableList<String>>()

    init {
        val fontSize = secureStorage.getEditorFontSize()
        val wrap = secureStorage.isWordWrapEnabled()
        val lines = secureStorage.isLineNumbersEnabled()
        _uiState.value = _uiState.value.copy(
            fontSizeSp = fontSize,
            isWordWrap = wrap,
            showLineNumbers = lines
        )

        loadAvailableRepos()

        viewModelScope.launch {
            StagedChangesManager.stagedMap.collect { map ->
                val repo = _uiState.value.currentRepo
                _uiState.value = _uiState.value.copy(
                    stagedChanges = map[repo] ?: emptyList()
                )
            }
        }

        // Open initial default file
        openFile("octodev/repomanager-core", "src/main/kotlin/RepositoryManager.kt", "main")
    }

    fun loadAvailableRepos() {
        viewModelScope.launch {
            val result = repoRepository.getRepositories()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(availableRepos = result.getOrThrow())
            }
        }
    }

    fun openFile(repoFullName: String, path: String, branch: String) {
        val tabId = "$repoFullName:$branch:$path"
        val existingIndex = _uiState.value.tabs.indexOfFirst { it.id == tabId }

        if (existingIndex != -1) {
            _uiState.value = _uiState.value.copy(
                activeTabId = tabId,
                currentRepo = repoFullName,
                isCompareMode = false
            )
            updateDiffForActiveTab()
            return
        }

        val parts = repoFullName.split("/")
        val owner = parts.getOrNull(0) ?: "octodev"
        val repo = parts.getOrNull(1) ?: "repomanager-core"
        val ext = path.substringAfterLast('.', "kt")
        val fileName = path.substringAfterLast('/')

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repoRepository.getFileContent(owner, repo, path, branch)
            val (text, sha) = result.getOrDefault(Pair("", null))

            val newTab = EditorTab(
                id = tabId,
                repoFullName = repoFullName,
                filePath = path,
                fileName = fileName,
                branch = branch,
                sha = sha,
                language = ext,
                content = text,
                originalContent = text,
                isDirty = false
            )

            val updatedTabs = _uiState.value.tabs + newTab
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                tabs = updatedTabs,
                activeTabId = tabId,
                currentRepo = repoFullName,
                isCompareMode = false
            )
            updateDiffForActiveTab()
        }
    }

    fun selectTab(tabId: String) {
        _uiState.value = _uiState.value.copy(
            activeTabId = tabId,
            currentRepo = _uiState.value.tabs.find { it.id == tabId }?.repoFullName ?: _uiState.value.currentRepo
        )
        updateDiffForActiveTab()
    }

    fun closeTab(tabId: String) {
        val currentTabs = _uiState.value.tabs.toMutableList()
        val index = currentTabs.indexOfFirst { it.id == tabId }
        if (index != -1) {
            currentTabs.removeAt(index)
            val newActive = if (currentTabs.isEmpty()) null
            else if (_uiState.value.activeTabId == tabId) {
                currentTabs.getOrNull(index.coerceAtMost(currentTabs.lastIndex))?.id
            } else {
                _uiState.value.activeTabId
            }

            _uiState.value = _uiState.value.copy(
                tabs = currentTabs,
                activeTabId = newActive
            )
            updateDiffForActiveTab()
        }
    }

    fun onContentChange(newText: String) {
        val active = _uiState.value.activeTab ?: return
        val tabId = active.id

        val stack = undoStackMap.getOrPut(tabId) { mutableListOf() }
        if (active.content != newText) {
            if (stack.size > 50) stack.removeAt(0)
            stack.add(active.content)
        }

        val isDirty = newText != active.originalContent
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == tabId) tab.copy(content = newText, isDirty = isDirty) else tab
        }

        _uiState.value = _uiState.value.copy(tabs = updatedTabs)
        updateSearchMatches(_uiState.value.searchQuery, newText)
        updateDiffForActiveTab()
    }

    fun updateCursor(line: Int, column: Int) {
        _uiState.value = _uiState.value.copy(cursorLine = line, cursorColumn = column)
    }

    fun undo() {
        val active = _uiState.value.activeTab ?: return
        val stack = undoStackMap[active.id]
        if (!stack.isNullOrEmpty()) {
            val prev = stack.removeAt(stack.lastIndex)
            val isDirty = prev != active.originalContent
            val updatedTabs = _uiState.value.tabs.map { tab ->
                if (tab.id == active.id) tab.copy(content = prev, isDirty = isDirty) else tab
            }
            _uiState.value = _uiState.value.copy(tabs = updatedTabs)
            updateDiffForActiveTab()
        }
    }

    // Insert Code Snippet / Tokens at cursor (VS Code helper bar)
    fun insertSnippet(snippet: String) {
        val active = _uiState.value.activeTab ?: return
        val newContent = active.content + snippet
        onContentChange(newContent)
    }

    // Toggle Git Compare Mode (Diff View)
    fun toggleCompareMode() {
        val newMode = !_uiState.value.isCompareMode
        _uiState.value = _uiState.value.copy(isCompareMode = newMode)
        if (newMode) {
            updateDiffForActiveTab()
        }
    }

    private fun updateDiffForActiveTab() {
        val active = _uiState.value.activeTab ?: return
        val summary = DiffEngine.computeDiff(active.originalContent, active.content)
        _uiState.value = _uiState.value.copy(diffSummary = summary)
    }

    // VS Code-style Search and Replace
    fun toggleSearch(visible: Boolean) {
        _uiState.value = _uiState.value.copy(
            isSearching = visible,
            searchQuery = if (!visible) "" else _uiState.value.searchQuery,
            isReplacing = false
        )
    }

    fun toggleReplace() {
        val next = !_uiState.value.isReplacing
        _uiState.value = _uiState.value.copy(
            isReplacing = next,
            isSearching = true
        )
    }

    fun toggleMatchCase() {
        val next = !_uiState.value.isMatchCase
        _uiState.value = _uiState.value.copy(isMatchCase = next)
        val text = _uiState.value.activeTab?.content ?: ""
        updateSearchMatches(_uiState.value.searchQuery, text)
    }

    fun toggleWholeWord() {
        val next = !_uiState.value.isWholeWord
        _uiState.value = _uiState.value.copy(isWholeWord = next)
        val text = _uiState.value.activeTab?.content ?: ""
        updateSearchMatches(_uiState.value.searchQuery, text)
    }

    fun toggleRegex() {
        val next = !_uiState.value.isRegex
        _uiState.value = _uiState.value.copy(isRegex = next)
        val text = _uiState.value.activeTab?.content ?: ""
        updateSearchMatches(_uiState.value.searchQuery, text)
    }

    fun toggleMultilineExpanded() {
        _uiState.value = _uiState.value.copy(isMultilineExpanded = !_uiState.value.isMultilineExpanded)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        val text = _uiState.value.activeTab?.content ?: ""
        updateSearchMatches(query, text)
    }

    fun onReplaceQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(replaceQuery = query)
    }

    private fun buildSearchRegex(query: String): Regex? {
        if (query.isEmpty()) return null
        return try {
            val pattern = when {
                _uiState.value.isRegex -> query
                _uiState.value.isWholeWord -> "\\b${Regex.escape(query)}\\b"
                else -> Regex.escape(query)
            }
            val options = mutableSetOf<RegexOption>()
            if (!_uiState.value.isMatchCase) {
                options.add(RegexOption.IGNORE_CASE)
            }
            if (_uiState.value.isMultilineExpanded) {
                options.add(RegexOption.MULTILINE)
            }
            Regex(pattern, options)
        } catch (_: Exception) {
            null
        }
    }

    private fun updateSearchMatches(query: String, text: String) {
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                searchMatchCount = 0,
                currentMatchIndex = 0,
                currentMatchLine = 1
            )
            return
        }
        val regex = buildSearchRegex(query)
        if (regex == null) {
            _uiState.value = _uiState.value.copy(
                searchMatchCount = 0,
                currentMatchIndex = 0,
                currentMatchLine = 1
            )
            return
        }
        val matches = regex.findAll(text).toList()
        val firstMatchStart = matches.firstOrNull()?.range?.first ?: 0
        val line = text.take(firstMatchStart).count { it == '\n' } + 1
        _uiState.value = _uiState.value.copy(
            searchMatchCount = matches.size,
            currentMatchIndex = if (matches.isNotEmpty()) 1 else 0,
            currentMatchLine = line
        )
    }

    private fun updateSelectedMatchLine(index: Int) {
        val active = _uiState.value.activeTab ?: return
        val regex = buildSearchRegex(_uiState.value.searchQuery) ?: return
        val matches = regex.findAll(active.content).toList()
        val match = matches.getOrNull(index - 1) ?: return
        val line = active.content.take(match.range.first).count { it == '\n' } + 1
        _uiState.value = _uiState.value.copy(currentMatchLine = line)
    }

    fun replaceCurrent() {
        val active = _uiState.value.activeTab ?: return
        val query = _uiState.value.searchQuery
        val replace = _uiState.value.replaceQuery
        if (query.isEmpty()) return

        val text = active.content
        val regex = buildSearchRegex(query) ?: return
        val matches = regex.findAll(text).toList()
        if (matches.isNotEmpty()) {
            val targetIndex = (_uiState.value.currentMatchIndex - 1).coerceIn(0, matches.lastIndex)
            val match = matches[targetIndex]
            val newText = text.replaceRange(match.range, replace)
            onContentChange(newText)
            _uiState.value = _uiState.value.copy(statusMessage = "Replaced 1 match in ${active.fileName}")
        }
    }

    fun replaceAll() {
        val active = _uiState.value.activeTab ?: return
        val query = _uiState.value.searchQuery
        val replace = _uiState.value.replaceQuery
        if (query.isEmpty()) return

        val text = active.content
        val regex = buildSearchRegex(query) ?: return
        val count = regex.findAll(text).count()
        if (count > 0) {
            val newText = regex.replace(text, replace)
            onContentChange(newText)
            _uiState.value = _uiState.value.copy(statusMessage = "Replaced $count occurrences in ${active.fileName}")
        } else {
            _uiState.value = _uiState.value.copy(statusMessage = "No matches found to replace")
        }
    }

    fun nextSearchMatch() {
        val total = _uiState.value.searchMatchCount
        if (total > 0) {
            val curr = _uiState.value.currentMatchIndex
            val next = if (curr >= total) 1 else curr + 1
            _uiState.value = _uiState.value.copy(currentMatchIndex = next)
            updateSelectedMatchLine(next)
        }
    }

    fun prevSearchMatch() {
        val total = _uiState.value.searchMatchCount
        if (total > 0) {
            val curr = _uiState.value.currentMatchIndex
            val prev = if (curr <= 1) total else curr - 1
            _uiState.value = _uiState.value.copy(currentMatchIndex = prev)
            updateSelectedMatchLine(prev)
        }
    }

    fun toggleWordWrap() {
        val newWrap = !_uiState.value.isWordWrap
        secureStorage.setWordWrapEnabled(newWrap)
        _uiState.value = _uiState.value.copy(isWordWrap = newWrap)
    }

    fun toggleLineNumbers() {
        val newLines = !_uiState.value.showLineNumbers
        secureStorage.setLineNumbersEnabled(newLines)
        _uiState.value = _uiState.value.copy(showLineNumbers = newLines)
    }

    fun setFontSize(size: Float) {
        val clamped = size.coerceIn(10f, 24f)
        secureStorage.setEditorFontSize(clamped)
        _uiState.value = _uiState.value.copy(fontSizeSp = clamped)
    }

    fun showCommitDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(commitDialogVisible = show)
    }

    fun showDiscardDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(discardDialogVisible = show)
    }

    fun showRepoPicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(repoPickerVisible = show)
    }

    fun selectRepo(repoFullName: String) {
        _uiState.value = _uiState.value.copy(
            currentRepo = repoFullName,
            repoPickerVisible = false,
            stagedChanges = StagedChangesManager.getStagedChanges(repoFullName)
        )
        // Load default README or root file for selected repo
        openFile(repoFullName, "README.md", "main")
    }

    fun discardChanges() {
        val active = _uiState.value.activeTab ?: return
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == active.id) tab.copy(content = tab.originalContent, isDirty = false) else tab
        }
        _uiState.value = _uiState.value.copy(
            tabs = updatedTabs,
            discardDialogVisible = false,
            statusMessage = "Reverted changes to ${active.fileName}"
        )
        undoStackMap[active.id]?.clear()
        updateDiffForActiveTab()
    }

    fun commitChanges(commitMessage: String, targetBranch: String) {
        val active = _uiState.value.activeTab ?: return
        val parts = active.repoFullName.split("/")
        val owner = parts.getOrNull(0) ?: "octodev"
        val repo = parts.getOrNull(1) ?: "repomanager-core"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCommitting = true, errorMessage = null)
            val result = repoRepository.saveAndCommitFile(
                owner = owner,
                repo = repo,
                path = active.filePath,
                content = active.content,
                commitMessage = commitMessage.ifBlank { "Update ${active.fileName}" },
                sha = active.sha,
                branch = targetBranch.ifBlank { active.branch }
            )

            if (result.isSuccess) {
                val pushResult = result.getOrThrow()
                val updatedTabs = _uiState.value.tabs.map { tab ->
                    if (tab.id == active.id) {
                        tab.copy(isDirty = false, originalContent = tab.content, branch = targetBranch)
                    } else tab
                }
                _uiState.value = _uiState.value.copy(
                    isCommitting = false,
                    commitDialogVisible = false,
                    tabs = updatedTabs,
                    statusMessage = pushResult.message,
                    latestPushResult = pushResult,
                    pushStatusDialogVisible = true,
                    isTrackingBuild = true
                )
                updateDiffForActiveTab()
                pollWorkflowStatusForPush(pushResult)
            } else {
                _uiState.value = _uiState.value.copy(
                    isCommitting = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Commit failed"
                )
            }
        }
    }

    fun pollWorkflowStatusForPush(pushResult: com.example.data.remote.models.PushResult) {
        val parts = pushResult.repoFullName.split("/")
        val owner = parts.getOrNull(0) ?: "octodev"
        val repo = parts.getOrNull(1) ?: "repomanager-core"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTrackingBuild = true)
            val runResult = repoRepository.getLatestWorkflowRunForCommit(owner, repo, pushResult.commitSha)
            val run = runResult.getOrNull()
            var jobs: List<com.example.data.remote.models.WorkflowJob> = emptyList()
            if (run != null) {
                val jobsResult = repoRepository.getWorkflowJobs(owner, repo, run.id)
                jobs = jobsResult.getOrDefault(emptyList())
            }
            _uiState.value = _uiState.value.copy(
                isTrackingBuild = false,
                latestWorkflowRun = run,
                latestWorkflowJobs = jobs
            )
        }
    }

    fun refreshWorkflowLogs() {
        val push = _uiState.value.latestPushResult ?: return
        pollWorkflowStatusForPush(push)
    }

    fun dismissPushStatusDialog() {
        _uiState.value = _uiState.value.copy(pushStatusDialogVisible = false)
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null, errorMessage = null)
    }

    fun stageActiveFile() {
        val active = _uiState.value.activeTab ?: return
        StagedChangesManager.stageFile(
            repoFullName = active.repoFullName,
            filePath = active.filePath,
            branch = active.branch,
            content = active.content,
            isNewFile = false,
            originalSha = active.sha
        )
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == active.id) tab.copy(isDirty = false, originalContent = active.content) else tab
        }
        _uiState.value = _uiState.value.copy(
            tabs = updatedTabs,
            statusMessage = "Staged ${active.fileName} for one-time batch commit"
        )
        updateDiffForActiveTab()
    }

    fun renameActiveTab(
        newFileName: String,
        commitMessage: String,
        onSuccess: () -> Unit = {}
    ) {
        val active = _uiState.value.activeTab ?: return
        val repoFullName = active.repoFullName
        val parts = repoFullName.split("/")
        val owner = parts.getOrNull(0) ?: "octodev"
        val repo = parts.getOrNull(1) ?: "repomanager-core"

        val parentPath = if (active.filePath.contains("/")) active.filePath.substringBeforeLast('/') else ""
        val newPath = if (parentPath.isBlank()) newFileName else "$parentPath/$newFileName"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCommitting = true, errorMessage = null)
            val result = repoRepository.renameFile(
                owner = owner,
                repo = repo,
                oldPath = active.filePath,
                newPath = newPath,
                commitMessage = commitMessage.ifBlank { "Rename ${active.fileName} to $newFileName" },
                branch = active.branch
            )

            if (result.isSuccess) {
                val pushResult = result.getOrThrow()
                val newTabId = "$repoFullName:${active.branch}:$newPath"
                val updatedTabs = _uiState.value.tabs.map { tab ->
                    if (tab.id == active.id) {
                        tab.copy(
                            id = newTabId,
                            filePath = newPath,
                            fileName = newFileName,
                            isDirty = false
                        )
                    } else tab
                }
                _uiState.value = _uiState.value.copy(
                    isCommitting = false,
                    tabs = updatedTabs,
                    activeTabId = newTabId,
                    statusMessage = pushResult.message,
                    latestPushResult = pushResult,
                    pushStatusDialogVisible = true,
                    isTrackingBuild = true
                )
                pollWorkflowStatusForPush(pushResult)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isCommitting = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Rename failed"
                )
            }
        }
    }

    fun stageRenameActiveTab(
        newFileName: String,
        onSuccess: () -> Unit = {}
    ) {
        val active = _uiState.value.activeTab ?: return
        val repoFullName = active.repoFullName
        val parentPath = if (active.filePath.contains("/")) active.filePath.substringBeforeLast('/') else ""
        val newPath = if (parentPath.isBlank()) newFileName else "$parentPath/$newFileName"

        StagedChangesManager.stageRename(
            repoFullName = repoFullName,
            oldPath = active.filePath,
            newPath = newPath,
            branch = active.branch
        )

        val newTabId = "$repoFullName:${active.branch}:$newPath"
        val updatedTabs = _uiState.value.tabs.map { tab ->
            if (tab.id == active.id) {
                tab.copy(
                    id = newTabId,
                    filePath = newPath,
                    fileName = newFileName
                )
            } else tab
        }

        _uiState.value = _uiState.value.copy(
            tabs = updatedTabs,
            activeTabId = newTabId,
            statusMessage = "Staged rename: ${active.fileName} -> $newFileName for batch commit"
        )
        onSuccess()
    }

    fun unstageFile(filePath: String) {
        StagedChangesManager.unstageFile(_uiState.value.currentRepo, filePath)
    }

    fun clearAllStaged() {
        StagedChangesManager.clearStagedChanges(_uiState.value.currentRepo)
    }

    fun showBatchCommitDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(batchCommitDialogVisible = show)
    }

    fun executeBatchCommitAndPush(
        commitTitle: String,
        commitDescription: String?,
        branch: String
    ) {
        val repoFullName = _uiState.value.currentRepo
        val parts = repoFullName.split("/")
        val owner = parts.getOrNull(0) ?: "octodev"
        val repo = parts.getOrNull(1) ?: "repomanager-core"
        val staged = StagedChangesManager.getStagedChanges(repoFullName)

        if (staged.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No staged changes to commit.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBatchCommitting = true, errorMessage = null)
            val result = repoRepository.batchCommitAndPush(
                owner = owner,
                repo = repo,
                branch = branch,
                commitTitle = commitTitle.ifBlank { "Batch update ${staged.size} files" },
                commitDescription = commitDescription,
                changes = staged
            )

            if (result.isSuccess) {
                val pushResult = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isBatchCommitting = false,
                    batchCommitDialogVisible = false,
                    statusMessage = pushResult.message,
                    latestPushResult = pushResult,
                    pushStatusDialogVisible = true,
                    isTrackingBuild = true
                )
                pollWorkflowStatusForPush(pushResult)
            } else {
                _uiState.value = _uiState.value.copy(
                    isBatchCommitting = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Batch commit & push failed"
                )
            }
        }
    }
}
