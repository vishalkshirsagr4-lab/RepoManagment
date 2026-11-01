package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.models.DeploymentProvider
import com.example.data.remote.models.GitHubWorkflowRun
import com.example.data.remote.models.UnifiedDeployment
import com.example.data.remote.models.WorkflowJob
import com.example.data.repository.DeploymentRepository
import com.example.data.repository.RepoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DeploymentsUiState(
    val isLoading: Boolean = false,
    val selectedFilter: DeploymentProvider? = null,
    val deployments: List<UnifiedDeployment> = emptyList(),
    val filteredDeployments: List<UnifiedDeployment> = emptyList(),
    val activeRepo: String = "octodev/repomanager-core",
    val liveWorkflowRuns: List<GitHubWorkflowRun> = emptyList(),
    val selectedRunJobs: List<WorkflowJob> = emptyList(),
    val selectedRunId: Long? = null,
    val isAutoRefresh: Boolean = false,
    val isCloudflareConfigured: Boolean = false,
    val isHuggingFaceConfigured: Boolean = false,
    val isGitHubConfigured: Boolean = false,
    val statusMessage: String? = null
)

class DeploymentsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DeploymentRepository(application)
    private val repoRepository = RepoRepository(application)

    private val _uiState = MutableStateFlow(DeploymentsUiState())
    val uiState: StateFlow<DeploymentsUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        loadDeployments()
        fetchLiveWorkflowRuns()
    }

    fun loadDeployments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCloudflareConfigured = repository.isCloudflareConnected(),
                isHuggingFaceConfigured = repository.isHuggingFaceConnected(),
                isGitHubConfigured = repository.isGitHubConnected()
            )
            repository.deployments.collect { list ->
                _uiState.value = _uiState.value.copy(
                    deployments = list,
                    filteredDeployments = applyFilter(list, _uiState.value.selectedFilter)
                )
            }
        }
    }

    fun setFilter(provider: DeploymentProvider?) {
        val newFilter = if (_uiState.value.selectedFilter == provider) null else provider
        _uiState.value = _uiState.value.copy(
            selectedFilter = newFilter,
            filteredDeployments = applyFilter(_uiState.value.deployments, newFilter)
        )
    }

    private fun applyFilter(
        list: List<UnifiedDeployment>,
        filter: DeploymentProvider?
    ): List<UnifiedDeployment> {
        return if (filter == null) list else list.filter { it.provider == filter }
    }

    fun setTargetRepo(repoFullName: String) {
        _uiState.value = _uiState.value.copy(activeRepo = repoFullName)
        fetchLiveWorkflowRuns()
    }

    fun fetchLiveWorkflowRuns() {
        val repo = _uiState.value.activeRepo
        val parts = repo.split("/")
        val owner = parts.getOrNull(0) ?: "octodev"
        val name = parts.getOrNull(1) ?: "repomanager-core"

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repoRepository.getWorkflowRuns(owner, name)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    liveWorkflowRuns = result.getOrThrow()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    fun toggleAutoRefresh() {
        val newAuto = !_uiState.value.isAutoRefresh
        _uiState.value = _uiState.value.copy(isAutoRefresh = newAuto)
        pollingJob?.cancel()
        if (newAuto) {
            pollingJob = viewModelScope.launch {
                while (isActive) {
                    fetchLiveWorkflowRuns()
                    val selectedId = _uiState.value.selectedRunId
                    if (selectedId != null) {
                        loadJobsForRun(selectedId)
                    }
                    delay(5000)
                }
            }
        }
    }

    fun selectWorkflowRun(run: GitHubWorkflowRun) {
        _uiState.value = _uiState.value.copy(selectedRunId = run.id)
        loadJobsForRun(run.id)
    }

    fun clearSelectedRun() {
        _uiState.value = _uiState.value.copy(selectedRunId = null, selectedRunJobs = emptyList())
    }

    fun loadJobsForRun(runId: Long) {
        val repo = _uiState.value.activeRepo
        val parts = repo.split("/")
        val owner = parts.getOrNull(0) ?: "octodev"
        val name = parts.getOrNull(1) ?: "repomanager-core"

        viewModelScope.launch {
            val result = repoRepository.getWorkflowJobs(owner, name, runId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(selectedRunJobs = result.getOrThrow())
            }
        }
    }

    fun rerunWorkflow(runId: Long) {
        val repo = _uiState.value.activeRepo
        val parts = repo.split("/")
        val owner = parts.getOrNull(0) ?: "octodev"
        val name = parts.getOrNull(1) ?: "repomanager-core"

        viewModelScope.launch {
            val result = repoRepository.rerunWorkflow(owner, name, runId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(statusMessage = "Workflow re-run requested.")
                fetchLiveWorkflowRuns()
            } else {
                _uiState.value = _uiState.value.copy(statusMessage = "Re-run failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun triggerRedeploy(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.triggerDeployment(id)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusMessage = if (result.isSuccess) "Deployment queued for ${result.getOrThrow().projectName}" else "Failed to trigger deployment"
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
