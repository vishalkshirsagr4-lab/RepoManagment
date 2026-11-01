package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.models.DeploymentProvider
import com.example.data.remote.models.GitHubWorkflowRun
import com.example.data.remote.models.UnifiedDeployment
import com.example.data.remote.models.WorkflowJob
import com.example.ui.components.ProviderBadge
import com.example.ui.components.StatusPill
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
import com.example.ui.viewmodels.DeploymentsViewModel

@Composable
fun DeploymentsScreen(
    viewModel: DeploymentsViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0: GitHub Actions, 1: Cloudflare & HF

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .testTag("deployments_screen")
    ) {
        // Section Tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = DarkSurface,
            contentColor = PrimaryPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                    color = PrimaryPurple
                )
            }
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Hub, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GitHub Actions CI/CD", color = if (selectedSection == 0) PrimaryPurple else TextSecondary)
                    }
                }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cloud, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cloudflare & Spaces", color = if (selectedSection == 1) PrimaryPurple else TextSecondary)
                    }
                }
            )
        }

        // Status notification
        if (!uiState.statusMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryPurple.copy(alpha = 0.15f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(text = uiState.statusMessage!!, color = PrimaryPurple, style = MaterialTheme.typography.bodySmall)
            }
        }

        if (selectedSection == 0) {
            // GitHub Actions Section
            GitHubActionsSection(
                viewModel = viewModel,
                uiState = uiState,
                onNavigateToSettings = onNavigateToSettings
            )
        } else {
            // Cloudflare & Hugging Face Section
            CloudflareAndHfSection(
                viewModel = viewModel,
                uiState = uiState,
                onNavigateToSettings = onNavigateToSettings,
                onCopyUrl = { url -> clipboardManager.setText(AnnotatedString(url)) }
            )
        }
    }
}

@Composable
private fun GitHubActionsSection(
    viewModel: DeploymentsViewModel,
    uiState: com.example.ui.viewmodels.DeploymentsUiState,
    onNavigateToSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Controls Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Live CI/CD Pipelines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Target: ${uiState.activeRepo}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = SecondaryCyan
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Auto-refresh polling button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isAutoRefresh) StatusSuccess.copy(alpha = 0.2f) else DarkElevatedSurface)
                                .border(1.dp, if (uiState.isAutoRefresh) StatusSuccess else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleAutoRefresh() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isAutoRefresh) StatusSuccess else TextTertiary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.isAutoRefresh) "Live (5s)" else "Auto-poll",
                                    fontSize = 11.sp,
                                    color = if (uiState.isAutoRefresh) StatusSuccess else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.fetchLiveWorkflowRuns() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // If a specific run is selected, show its live step execution logs
        if (uiState.selectedRunId != null) {
            item {
                WorkflowRunDetailCard(
                    runId = uiState.selectedRunId,
                    jobs = uiState.selectedRunJobs,
                    onBack = { viewModel.clearSelectedRun() },
                    onRerun = { viewModel.rerunWorkflow(uiState.selectedRunId) }
                )
            }
        }

        // Workflow Runs List
        val runs = uiState.liveWorkflowRuns
        if (uiState.isLoading && runs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            }
        } else if (runs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No GitHub Actions workflows configured or found for ${uiState.activeRepo}.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        } else {
            items(runs) { run ->
                WorkflowRunCard(
                    run = run,
                    isSelected = run.id == uiState.selectedRunId,
                    onClick = { viewModel.selectWorkflowRun(run) },
                    onRerun = { viewModel.rerunWorkflow(run.id) }
                )
            }
        }
    }
}

@Composable
private fun WorkflowRunCard(
    run: GitHubWorkflowRun,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRerun: () -> Unit
) {
    val statusText = run.conclusion ?: run.status
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimaryPurple else BorderSubtle
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val statusIcon = when (run.conclusion?.lowercase()) {
                        "success" -> Icons.Default.CheckCircle
                        "failure" -> Icons.Default.Error
                        else -> Icons.Default.Pending
                    }
                    val statusColor = when (run.conclusion?.lowercase()) {
                        "success" -> StatusSuccess
                        "failure" -> StatusError
                        else -> StatusWarning
                    }

                    Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = run.name.ifBlank { "Workflow #${run.runNumber}" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                StatusPill(status = statusText)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${run.headBranch} • ${run.headSha?.take(7) ?: ""}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = SecondaryCyan
                    )
                    Text(
                        text = " • Event: ${run.event}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }

                IconButton(onClick = onRerun, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Replay, contentDescription = "Rerun", tint = TextSecondary, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun WorkflowRunDetailCard(
    runId: Long,
    jobs: List<WorkflowJob>,
    onBack: () -> Unit,
    onRerun: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkElevatedSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Run #$runId Execution Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = onRerun,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Re-run", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (jobs.isEmpty()) {
                Text(
                    text = "Loading step execution logs...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                jobs.forEach { job ->
                    Text(
                        text = "Job: ${job.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SecondaryCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    job.steps.forEach { step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (step.conclusion) {
                                "success" -> Icons.Default.CheckCircle
                                "failure" -> Icons.Default.Error
                                else -> Icons.Default.Pending
                            }
                            val tint = when (step.conclusion) {
                                "success" -> StatusSuccess
                                "failure" -> StatusError
                                else -> StatusWarning
                            }
                            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${step.number}. ${step.name}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun CloudflareAndHfSection(
    viewModel: DeploymentsViewModel,
    uiState: com.example.ui.viewmodels.DeploymentsUiState,
    onNavigateToSettings: () -> Unit,
    onCopyUrl: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(uiState.deployments) { deployment ->
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
                        ProviderBadge(provider = deployment.provider)
                        StatusPill(status = deployment.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = deployment.projectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${deployment.branch} • ${deployment.commitSha}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = SecondaryCyan
                    )

                    if (!deployment.deployedUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkElevatedSurface)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = deployment.deployedUrl,
                                fontSize = 11.sp,
                                color = SecondaryCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onCopyUrl(deployment.deployedUrl) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.ContentCopy, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    if (!deployment.logsExcerpt.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                                .padding(8.dp)
                        ) {
                            Text(text = deployment.logsExcerpt, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            onClick = { viewModel.triggerRedeploy(deployment.id) },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Icon(Icons.Default.Refresh, null, tint = PrimaryPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trigger Build", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}
