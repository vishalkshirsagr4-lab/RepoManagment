package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.data.remote.models.GitHubCheckRun
import com.example.data.remote.models.GitHubWorkflow
import com.example.data.remote.models.RepoSecretItem
import com.example.data.remote.models.StagedChange
import com.example.ui.common.FileUtils
import com.example.ui.components.BatchCommitAndPushDialog
import com.example.ui.components.PushStatusDialog
import com.example.ui.components.RenameItemDialog
import com.example.ui.components.UniversalFilePreviewDialog
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.remote.models.GitHubBranch
import com.example.data.remote.models.GitHubCommit
import com.example.data.remote.models.GitHubContentItem
import com.example.data.remote.models.GitHubRepo
import com.example.ui.components.LanguageBadge
import com.example.ui.components.getFileIcon
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodels.RepoViewModel

@Composable
fun RepositoriesScreen(
    viewModel: RepoViewModel,
    onOpenFileInEditor: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showCustomRepoDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().testTag("repositories_screen")) {
        if (uiState.selectedRepo != null) {
            // Selected Repo Detail View
            RepoDetailContent(
                repo = uiState.selectedRepo!!,
                activeBranch = uiState.activeBranch,
                branches = uiState.branches,
                commits = uiState.commits,
                currentPath = uiState.currentPath,
                pathSegments = uiState.pathSegments,
                directoryContents = uiState.directoryContents,
                isContentsLoading = uiState.isContentsLoading,
                isUploading = uiState.isUploading,
                uploadError = uiState.uploadError,
                statusMessage = uiState.statusMessage,
                onBack = { viewModel.clearSelectedRepo() },
                onSelectBranch = { viewModel.selectBranch(it) },
                onNavigateToDirectory = { viewModel.navigateToDirectory(it) },
                onNavigateUp = { viewModel.navigateUp() },
                onOpenFile = { path ->
                    onOpenFileInEditor(uiState.selectedRepo!!.fullName, path, uiState.activeBranch)
                },
                onUploadBinary = { fileName, bytes, message ->
                    viewModel.uploadBinaryFile(fileName, bytes, message) {}
                },
                onCreateFile = { fileName, content, message ->
                    viewModel.createFileInRepo(fileName, content, message) {}
                },
                onDeleteFile = { item, message ->
                    viewModel.deleteFileInRepo(item, message) {}
                },
                onRenameItem = { item, newName, message ->
                    viewModel.renameItemInRepo(item, newName, message) {}
                },
                onStageRename = { item, newName ->
                    viewModel.stageRenameInRepo(item, newName) {}
                },
                onDownloadZip = {
                    viewModel.downloadZipBackup(uiState.selectedRepo!!) { _, _ -> }
                },
                secrets = uiState.secrets,
                isSecretsLoading = uiState.isSecretsLoading,
                workflows = uiState.workflows,
                isWorkflowsLoading = uiState.isWorkflowsLoading,
                checkRuns = uiState.checkRuns,
                isCheckRunsLoading = uiState.isCheckRunsLoading,
                onAddSecret = { name, value, onDone ->
                    val parts = uiState.selectedRepo!!.fullName.split("/")
                    viewModel.addSecret(parts[0], parts[1], name, value, onDone)
                },
                onAddBulkSecrets = { raw, onDone ->
                    val parts = uiState.selectedRepo!!.fullName.split("/")
                    viewModel.addBulkSecrets(parts[0], parts[1], raw, onDone)
                },
                onDeleteSecret = { name ->
                    val parts = uiState.selectedRepo!!.fullName.split("/")
                    viewModel.deleteSecret(parts[0], parts[1], name)
                },
                onTriggerWorkflow = { wfId, branch ->
                    val parts = uiState.selectedRepo!!.fullName.split("/")
                    viewModel.triggerWorkflow(parts[0], parts[1], wfId, branch)
                },
                onToggleWorkflowEnabled = { wf ->
                    val parts = uiState.selectedRepo!!.fullName.split("/")
                    viewModel.toggleWorkflowEnabled(parts[0], parts[1], wf)
                },
                onOpenFileInEditor = onOpenFileInEditor,
                stagedChanges = uiState.stagedChanges,
                onStageNewFile = { fileName, content ->
                    viewModel.stageNewFileInRepo(fileName, content) {}
                },
                onStageBinaryUpload = { fileName, bytes ->
                    viewModel.stageBinaryUploadInRepo(fileName, bytes) {}
                },
                onStageFileDeletion = { item ->
                    viewModel.stageFileDeletionInRepo(item) {}
                },
                onOpenBatchCommitDialog = {
                    viewModel.showBatchCommitDialog(true)
                },
                onClearAllStaged = {
                    viewModel.clearAllStagedInRepo()
                }
            )
        } else {
            // All Repositories List View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Row: Search and Load Public Repo button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Search repositories...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("repo_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { showCustomRepoDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkElevatedSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Load", color = SecondaryCyan, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Language Filters
                val languages = listOf("All", "Kotlin", "Python", "TypeScript", "JavaScript", "HCL")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(languages) { lang ->
                        val isSelected = if (lang == "All") uiState.selectedLanguage == null
                        else uiState.selectedLanguage.equals(lang, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryPurple else DarkSurface)
                                .border(1.dp, if (isSelected) PrimaryPurple else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.onLanguageFilterChange(if (lang == "All") null else lang)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = lang,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Error message
                if (!uiState.errorMessage.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusError.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        Text(text = uiState.errorMessage!!, color = StatusError, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Repos List
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else if (uiState.filteredRepos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (uiState.searchQuery.isNotBlank()) "No repositories matching '${uiState.searchQuery}'"
                                else "No repositories loaded. Connect your GitHub account or Load any public repo.",
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Button(
                                onClick = { showCustomRepoDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                            ) {
                                Text("Load Public Repository")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.filteredRepos) { repo ->
                            RepositoryCard(
                                repo = repo,
                                onClick = { viewModel.selectRepo(repo) }
                            )
                        }
                    }
                }
            }

            // FAB for New Repository
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PrimaryPurple,
                contentColor = TextPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("create_repo_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Repository")
            }
        }

        // Create Repository Modal Dialog
        if (showCreateDialog) {
            CreateRepoDialog(
                isCreating = uiState.isCreatingRepo,
                errorMessage = uiState.createRepoError,
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, desc, isPrivate, autoInit ->
                    viewModel.createRepository(name, desc, isPrivate, autoInit) {
                        showCreateDialog = false
                    }
                }
            )
        }

        // Load Custom Repository Modal Dialog
        if (showCustomRepoDialog) {
            LoadCustomRepoDialog(
                onDismiss = { showCustomRepoDialog = false },
                onConfirm = { query ->
                    viewModel.loadCustomRepo(query)
                    showCustomRepoDialog = false
                }
            )
        }

        // Push and GitHub Actions Build Logs Dialog
        if (uiState.pushStatusDialogVisible && uiState.latestPushResult != null) {
            PushStatusDialog(
                pushResult = uiState.latestPushResult!!,
                workflowRun = uiState.latestWorkflowRun,
                workflowJobs = uiState.latestWorkflowJobs,
                isRefreshingLogs = uiState.isTrackingBuild,
                onRefreshLogs = { viewModel.refreshWorkflowLogs() },
                onDismiss = { viewModel.dismissPushStatusDialog() }
            )
        }

        // Batch One-Time Commit and Push Dialog
        if (uiState.batchCommitDialogVisible && uiState.selectedRepo != null) {
            BatchCommitAndPushDialog(
                repoFullName = uiState.selectedRepo!!.fullName,
                initialBranch = uiState.activeBranch,
                availableBranches = uiState.branches.map { it.name }.ifEmpty { listOf(uiState.activeBranch) },
                stagedChanges = uiState.stagedChanges,
                isCommitting = uiState.isBatchCommitting,
                onUnstage = { viewModel.unstageFileInRepo(it) },
                onClearAll = { viewModel.clearAllStagedInRepo() },
                onDismiss = { viewModel.showBatchCommitDialog(false) },
                onCommitAndPush = { title, desc, branch ->
                    viewModel.executeBatchCommitAndPush(title, desc, branch)
                }
            )
        }
    }
}

