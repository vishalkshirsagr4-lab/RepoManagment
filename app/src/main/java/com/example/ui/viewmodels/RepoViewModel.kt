package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.models.GitHubBranch
import com.example.data.remote.models.GitHubCheckRun
import com.example.data.remote.models.GitHubCommit
import com.example.data.remote.models.GitHubContentItem
import com.example.data.remote.models.GitHubRepo
import com.example.data.remote.models.GitHubWorkflow
import com.example.data.remote.models.GitHubWorkflowRun
import com.example.data.remote.models.PushResult
import com.example.data.remote.models.RepoSecretItem
import com.example.data.remote.models.StagedChange
import com.example.data.remote.models.WorkflowJob
import com.example.data.repository.BackupRepository
import com.example.data.repository.RepoRepository
import com.example.data.repository.StagedChangesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RepositoriesUiState(
    val isLoading: Boolean = false,
    val repos: List<GitHubRepo> = emptyList(),
    val filteredRepos: List<GitHubRepo> = emptyList(),
    val searchQuery: String = "",
    val selectedLanguage: String? = null,
    val selectedRepo: GitHubRepo? = null,
    val activeBranch: String = "main",
    val branches: List<GitHubBranch> = emptyList(),
    val commits: List<GitHubCommit> = emptyList(),
    val currentPath: String = "",
    val pathSegments: List<String> = emptyList(),
    val directoryContents: List<GitHubContentItem> = emptyList(),
    val isContentsLoading: Boolean = false,
    val isCreatingRepo: Boolean = false,
    val isCreatingFile: Boolean = false,
    val isDeletingFile: Boolean = false,
    val isUploading: Boolean = false,
    val stagedChanges: List<StagedChange> = emptyList(),
    val isBatchCommitting: Boolean = false,
    val batchCommitDialogVisible: Boolean = false,
    val latestPushResult: PushResult? = null,
    val pushStatusDialogVisible: Boolean = false,
    val isTrackingBuild: Boolean = false,
    val latestWorkflowRun: GitHubWorkflowRun? = null,
    val latestWorkflowJobs: List<WorkflowJob> = emptyList(),
    val secrets: List<RepoSecretItem> = emptyList(),
    val isSecretsLoading: Boolean = false,
    val secretsError: String? = null,
    val isAddingSecret: Boolean = false,
    val addSecretError: String? = null,
    val workflows: List<GitHubWorkflow> = emptyList(),
    val isWorkflowsLoading: Boolean = false,
    val checkRuns: List<GitHubCheckRun> = emptyList(),
    val isCheckRunsLoading: Boolean = false,
    val isDispatchingWorkflow: Boolean = false,
    val uploadError: String? = null,
    val createRepoError: String? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class RepoViewModel(application: Application) : AndroidViewModel(application) {
    private val repoRepository = RepoRepository(application)
    private val backupRepository = BackupRepository(application, repoRepository)

    private val _uiState = MutableStateFlow(RepositoriesUiState())
    val uiState: StateFlow<RepositoriesUiState> = _uiState.asStateFlow()

    init {
        loadRepositories()
        viewModelScope.launch {
            StagedChangesManager.stagedMap.collect { map ->
                val repo = _uiState.value.selectedRepo
                val staged = if (repo != null) map[repo.fullName] ?: emptyList() else emptyList()
                _uiState.value = _uiState.value.copy(stagedChanges = staged)
            }
        }
    }

    fun loadRepositories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repoRepository.getRepositories()
            if (result.isSuccess) {
                val repos = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    repos = repos,
                    filteredRepos = filterRepos(repos, _uiState.value.searchQuery, _uiState.value.selectedLanguage)
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load repositories"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredRepos = filterRepos(_uiState.value.repos, query, _uiState.value.selectedLanguage)
        )
    }

    fun onLanguageFilterChange(language: String?) {
        val newLang = if (_uiState.value.selectedLanguage == language) null else language
        _uiState.value = _uiState.value.copy(
            selectedLanguage = newLang,
            filteredRepos = filterRepos(_uiState.value.repos, _uiState.value.searchQuery, newLang)
        )
    }

    private fun filterRepos(
        list: List<GitHubRepo>,
        query: String,
        language: String?
    ): List<GitHubRepo> {
        return list.filter { repo ->
            val matchesQuery = query.isBlank() ||
                repo.name.contains(query, ignoreCase = true) ||
                repo.fullName.contains(query, ignoreCase = true) ||
                repo.description?.contains(query, ignoreCase = true) == true ||
                repo.language?.contains(query, ignoreCase = true) == true
            val matchesLang = language == null || repo.language.equals(language, ignoreCase = true)
            matchesQuery && matchesLang
        }
    }

    fun loadCustomRepo(ownerRepoString: String) {
        val clean = ownerRepoString.trim().removePrefix("https://github.com/").trim('/')
        val parts = clean.split("/")
        if (parts.size < 2) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter in 'owner/repo' format (e.g. octocat/Hello-World)")
            return
        }
        val owner = parts[0]
        val repo = parts[1]

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repoRepository.getRepoDetails(owner, repo)
            if (result.isSuccess) {
                val found = result.getOrThrow()
                val updated = listOf(found) + _uiState.value.repos.filter { it.fullName != found.fullName }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    repos = updated,
                    filteredRepos = updated
                )
                selectRepo(found)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Repository not found."
                )
            }
        }
    }

    fun selectRepo(repo: GitHubRepo) {
        val parts = repo.fullName.split("/")
        val owner = parts.getOrNull(0) ?: repo.owner.login
        val name = parts.getOrNull(1) ?: repo.name
        val branch = repo.defaultBranch.ifBlank { "main" }

        _uiState.value = _uiState.value.copy(
            selectedRepo = repo,
            activeBranch = branch,
            currentPath = "",
            pathSegments = emptyList(),
            directoryContents = emptyList(),
            stagedChanges = StagedChangesManager.getStagedChanges(repo.fullName)
        )

        loadBranchesAndCommits(owner, name, branch)
        loadDirectoryContents(owner, name, "", branch)
        loadRepoSecrets(owner, name)
        loadWorkflowsAndChecks(owner, name, branch)
    }

    fun clearSelectedRepo() {
        _uiState.value = _uiState.value.copy(
            selectedRepo = null,
            currentPath = "",
            pathSegments = emptyList(),
            directoryContents = emptyList()
        )
    }

    fun selectBranch(branch: String) {
        val repo = _uiState.value.selectedRepo ?: return
        val parts = repo.fullName.split("/")
        val owner = parts.getOrNull(0) ?: repo.owner.login
        val name = parts.getOrNull(1) ?: repo.name

        _uiState.value = _uiState.value.copy(activeBranch = branch)
        loadBranchesAndCommits(owner, name, branch)
        loadDirectoryContents(owner, name, _uiState.value.currentPath, branch)
    }

    private fun loadBranchesAndCommits(owner: String, repo: String, branch: String) {
        viewModelScope.launch {
            val branchesResult = repoRepository.getBranches(owner, repo)
            if (branchesResult.isSuccess) {
                _uiState.value = _uiState.value.copy(branches = branchesResult.getOrThrow())
            }
            val commitsResult = repoRepository.getCommits(owner, repo, branch)
            if (commitsResult.isSuccess) {
                _uiState.value = _uiState.value.copy(commits = commitsResult.getOrThrow())
            }
        }
    }

    fun navigateToDirectory(folderPath: String) {
        val repo = _uiState.value.selectedRepo ?: return
        val parts = repo.fullName.split("/")
        val owner = parts.getOrNull(0) ?: repo.owner.login
        val name = parts.getOrNull(1) ?: repo.name
        val segments = if (folderPath.isBlank()) emptyList() else folderPath.split("/").filter { it.isNotBlank() }

        _uiState.value = _uiState.value.copy(
            currentPath = folderPath,
            pathSegments = segments
        )
        loadDirectoryContents(owner, name, folderPath, _uiState.value.activeBranch)
    }

    fun navigateUp() {
        val segments = _uiState.value.pathSegments
        if (segments.isEmpty()) return
        val parentPath = segments.dropLast(1).joinToString("/")
        navigateToDirectory(parentPath)
    }

    fun loadDirectoryContents(owner: String, repo: String, path: String, branch: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isContentsLoading = true)
            val result = repoRepository.getContents(owner, repo, path, branch)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isContentsLoading = false,
                    directoryContents = result.getOrThrow()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isContentsLoading = false,
                    statusMessage = "Unable to fetch directory contents."
                )
            }
        }
    }

    fun uploadBinaryFile(
        filePath: String,
        fileBytes: ByteArray,
        commitMessage: String,
        onSuccess: () -> Unit
    ) {
        val repo = _uiState.value.selectedRepo ?: return
        val parts = repo.fullName.split("/")
        val owner = parts.getOrNull(0) ?: repo.owner.login
        val name = parts.getOrNull(1) ?: repo.name

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, uploadError = null)
            val fullPath = if (_uiState.value.currentPath.isBlank()) filePath
            else "${_uiState.value.currentPath.trim('/')}/$filePath"

            val result = repoRepository.uploadBinaryFile(
                owner = owner,
                repo = name,
                path = fullPath,
                fileBytes = fileBytes,
                commitMessage = commitMessage,
                branch = _uiState.value.activeBranch
            )

            if (result.isSuccess) {
                val uploadMsg = result.getOrThrow()
                val pushResult = PushResult(
                    success = true,
                    commitSha = "up-" + (1000..9999).random(),
                    branch = _uiState.value.activeBranch,
                    message = commitMessage.ifBlank { "Upload $fullPath" },
                    timestamp = "Just now",
                    repoFullName = repo.fullName,
                    filePath = fullPath
                )
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    statusMessage = uploadMsg,
                    latestPushResult = pushResult,
                    pushStatusDialogVisible = true,
                    isTrackingBuild = true
                )
                loadDirectoryContents(owner, name, _uiState.value.currentPath, _uiState.value.activeBranch)
                pollWorkflowStatusForPush(pushResult)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadError = result.exceptionOrNull()?.message ?: "Upload failed"
                )
            }
        }
    }

    fun createFileInRepo(
        fileName: String,
        content: String,
        commitMessage: String,
        onSuccess: () -> Unit
    ) {
        val repo = _uiState.value.selectedRepo ?: return
        val parts = repo.fullName.split("/")
        val owner = parts.getOrNull(0) ?: repo.owner.login
        val name = parts.getOrNull(1) ?: repo.name

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingFile = true, errorMessage = null)
            val fullPath = if (_uiState.value.currentPath.isBlank()) fileName
            else "${_uiState.value.currentPath.trim('/')}/$fileName"

            val result = repoRepository.createFile(
                owner = owner,
                repo = name,
                path = fullPath,
                content = content,
                commitMessage = commitMessage.ifBlank { "Create $fullPath via Repo Manager Pro" },
                branch = _uiState.value.activeBranch
            )

            if (result.isSuccess) {
                val pushResult = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isCreatingFile = false,
                    statusMessage = pushResult.message,
                    latestPushResult = pushResult,
                    pushStatusDialogVisible = true,
                    isTrackingBuild = true
                )
                loadDirectoryContents(owner, name, _uiState.value.currentPath, _uiState.value.activeBranch)
                pollWorkflowStatusForPush(pushResult)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isCreatingFile = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to create file"
                )
            }
        }
    }

    fun deleteFileInRepo(
        item: GitHubContentItem,
        commitMessage: String,
        onSuccess: () -> Unit
    ) {
        val repo = _uiState.value.selectedRepo ?: return
        val parts = repo.fullName.split("/")
        val owner = parts.getOrNull(0) ?: repo.owner.login
        val name = parts.getOrNull(1) ?: repo.name

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeletingFile = true, errorMessage = null)
            val result = repoRepository.deleteFile(
                owner = owner,
                repo = name,
                path = item.path,
                sha = item.sha,
                commitMessage = commitMessage.ifBlank { "Delete ${item.name} via Repo Manager Pro" },
                branch = _uiState.value.activeBranch
            )

            if (result.isSuccess) {
                val pushResult = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isDeletingFile = false,
                    statusMessage = pushResult.message,
                    latestPushResult = pushResult,
                    pushStatusDialogVisible = true,
                    isTrackingBuild = true
                )
                loadDirectoryContents(owner, name, _uiState.value.currentPath, _uiState.value.activeBranch)
                pollWorkflowStatusForPush(pushResult)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isDeletingFile = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete file"
                )
            }
        }
    }

    fun renameItemInRepo(
        oldItem: GitHubContentItem,
        newName: String,
        commitMessage: String,
        onSuccess: () -> Unit
    ) {
        val repo = _uiState.value.selectedRepo ?: return
        val parts = repo.fullName.split("/")
        val owner = parts.getOrNull(0) ?: repo.owner.login
        val name = parts.getOrNull(1) ?: repo.name

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isContentsLoading = true, errorMessage = null)
            val parentPath = if (oldItem.path.contains("/")) oldItem.path.substringBeforeLast('/') else ""
            val newPath = if (parentPath.isBlank()) newName else "$parentPath/$newName"

            val result = if (oldItem.type == "dir") {
                repoRepository.renameFolder(
                    owner = owner,
                    repo = name,
                    oldFolderPath = oldItem.path,
                    newFolderPath = newPath,
                    commitMessage = commitMessage.ifBlank { "Rename folder ${oldItem.name} to $newName" },
                    branch = _uiState.value.activeBranch
                )
            } else {
                repoRepository.renameFile(
                    owner = owner,
                    repo = name,
                    oldPath = oldItem.path,
                    newPath = newPath,
                    commitMessage = commitMessage.ifBlank { "Rename ${oldItem.name} to $newName" },
                    branch = _uiState.value.activeBranch
                )
            }

            if (result.isSuccess) {
                val pushResult = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isContentsLoading = false,
                    statusMessage = pushResult.message,
                    latestPushResult = pushResult,
                    pushStatusDialogVisible = true,
                    isTrackingBuild = true
                )
                loadDirectoryContents(owner, name, _uiState.value.currentPath, _uiState.value.activeBranch)
                pollWorkflowStatusForPush(pushResult)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isContentsLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Rename failed"
                )
            }
        }
    }

    fun stageRenameInRepo(
        oldItem: GitHubContentItem,
        newName: String,
        onSuccess: () -> Unit
    ) {
        val repo = _uiState.value.selectedRepo ?: return
        val parentPath = if (oldItem.path.contains("/")) oldItem.path.substringBeforeLast('/') else ""
        val newPath = if (parentPath.isBlank()) newName else "$parentPath/$newName"

        StagedChangesManager.stageRename(
            repoFullName = repo.fullName,
            oldPath = oldItem.path,
            newPath = newPath,
            branch = _uiState.value.activeBranch
        )
        _uiState.value = _uiState.value.copy(
            statusMessage = "Staged rename: ${oldItem.name} -> $newName for batch commit"
        )
        onSuccess()
    }

    fun stageNewFileInRepo(
        fileName: String,
        content: String,
        onSuccess: () -> Unit
    ) {
        val repo = _uiState.value.selectedRepo ?: return
        val fullPath = if (_uiState.value.currentPath.isBlank()) fileName
        else "${_uiState.value.currentPath.trim('/')}/$fileName"

        StagedChangesManager.stageFile(
            repoFullName = repo.fullName,
            filePath = fullPath,
            branch = _uiState.value.activeBranch,
            content = content,
            isNewFile = true
        )
        _uiState.value = _uiState.value.copy(
            statusMessage = "Staged new file: $fileName for batch commit"
        )
        onSuccess()
    }

    fun stageBinaryUploadInRepo(
        filePath: String,
        fileBytes: ByteArray,
        onSuccess: () -> Unit
    ) {
        val repo = _uiState.value.selectedRepo ?: return
        val fullPath = if (_uiState.value.currentPath.isBlank()) filePath
        else "${_uiState.value.currentPath.trim('/')}/$filePath"

        StagedChangesManager.stageBinaryFile(
            repoFullName = repo.fullName,
            filePath = fullPath,
            branch = _uiState.value.activeBranch,
            fileBytes = fileBytes
        )
        _uiState.value = _uiState.value.copy(
            statusMessage = "Staged upload: $filePath for batch commit"
        )
        onSuccess()
    }

    fun stageFileDeletionInRepo(
        item: GitHubContentItem,
        onSuccess: () -> Unit
    ) {
        val repo = _uiState.value.selectedRepo ?: return
        StagedChangesManager.stageDeletion(
            repoFullName = repo.fullName,
            filePath = item.path,
            branch = _uiState.value.activeBranch,
            sha = item.sha
        )
        _uiState.value = _uiState.value.copy(
            statusMessage = "Staged deletion: ${item.name} for batch commit"
        )
        onSuccess()
    }

    fun unstageFileInRepo(filePath: String) {
        val repo = _uiState.value.selectedRepo ?: return
        StagedChangesManager.unstageFile(repo.fullName, filePath)
    }

    fun clearAllStagedInRepo() {
        val repo = _uiState.value.selectedRepo ?: return
        StagedChangesManager.clearStagedChanges(repo.fullName)
    }

    fun showBatchCommitDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(batchCommitDialogVisible = show)
    }

    fun executeBatchCommitAndPush(
        commitTitle: String,
        commitDescription: String?,
        branch: String
    ) {
        val repo = _uiState.value.selectedRepo ?: return
        val parts = repo.fullName.split("/")
        val owner = parts.getOrNull(0) ?: repo.owner.login
        val name = parts.getOrNull(1) ?: repo.name
        val staged = StagedChangesManager.getStagedChanges(repo.fullName)

        if (staged.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No staged changes to commit.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBatchCommitting = true, errorMessage = null)
            val result = repoRepository.batchCommitAndPush(
                owner = owner,
                repo = name,
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
                loadDirectoryContents(owner, name, _uiState.value.currentPath, branch)
                pollWorkflowStatusForPush(pushResult)
            } else {
                _uiState.value = _uiState.value.copy(
                    isBatchCommitting = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Batch commit & push failed"
                )
            }
        }
    }

    fun pollWorkflowStatusForPush(pushResult: PushResult) {
        val parts = pushResult.repoFullName.split("/")
        val owner = parts.getOrNull(0) ?: "octodev"
        val repo = parts.getOrNull(1) ?: "repomanager-core"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTrackingBuild = true)
            val runResult = repoRepository.getLatestWorkflowRunForCommit(owner, repo, pushResult.commitSha)
            val run = runResult.getOrNull()
            var jobs: List<WorkflowJob> = emptyList()
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

    fun createRepository(
        name: String,
        description: String,
        isPrivate: Boolean,
        autoInit: Boolean,
        onSuccess: (GitHubRepo) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingRepo = true, createRepoError = null)
            val result = repoRepository.createRepository(name, description, isPrivate, autoInit)
            if (result.isSuccess) {
                val created = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isCreatingRepo = false,
                    statusMessage = "Repository created: ${created.name}"
                )
                loadRepositories()
                onSuccess(created)
            } else {
                _uiState.value = _uiState.value.copy(
                    isCreatingRepo = false,
                    createRepoError = result.exceptionOrNull()?.message ?: "Failed to create repository"
                )
            }
        }
    }

    fun downloadZipBackup(repo: GitHubRepo, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(statusMessage = "Starting ZIP backup for ${repo.name}...")
            val result = backupRepository.createRepositoryBackup(
                repoFullName = repo.fullName,
                branch = _uiState.value.activeBranch
            )
            if (result.isSuccess) {
                val record = result.getOrThrow()
                _uiState.value = _uiState.value.copy(statusMessage = "Saved backup: ${record.fileName}")
                onDone(true, record.fileName)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Backup failed"
                _uiState.value = _uiState.value.copy(statusMessage = "Backup error: $err")
                onDone(false, err)
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null, errorMessage = null)
    }
// Repository Secrets
fun loadRepoSecrets(owner: String, repo: String) {
    viewModelScope.launch {

        _uiState.value = _uiState.value.copy(
            isSecretsLoading = true,
            secretsError = null,
            secrets = emptyList()
        )

        val result = repoRepository.getRepoSecrets(owner, repo)

        if (result.isSuccess) {

            _uiState.value = _uiState.value.copy(
                isSecretsLoading = false,
                secrets = result.getOrThrow(),
                secretsError = null
            )

        } else {

            _uiState.value = _uiState.value.copy(
                isSecretsLoading = false,
                secrets = emptyList(),
                secretsError = result.exceptionOrNull()?.message
                    ?: "Unable to fetch GitHub secrets."
            )
        }
    }
}

    fun addSecret(
        owner: String,
        repo: String,
        name: String,
        value: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingSecret = true, addSecretError = null)
            val result = repoRepository.addOrUpdateSecret(owner, repo, name, value)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isAddingSecret = false,
                    statusMessage = "Secret '$name' saved successfully"
                )
                loadRepoSecrets(owner, repo)
                onSuccess()
            } else {
                _uiState.value = _uiState.value.copy(
                    isAddingSecret = false,
                    addSecretError = result.exceptionOrNull()?.message ?: "Failed to save secret"
                )
            }
        }
    }

    fun addBulkSecrets(
        owner: String,
        repo: String,
        rawContent: String,
        onSuccess: (Int) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingSecret = true, addSecretError = null)
            val result = repoRepository.addBulkSecrets(owner, repo, rawContent)
            if (result.isSuccess) {
                val count = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    isAddingSecret = false,
                    statusMessage = "Successfully imported $count repository secrets"
                )
                loadRepoSecrets(owner, repo)
                onSuccess(count)
            } else {
                _uiState.value = _uiState.value.copy(
                    isAddingSecret = false,
                    addSecretError = result.exceptionOrNull()?.message ?: "Failed to import bulk secrets"
                )
            }
        }
    }

    fun deleteSecret(owner: String, repo: String, secretName: String) {
        viewModelScope.launch {
            val result = repoRepository.deleteSecret(owner, repo, secretName)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Secret '$secretName' deleted"
                )
                loadRepoSecrets(owner, repo)
            } else {
                _uiState.value = _uiState.value.copy(
                    statusMessage = result.exceptionOrNull()?.message
                        ?: "Could not delete '$secretName'"
                )
            }
        }
    }

    // Workflows and Check Runs
    fun loadWorkflowsAndChecks(owner: String, repo: String, ref: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWorkflowsLoading = true, isCheckRunsLoading = true)
            val wResult = repoRepository.getWorkflows(owner, repo)
            val cResult = repoRepository.getCheckRuns(owner, repo, ref)

            _uiState.value = _uiState.value.copy(
                isWorkflowsLoading = false,
                isCheckRunsLoading = false,
                workflows = wResult.getOrDefault(emptyList()),
                checkRuns = cResult.getOrDefault(emptyList())
            )
        }
    }

    fun triggerWorkflow(owner: String, repo: String, workflowId: Long, branch: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDispatchingWorkflow = true)
            val result = repoRepository.triggerWorkflowDispatch(owner, repo, workflowId, branch)
            _uiState.value = _uiState.value.copy(
                isDispatchingWorkflow = false,
                statusMessage = result.getOrDefault("Workflow dispatched")
            )
            loadBranchesAndCommits(owner, repo, branch)
        }
    }

    fun toggleWorkflowEnabled(owner: String, repo: String, workflow: GitHubWorkflow) {
        val willEnable = workflow.state != "active"
        viewModelScope.launch {
            val result = repoRepository.setWorkflowState(owner, repo, workflow.id, willEnable)
            if (result.isSuccess) {
                val updated = _uiState.value.workflows.map {
                    if (it.id == workflow.id) it.copy(state = if (willEnable) "active" else "disabled_manually") else it
                }
                _uiState.value = _uiState.value.copy(
                    workflows = updated,
                    statusMessage = if (willEnable) "Workflow '${workflow.name}' enabled" else "Workflow '${workflow.name}' disabled"
                )
            }
        }
    }

    fun cancelWorkflowRun(owner: String, repo: String, runId: Long) {
        viewModelScope.launch {
            val result = repoRepository.cancelWorkflowRun(owner, repo, runId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(statusMessage = "Workflow run cancelled")
                val branch = _uiState.value.activeBranch
                loadBranchesAndCommits(owner, repo, branch)
            }
        }
    }
}