@Composable
private fun RepositoryCard(
    repo: GitHubRepo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("repo_item_${repo.name}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkElevatedSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = repo.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = repo.fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkElevatedSurface)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (repo.private) Icons.Default.Lock else Icons.Default.Public,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (repo.private) "Private" else "Public",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            if (!repo.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageBadge(language = repo.language)

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Stars",
                            tint = StatusWarning,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = repo.stargazersCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ForkRight,
                            contentDescription = "Forks",
                            tint = TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = repo.forksCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoDetailContent(
    repo: GitHubRepo,
    activeBranch: String,
    branches: List<GitHubBranch>,
    commits: List<GitHubCommit>,
    currentPath: String,
    pathSegments: List<String>,
    directoryContents: List<GitHubContentItem>,
    isContentsLoading: Boolean,
    isUploading: Boolean,
    uploadError: String?,
    statusMessage: String?,
    onBack: () -> Unit,
    onSelectBranch: (String) -> Unit,
    onNavigateToDirectory: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onOpenFile: (String) -> Unit,
    onUploadBinary: (String, ByteArray, String) -> Unit,
    onCreateFile: (String, String, String) -> Unit,
    onDeleteFile: (GitHubContentItem, String) -> Unit,
    onDownloadZip: () -> Unit,
    secrets: List<RepoSecretItem>,
    isSecretsLoading: Boolean,
    workflows: List<GitHubWorkflow>,
    isWorkflowsLoading: Boolean,
    checkRuns: List<GitHubCheckRun>,
    isCheckRunsLoading: Boolean,
    onAddSecret: (String, String, () -> Unit) -> Unit,
    onAddBulkSecrets: (String, (Int) -> Unit) -> Unit,
    onDeleteSecret: (String) -> Unit,
    onTriggerWorkflow: (Long, String) -> Unit,
    onToggleWorkflowEnabled: (GitHubWorkflow) -> Unit,
    onOpenFileInEditor: (String, String, String) -> Unit,
    stagedChanges: List<StagedChange> = emptyList(),
    onStageNewFile: (String, String) -> Unit,
    onStageBinaryUpload: (String, ByteArray) -> Unit,
    onStageFileDeletion: (GitHubContentItem) -> Unit,
    onRenameItem: (GitHubContentItem, String, String) -> Unit,
    onStageRename: (GitHubContentItem, String) -> Unit,
    onOpenBatchCommitDialog: () -> Unit,
    onClearAllStaged: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var branchMenuOpen by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var deletingItem by remember { mutableStateOf<GitHubContentItem?>(null) }
    var renamingItem by remember { mutableStateOf<GitHubContentItem?>(null) }
    var previewFileItem by remember { mutableStateOf<GitHubContentItem?>(null) }
    var showAddSecretDialog by remember { mutableStateOf(false) }
    var showBulkSecretsDialog by remember { mutableStateOf(false) }
    var deletingSecret by remember { mutableStateOf<RepoSecretItem?>(null) }
    var runningWorkflow by remember { mutableStateOf<GitHubWorkflow?>(null) }
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Detail Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = repo.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = repo.fullName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick Batch Push Button if files staged
                        if (stagedChanges.isNotEmpty()) {
                            Button(
                                onClick = onOpenBatchCommitDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp).testTag("repo_header_batch_push_btn")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Push (${stagedChanges.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Upload File Action
                        IconButton(
                            onClick = { showUploadDialog = true },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkElevatedSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload File",
                                tint = PrimaryPurple
                            )
                        }

                        // Download ZIP Backup Button
                        IconButton(
                            onClick = onDownloadZip,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkElevatedSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "Download ZIP",
                                tint = SecondaryCyan
                            )
                        }
                    }
                }

                if (!repo.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = repo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Branch Selector Dropdown & Clone Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkElevatedSurface)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { branchMenuOpen = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeBranch,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = branchMenuOpen,
                            onDismissRequest = { branchMenuOpen = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            val branchOptions = if (branches.isEmpty()) listOf(activeBranch)
                            else branches.map { it.name }
                            branchOptions.forEach { bName ->
                                DropdownMenuItem(
                                    text = { Text(bName, color = TextPrimary) },
                                    trailingIcon = {
                                        if (bName == activeBranch) {
                                            Icon(Icons.Default.Check, null, tint = PrimaryPurple)
                                        }
                                    },
                                    onClick = {
                                        onSelectBranch(bName)
                                        branchMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkElevatedSurface)
                            .clickable {
                                clipboardManager.setText(AnnotatedString("https://github.com/${repo.fullName}.git"))
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy URL",
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clone",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Staged Changes Banner
        if (stagedChanges.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryPurple.copy(alpha = 0.15f))
                    .border(androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.35f)))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stagedChanges.size} ${if (stagedChanges.size == 1) "change" else "changes"} staged for one-time push",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onOpenBatchCommitDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp).testTag("repo_banner_review_btn")
                    ) {
                        Text("Review & Push", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clear",
                        fontSize = 11.sp,
                        color = StatusError,
                        modifier = Modifier
                            .clickable { onClearAllStaged() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (!statusMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryPurple.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryPurple
                )
            }
        }

        // Tabs: Files, Commits, Branches
        // Tabs: Files, Commits, Branches, Actions, Secrets
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkBg,
            contentColor = PrimaryPurple,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimaryPurple
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Files", color = if (selectedTab == 0) PrimaryPurple else TextSecondary) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Commits (${commits.size})", color = if (selectedTab == 1) PrimaryPurple else TextSecondary) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Branches", color = if (selectedTab == 2) PrimaryPurple else TextSecondary) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Actions (${workflows.size})", color = if (selectedTab == 3) PrimaryPurple else TextSecondary) }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("Secrets (${secrets.size})", color = if (selectedTab == 4) PrimaryPurple else TextSecondary) }
            )
        }

        // Tab Content
        when (selectedTab) {
            0 -> FileExplorerTab(
                currentPath = currentPath,
                pathSegments = pathSegments,
                items = directoryContents,
                isLoading = isContentsLoading,
                onNavigateToDirectory = onNavigateToDirectory,
                onNavigateUp = onNavigateUp,
                onOpenFile = onOpenFile,
                onPreviewFile = { item -> previewFileItem = item },
                onUploadClick = { showUploadDialog = true },
                onNewFileClick = { showCreateFileDialog = true },
                onDeleteFileClick = { item -> deletingItem = item },
                onRenameItemClick = { item -> renamingItem = item }
            )
            1 -> CommitsTab(commits = commits)
            2 -> BranchesTab(branches = branches, activeBranch = activeBranch, onSelectBranch = onSelectBranch)
            3 -> ActionsWorkflowsTab(
                workflows = workflows,
                checkRuns = checkRuns,
                activeBranch = activeBranch,
                isWorkflowsLoading = isWorkflowsLoading,
                isCheckRunsLoading = isCheckRunsLoading,
                onRunWorkflow = { wf -> runningWorkflow = wf },
                onToggleWorkflow = { wf -> onToggleWorkflowEnabled(wf) },
                onOpenFile = { path -> onOpenFile(path) }
            )
            4 -> RepoSecretsTab(
                secrets = secrets,
                isLoading = isSecretsLoading,
                onAddSecretClick = { showAddSecretDialog = true },
                onBulkSecretsClick = { showBulkSecretsDialog = true },
                onDeleteSecretClick = { sec -> deletingSecret = sec }
            )
        }
    }

    // Binary Upload Dialog
    if (showUploadDialog) {
        UploadFileDialog(
            isUploading = isUploading,
            uploadError = uploadError,
            currentPath = currentPath,
            onDismiss = { showUploadDialog = false },
            onStage = { fileName, bytes ->
                onStageBinaryUpload(fileName, bytes)
                showUploadDialog = false
            },
            onConfirm = { fileName, bytes, message ->
                onUploadBinary(fileName, bytes, message)
                showUploadDialog = false
            }
        )
    }

    // Create File Dialog
    if (showCreateFileDialog) {
        CreateFileDialog(
            currentPath = currentPath,
            onDismiss = { showCreateFileDialog = false },
            onStage = { fileName, content ->
                onStageNewFile(fileName, content)
                showCreateFileDialog = false
            },
            onConfirm = { fileName, content, message ->
                onCreateFile(fileName, content, message)
                showCreateFileDialog = false
            }
        )
    }

    // Delete File Confirmation Dialog
    if (deletingItem != null) {
        DeleteFileDialog(
            item = deletingItem!!,
            activeBranch = activeBranch,
            onDismiss = { deletingItem = null },
            onStageDelete = { item ->
                onStageFileDeletion(item)
                deletingItem = null
            },
            onConfirm = { item, message ->
                onDeleteFile(item, message)
                deletingItem = null
            }
        )
    }

    // Universal File Preview Dialog (Original content: Images, Word Docs, PDF, Excel, Media, Code)
    if (previewFileItem != null) {
        UniversalFilePreviewDialog(
            item = previewFileItem!!,
            repoFullName = repo.fullName,
            branch = activeBranch,
            onDismiss = { previewFileItem = null },
            onOpenFileInEditor = {
                val p = previewFileItem!!.path
                previewFileItem = null
                onOpenFile(p)
            },
            onRename = {
                val itemToRename = previewFileItem
                previewFileItem = null
                renamingItem = itemToRename
            },
            onDelete = {
                val itemToDelete = previewFileItem
                previewFileItem = null
                deletingItem = itemToDelete
            }
        )
    }

    // Rename Item Dialog (files, folders, images, raw assets)
    if (renamingItem != null) {
        RenameItemDialog(
            currentName = renamingItem!!.name,
            currentPath = renamingItem!!.path,
            isFolder = renamingItem!!.type == "dir",
            onDismiss = { renamingItem = null },
            onStageRename = { newName ->
                onStageRename(renamingItem!!, newName)
                renamingItem = null
            },
            onConfirmRename = { newName, commitMsg ->
                onRenameItem(renamingItem!!, newName, commitMsg)
                renamingItem = null
            }
        )
    }

    // Run Workflow Dispatch Dialog
    if (runningWorkflow != null) {
        RunWorkflowDialog(
            workflow = runningWorkflow!!,
            activeBranch = activeBranch,
            branches = branches,
            onDismiss = { runningWorkflow = null },
            onConfirm = { branch ->
                onTriggerWorkflow(runningWorkflow!!.id, branch)
                runningWorkflow = null
            }
        )
    }

    // Add Single Secret Dialog
    if (showAddSecretDialog) {
        AddSecretDialog(
            repoFullName = repo.fullName,
            onDismiss = { showAddSecretDialog = false },
            onConfirm = { name, value ->
                onAddSecret(name, value) {
                    showAddSecretDialog = false
                }
            }
        )
    }

    // Bulk Secrets Import Dialog
    if (showBulkSecretsDialog) {
        AddBulkSecretsDialog(
            repoFullName = repo.fullName,
            onDismiss = { showBulkSecretsDialog = false },
            onConfirm = { rawText ->
                onAddBulkSecrets(rawText) { count ->
                    showBulkSecretsDialog = false
                }
            }
        )
    }

    // Delete Secret Dialog
    if (deletingSecret != null) {
        DeleteSecretDialog(
            secretName = deletingSecret!!.name,
            onDismiss = { deletingSecret = null },
            onConfirm = {
                onDeleteSecret(deletingSecret!!.name)
                deletingSecret = null
            }
        )
    }
}

@Composable
private fun FileExplorerTab(
    currentPath: String,
    pathSegments: List<String>,
    items: List<GitHubContentItem>,
    isLoading: Boolean,
    onNavigateToDirectory: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onOpenFile: (String) -> Unit,
    onPreviewFile: (GitHubContentItem) -> Unit,
    onUploadClick: () -> Unit,
    onNewFileClick: () -> Unit,
    onDeleteFileClick: (GitHubContentItem) -> Unit,
    onRenameItemClick: (GitHubContentItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Breadcrumbs & Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "root",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (pathSegments.isEmpty()) PrimaryPurple else SecondaryCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToDirectory("") }
                )
                var accumulated = ""
                for (seg in pathSegments) {
                    accumulated += if (accumulated.isEmpty()) seg else "/$seg"
                    val target = accumulated
                    Text(text = " / ", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text(
                        text = seg,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (target == currentPath) PrimaryPurple else SecondaryCyan,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onNavigateToDirectory(target) }
                    )
                }
            }

            // Quick Actions: New File & Upload
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onNewFileClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkElevatedSurface),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp).testTag("new_file_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp), tint = SecondaryCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New File", fontSize = 11.sp, color = SecondaryCyan)
                }

                Button(
                    onClick = onUploadClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp).testTag("upload_file_btn")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", fontSize = 11.sp)
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Directory is empty.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pathSegments.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateUp() },
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = "Up", tint = SecondaryCyan)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(".. (parent directory)", color = SecondaryCyan)
                            }
                        }
                    }
                }

                items(items) { item ->
                    val isDir = item.type == "dir"
                    val isBinaryOrPreview = FileUtils.isBinary(item.name) ||
                            FileUtils.isImage(item.name) ||
                            FileUtils.isDocument(item.name) ||
                            FileUtils.isMedia(item.name)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                if (isDir) {
                                    onNavigateToDirectory(item.path)
                                } else if (isBinaryOrPreview) {
                                    onPreviewFile(item)
                                } else {
                                    onOpenFile(item.path)
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isDir) Icons.Default.Folder
                                    else getFileIcon(item.name),
                                    contentDescription = null,
                                    tint = if (isDir) PrimaryPurple
                                    else if (FileUtils.isImage(item.name)) StatusWarning
                                    else if (FileUtils.isDocument(item.name)) StatusSuccess
                                    else SecondaryCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        fontWeight = if (isDir) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!isDir && isBinaryOrPreview) {
                                        Text(
                                            text = FileUtils.getFileTypeLabel(item.name),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!isDir && item.size > 0) {
                                    Text(
                                        text = formatFileSize(item.size),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                // Rename Action for Folders, Files, Images, and Raw binary assets
                                IconButton(
                                    onClick = { onRenameItemClick(item) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("rename_item_${item.name}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DriveFileRenameOutline,
                                        contentDescription = "Rename ${item.name}",
                                        tint = SecondaryCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                if (!isDir) {
                                    IconButton(
                                        onClick = { onDeleteFileClick(item) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("delete_file_${item.name}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete File",
                                            tint = StatusError.copy(alpha = 0.85f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateFileDialog(
    currentPath: String,
    onDismiss: () -> Unit,
    onStage: (String, String) -> Unit = { _, _ -> },
    onConfirm: (String, String, String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var fileContent by remember { mutableStateOf("") }
    var commitMessage by remember { mutableStateOf("") }

    val templates = listOf(
        "Blank" to "",
        "Kotlin" to "package com.example\n\nclass NewModule {\n    fun execute() {\n        println(\"Running...\")\n    }\n}\n",
        "Python" to "#!/usr/bin/env python3\n\ndef main():\n    print(\"Hello from Repo Manager Pro\")\n\nif __name__ == \"__main__\":\n    main()\n",
        "TypeScript" to "export interface Config {\n    version: string;\n    enabled: boolean;\n}\n\nexport function init(): void {\n    console.log(\"Initialized\");\n}\n",
        "Markdown" to "# Project Documentation\n\nCreated with Repo Manager Pro.\n\n## Overview\nDescribe your feature here.\n",
        "JSON" to "{\n  \"name\": \"project\",\n  \"version\": \"1.0.0\",\n  \"private\": true\n}\n"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Create New File",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Directory: ${if (currentPath.isBlank()) "root (/)" else currentPath}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = SecondaryCyan
                )

                OutlinedTextField(
                    value = fileName,
                    onValueChange = {
                        fileName = it
                        if (commitMessage.isBlank() || commitMessage.startsWith("Create ")) {
                            commitMessage = "Create $it via Repo Manager Pro"
                        }
                    },
                    label = { Text("File Name * (e.g. App.kt, script.py)") },
                    modifier = Modifier.fillMaxWidth().testTag("create_file_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Text(
                    text = "Templates:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(templates) { (label, code) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkElevatedSurface)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable {
                                    fileContent = code
                                    if (fileName.isBlank()) {
                                        fileName = when (label) {
                                            "Kotlin" -> "NewModule.kt"
                                            "Python" -> "script.py"
                                            "TypeScript" -> "index.ts"
                                            "Markdown" -> "README.md"
                                            "JSON" -> "config.json"
                                            else -> fileName
                                        }
                                        commitMessage = "Create $fileName via Repo Manager Pro"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(label, fontSize = 11.sp, color = SecondaryCyan)
                        }
                    }
                }

                OutlinedTextField(
                    value = fileContent,
                    onValueChange = { fileContent = it },
                    label = { Text("Initial Code / Content") },
                    modifier = Modifier.fillMaxWidth().height(140.dp).testTag("create_file_content_input"),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message") },
                    modifier = Modifier.fillMaxWidth().testTag("create_file_commit_msg"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onStage(fileName.trim(), fileContent)
                    },
                    enabled = fileName.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("stage_create_file_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stage (Batch)")
                }

                Button(
                    onClick = {
                        onConfirm(
                            fileName.trim(),
                            fileContent,
                            commitMessage.ifBlank { "Create ${fileName.trim()}" }
                        )
                    },
                    enabled = fileName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_create_file_btn")
                ) {
                    Text("Commit Now")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun DeleteFileDialog(
    item: GitHubContentItem,
    activeBranch: String,
    onDismiss: () -> Unit,
    onStageDelete: (GitHubContentItem) -> Unit = {},
    onConfirm: (GitHubContentItem, String) -> Unit
) {
    var commitMessage by remember { mutableStateOf("Delete ${item.name} via Repo Manager Pro") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = StatusError, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete File?", color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Are you sure you want to permanently delete '${item.name}' from branch '$activeBranch' on GitHub?",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Path: ${item.path}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = SecondaryCyan
                )

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message") },
                    modifier = Modifier.fillMaxWidth().testTag("delete_file_commit_msg"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedBorderColor = StatusError,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onStageDelete(item) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusError.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("stage_delete_file_btn")
                ) {
                    Text("Stage (Batch)")
                }

                Button(
                    onClick = { onConfirm(item, commitMessage) },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_file_btn")
                ) {
                    Text("Delete Now")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun UploadFileDialog(
    isUploading: Boolean,
    uploadError: String?,
    currentPath: String,
    onDismiss: () -> Unit,
    onStage: (String, ByteArray) -> Unit = { _, _ -> },
    onConfirm: (String, ByteArray, String) -> Unit
) {
    val context = LocalContext.current
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var commitMessage by remember { mutableStateOf("Upload file via Repo Manager Pro") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                val name = uri.lastPathSegment?.substringAfterLast('/') ?: "uploaded_asset"
                selectedFileName = name
                selectedFileBytes = bytes
            } catch (_: Exception) {}
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Upload File or Media", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Upload any image, video, code, or document into '${currentPath.ifBlank { "root" }}'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Icon(Icons.Default.CloudUpload, null, tint = SecondaryCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedFileName.isBlank()) "Choose File / Media" else "Selected: $selectedFileName")
                }

                if (selectedFileBytes != null) {
                    Text(
                        text = "File size: ${formatFileSize(selectedFileBytes!!.size.toLong())}",
                        fontSize = 11.sp,
                        color = StatusSuccess
                    )
                }

                OutlinedTextField(
                    value = selectedFileName,
                    onValueChange = { selectedFileName = it },
                    label = { Text("Destination Filename") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                if (!uploadError.isNullOrBlank()) {
                    Text(text = uploadError, color = StatusError, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        if (selectedFileBytes != null && selectedFileName.isNotBlank()) {
                            onStage(selectedFileName.trim(), selectedFileBytes!!)
                        }
                    },
                    enabled = selectedFileBytes != null && selectedFileName.isNotBlank() && !isUploading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("stage_upload_file_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stage (Batch)")
                }

                Button(
                    onClick = {
                        if (selectedFileBytes != null && selectedFileName.isNotBlank()) {
                            onConfirm(selectedFileName.trim(), selectedFileBytes!!, commitMessage.trim())
                        }
                    },
                    enabled = selectedFileBytes != null && selectedFileName.isNotBlank() && !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_upload_file_btn")
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Upload Now")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun ImagePreviewDialog(
    item: GitHubContentItem,
    repoFullName: String,
    branch: String,
    onDismiss: () -> Unit
) {
    val rawUrl = item.downloadUrl
        ?: "https://raw.githubusercontent.com/$repoFullName/$branch/${item.path}"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.name, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBg),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = rawUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Path: ${item.path} • Size: ${formatFileSize(item.size)}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun LoadCustomRepoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Load Any GitHub Repository", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter any public or private GitHub repository (format: owner/repo).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("e.g. facebook/react or torvalds/linux") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(query.trim()) },
                enabled = query.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Fetch & Open")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun CommitsTab(commits: List<GitHubCommit>) {
    if (commits.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No commit history available.", color = TextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(commits) { commit ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = commit.commit.author.name.ifBlank { "Author" },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkElevatedSurface)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = commit.sha.take(7),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = SecondaryCyan
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = commit.commit.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = commit.commit.author.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun BranchesTab(
    branches: List<GitHubBranch>,
    activeBranch: String,
    onSelectBranch: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(branches) { branch ->
            val isActive = branch.name == activeBranch
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectBranch(branch.name) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isActive) PrimaryPurple else BorderSubtle
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = if (isActive) PrimaryPurple else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = branch.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) PrimaryPurple else TextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (branch.protected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusWarning.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("protected", fontSize = 10.sp, color = StatusWarning)
                            }
                        }
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryPurple.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("active", fontSize = 10.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateRepoDialog(
    isCreating: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var autoInit by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Create New Repository",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Repository Name *") },
                    placeholder = { Text("e.g. awesome-engine") },
                    modifier = Modifier.fillMaxWidth().testTag("new_repo_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isPrivate,
                        onClick = { isPrivate = false },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
                    )
                    Text("Public", color = TextPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = isPrivate,
                        onClick = { isPrivate = true },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryPurple)
                    )
                    Text("Private", color = TextPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = autoInit,
                        onCheckedChange = { autoInit = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                    )
                    Text("Initialize with README", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(text = errorMessage, color = StatusError, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), description.trim(), isPrivate, autoInit) },
                enabled = name.isNotBlank() && !isCreating,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                modifier = Modifier.testTag("confirm_create_repo_btn")
            ) {
                if (isCreating) {
                    CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(16.dp))
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private fun isImageFile(fileName: String): Boolean {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return ext in listOf("png", "jpg", "jpeg", "gif", "webp", "svg", "ico")
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format("%.1f MB", mb)
}

// -------------------------------------------------------------
// Actions & Workflows Tab
// -------------------------------------------------------------
@Composable
private fun ActionsWorkflowsTab(
    workflows: List<GitHubWorkflow>,
    checkRuns: List<GitHubCheckRun>,
    activeBranch: String,
    isWorkflowsLoading: Boolean,
    isCheckRunsLoading: Boolean,
    onRunWorkflow: (GitHubWorkflow) -> Unit,
    onToggleWorkflow: (GitHubWorkflow) -> Unit,
    onOpenFile: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("actions_workflows_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Workflows Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GitHub Actions Workflows",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (isWorkflowsLoading) {
                    CircularProgressIndicator(color = PrimaryPurple, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (workflows.isEmpty() && !isWorkflowsLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No workflows found in .github/workflows", color = TextSecondary)
                    }
                }
            }
        } else {
            items(workflows) { wf ->
                val isActive = wf.state == "active"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workflow_item_${wf.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isActive) PrimaryPurple.copy(alpha = 0.2f) else DarkElevatedSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        tint = if (isActive) PrimaryPurple else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = wf.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = wf.path,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextTertiary
                                    )
                                }
                            }

                            // State Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isActive) StatusSuccess.copy(alpha = 0.15f) else DarkElevatedSurface)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isActive) "Active" else "Disabled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isActive) StatusSuccess else TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Run, Disable/Enable, Open File
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Run Workflow Dispatch
                            Button(
                                onClick = { onRunWorkflow(wf) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("run_workflow_btn_${wf.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Run", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Disable / Enable Toggle
                            OutlinedButton(
                                onClick = { onToggleWorkflow(wf) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isActive) StatusWarning else StatusSuccess
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isActive) StatusWarning.copy(alpha = 0.5f) else StatusSuccess.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isActive) "Disable" else "Enable", fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Open YAML file in Editor
                            OutlinedButton(
                                onClick = { onOpenFile(wf.path) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("open_workflow_file_${wf.id}")
                            ) {
                                Icon(Icons.Default.Code, null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open File", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Check Runs / Commit Statuses
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SecondaryCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Check Runs & Commit Statuses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkElevatedSurface)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(activeBranch, fontSize = 11.sp, color = SecondaryCyan)
                }
            }
        }

        if (checkRuns.isEmpty() && !isCheckRunsLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No check runs reported for this branch.", color = TextSecondary)
                    }
                }
            }
        } else {
            items(checkRuns) { check ->
                val isSuccess = check.conclusion == "success"
                val isFailed = check.conclusion == "failure"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = when {
                                    isSuccess -> Icons.Default.CheckCircle
                                    isFailed -> Icons.Default.Cancel
                                    else -> Icons.Default.Schedule
                                },
                                contentDescription = null,
                                tint = when {
                                    isSuccess -> StatusSuccess
                                    isFailed -> StatusError
                                    else -> StatusWarning
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = check.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Status: ${check.status} • Conclusion: ${check.conclusion ?: "pending"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        isSuccess -> StatusSuccess.copy(alpha = 0.15f)
                                        isFailed -> StatusError.copy(alpha = 0.15f)
                                        else -> StatusWarning.copy(alpha = 0.15f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = (check.conclusion ?: check.status).uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isSuccess -> StatusSuccess
                                    isFailed -> StatusError
                                    else -> StatusWarning
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Repository Action Secrets Tab
// -------------------------------------------------------------
@Composable
private fun RepoSecretsTab(
    secrets: List<RepoSecretItem>,
    isLoading: Boolean,
    onAddSecretClick: () -> Unit,
    onBulkSecretsClick: () -> Unit,
    onDeleteSecretClick: (RepoSecretItem) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("repo_secrets_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Action Secrets",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(color = PrimaryPurple, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Encrypted repository secrets are masked in logs and exposed to GitHub Actions workflows as environment variables.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAddSecretClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_secret_button")
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Secret", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onBulkSecretsClick,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkElevatedSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bulk_secrets_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, null, tint = SecondaryCyan, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bulk Secrets", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // List of Secrets
        if (secrets.isEmpty() && !isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No secrets configured yet. Tap 'New Secret' or 'Bulk Secrets' to add.", color = TextSecondary)
                    }
                }
            }
        } else {
            items(secrets) { sec ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("secret_item_${sec.name}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkElevatedSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = sec.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SecondaryCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = if (sec.isLocal) "Vault" else "Encrypted",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = SecondaryCyan
                                        )
                                    }
                                }
                                Text(
                                    text = if (sec.updatedAt.isNotBlank()) "Updated: ${sec.updatedAt.take(10)}" else "Secret configured",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(sec.name))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, "Copy secret name", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { onDeleteSecretClick(sec) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Delete secret", tint = StatusError, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Dialogs: Run Workflow, Add Secret, Bulk Secrets, Delete Secret
// -------------------------------------------------------------
@Composable
private fun RunWorkflowDialog(
    workflow: GitHubWorkflow,
    activeBranch: String,
    branches: List<GitHubBranch>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedBranch by remember { mutableStateOf(activeBranch) }
    var branchMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RocketLaunch, null, tint = PrimaryPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Run Workflow", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Dispatch a manual run for workflow '${workflow.name}' (${workflow.path}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Text("Target Branch:", style = MaterialTheme.typography.labelMedium, color = TextPrimary)

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkElevatedSurface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { branchMenuOpen = true }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedBranch, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = branchMenuOpen,
                        onDismissRequest = { branchMenuOpen = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        val branchOptions = if (branches.isEmpty()) listOf(selectedBranch) else branches.map { it.name }
                        branchOptions.forEach { bName ->
                            DropdownMenuItem(
                                text = { Text(bName, color = TextPrimary) },
                                onClick = {
                                    selectedBranch = bName
                                    branchMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedBranch) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                modifier = Modifier.testTag("confirm_run_workflow_btn")
            ) {
                Text("Trigger Run")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun AddSecretDialog(
    repoFullName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var secretName by remember { mutableStateOf("") }
    var secretValue by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VpnKey, null, tint = PrimaryPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Action Secret", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Add an encrypted secret to $repoFullName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = secretName,
                    onValueChange = { input ->
                        secretName = input.uppercase().replace(" ", "_")
                        errorMessage = null
                    },
                    label = { Text("Secret Name *") },
                    placeholder = { Text("e.g. DATABASE_URL") },
                    modifier = Modifier.fillMaxWidth().testTag("secret_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = secretValue,
                    onValueChange = {
                        secretValue = it
                        errorMessage = null
                    },
                    label = { Text("Secret Value *") },
                    placeholder = { Text("Paste secret value or token") },
                    modifier = Modifier.fillMaxWidth().testTag("secret_value_input"),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide value" else "Show value",
                                tint = TextSecondary
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = StatusError, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = secretName.trim().uppercase()
                    if (clean.isBlank()) {
                        errorMessage = "Please enter a secret name"
                    } else if (clean.startsWith("GITHUB_")) {
                        errorMessage = "Secret name cannot start with GITHUB_"
                    } else if (secretValue.isEmpty()) {
                        errorMessage = "Please enter a secret value"
                    } else {
                        onConfirm(clean, secretValue)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                modifier = Modifier.testTag("save_secret_btn")
            ) {
                Text("Save Secret")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun AddBulkSecretsDialog(
    repoFullName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }

    val detectedCount = remember(rawText) {
        rawText.lines().count { line ->
            val trim = line.trim()
            trim.isNotEmpty() && !trim.startsWith("#") && !trim.startsWith("//") && (trim.contains("=") || trim.contains(":"))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentCopy, null, tint = SecondaryCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Bulk Secrets", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Paste your .env file or multiple KEY=VALUE pairs below to import all secrets at once.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = {
                        Text(
                            "DATABASE_URL=postgres://...\nCLOUDFLARE_API_KEY=xyz...\nPORT=8080\nSTRIPE_SECRET=sk_live_...",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("bulk_secrets_input"),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (detectedCount > 0) "$detectedCount secrets detected" else "No key-value pairs detected yet",
                        fontSize = 11.sp,
                        color = if (detectedCount > 0) StatusSuccess else TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    TextButton(
                        onClick = {
                            rawText = "DATABASE_URL=postgres://user:pass@db:5432/main\nAPI_SECRET_KEY=prod_sk_live_9942\nREDIS_HOST=redis.internal.net\nPORT=8080"
                        }
                    ) {
                        Text("Fill Sample", fontSize = 11.sp, color = SecondaryCyan)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rawText) },
                enabled = rawText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                modifier = Modifier.testTag("import_bulk_secrets_btn")
            ) {
                Text("Import All Secrets")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
private fun DeleteSecretDialog(
    secretName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Delete Secret", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Text(
                "Are you sure you want to delete secret '$secretName'? Workflows relying on this secret may fail.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = StatusError)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
