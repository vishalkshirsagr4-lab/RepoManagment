package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.remote.models.GitHubRepo
import com.example.ui.components.LanguageBadge
import com.example.ui.components.StatusPill
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodels.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToRepos: () -> Unit,
    onNavigateToEditor: (String, String, String) -> Unit,
    onNavigateToDeployments: () -> Unit,
    onNavigateToBackups: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenCreateRepo: () -> Unit,
    onSelectRepo: (GitHubRepo) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputToken by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    if (uiState.isLoading && !isVerifying) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryPurple)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Welcome & Connection Banner
        item {
            WelcomeHeader(
                userName = uiState.user?.name ?: uiState.user?.login ?: "Developer",
                userLogin = uiState.user?.login ?: "guest",
                avatarUrl = uiState.user?.avatarUrl,
                isDemoMode = uiState.isDemoMode,
                isGitHubConnected = uiState.isGitHubConnected,
                onSettingsClick = onNavigateToSettings
            )
        }

        // 2. Direct GitHub Connect CTA if not connected
        if (!uiState.isGitHubConnected) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Connect Live GitHub Account",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Authenticate securely to browse, edit, commit, and monitor live CI/CD workflows. Token is encrypted with hardware AES-256 GCM.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputToken,
                                onValueChange = { inputToken = it },
                                placeholder = { Text("Paste Personal Access Token (ghp_...)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkElevatedSurface,
                                    unfocusedContainerColor = DarkElevatedSurface,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = BorderSubtle
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (inputToken.isNotBlank()) {
                                        isVerifying = true
                                        viewModel.connectGitHub(inputToken) { _, _ ->
                                            isVerifying = false
                                        }
                                    }
                                },
                                enabled = inputToken.isNotBlank() && !isVerifying,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isVerifying) {
                                    CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("Connect")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Metrics Grid
        item {
            MetricsGrid(
                repoCount = uiState.repos.size,
                deploymentCount = uiState.deployments.size,
                backupCount = uiState.backupCount,
                isDemoMode = uiState.isDemoMode
            )
        }

        // 4. Quick Actions
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            QuickActionsRow(
                onBrowseRepos = onNavigateToRepos,
                onCreateRepo = onOpenCreateRepo,
                onOpenEditor = {
                    val defaultRepo = uiState.repos.firstOrNull()?.fullName ?: "octodev/repomanager-core"
                    onNavigateToEditor(defaultRepo, "src/main/kotlin/RepositoryManager.kt", "main")
                },
                onViewDeployments = onNavigateToDeployments,
                onViewBackups = onNavigateToBackups
            )
        }

        // 5. Cloud Hosting Integrations
        item {
            Text(
                text = "Cloud Integrations",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            IntegrationsRow(
                isGitHubConnected = uiState.isGitHubConnected,
                isCloudflareConnected = uiState.isCloudflareConnected,
                isHuggingFaceConnected = uiState.isHuggingFaceConnected,
                isDemoMode = uiState.isDemoMode,
                onManageClick = onNavigateToSettings
            )
        }

        // 6. Recent Repositories Carousel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Repositories",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "View all (${uiState.repos.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryCyan,
                    modifier = Modifier
                        .clickable { onNavigateToRepos() }
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.repos.isEmpty()) {
                EmptyStateCard(
                    message = "No repositories loaded. Connect your GitHub token above or load any public repo.",
                    buttonText = "Browse Repositories",
                    onClick = onNavigateToRepos
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.repos.take(6)) { repo ->
                        RecentRepoCard(
                            repo = repo,
                            onClick = {
                                onSelectRepo(repo)
                                onNavigateToRepos()
                            }
                        )
                    }
                }
            }
        }

        // 7. Active Deployments Snippet
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Cloud Deployments",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "View pipelines",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryCyan,
                    modifier = Modifier
                        .clickable { onNavigateToDeployments() }
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.deployments.take(3).forEach { dep ->
                    DeploymentSummaryItem(
                        deployment = dep,
                        onClick = onNavigateToDeployments
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeader(
    userName: String,
    userLogin: String,
    avatarUrl: String?,
    isDemoMode: Boolean,
    isGitHubConnected: Boolean,
    onSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, PrimaryPurple, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DarkElevatedSurface)
                            .border(1.5.dp, PrimaryPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isGitHubConnected) "@$userLogin (Live)" else "Not authenticated",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isGitHubConnected) StatusSuccess.copy(alpha = 0.15f)
                        else StatusWarning.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (isGitHubConnected) StatusSuccess.copy(alpha = 0.4f)
                        else StatusWarning.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSettingsClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isGitHubConnected) StatusSuccess else StatusWarning)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGitHubConnected) "GitHub Live" else "Setup Key",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGitHubConnected) StatusSuccess else StatusWarning
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricsGrid(
    repoCount: Int,
    deploymentCount: Int,
    backupCount: Int,
    isDemoMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricItem(
            title = "Repositories",
            value = repoCount.toString(),
            icon = Icons.Default.FolderOpen,
            tint = PrimaryPurple,
            modifier = Modifier.weight(1f)
        )
        MetricItem(
            title = "Deployments",
            value = deploymentCount.toString(),
            icon = Icons.Default.RocketLaunch,
            tint = SecondaryCyan,
            modifier = Modifier.weight(1f)
        )
        MetricItem(
            title = "Backups",
            value = backupCount.toString(),
            icon = Icons.Default.Archive,
            tint = StatusSuccess,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricItem(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onBrowseRepos: () -> Unit,
    onCreateRepo: () -> Unit,
    onOpenEditor: () -> Unit,
    onViewDeployments: () -> Unit,
    onViewBackups: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            label = "Repos",
            icon = Icons.Default.FolderOpen,
            color = PrimaryPurple,
            onClick = onBrowseRepos,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            label = "New",
            icon = Icons.Default.Add,
            color = SecondaryCyan,
            onClick = onCreateRepo,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            label = "VS Editor",
            icon = Icons.Default.Code,
            color = StatusSuccess,
            onClick = onOpenEditor,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            label = "CI/CD",
            icon = Icons.Default.RocketLaunch,
            color = Color(0xFFF97316),
            onClick = onViewDeployments,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun IntegrationsRow(
    isGitHubConnected: Boolean,
    isCloudflareConnected: Boolean,
    isHuggingFaceConnected: Boolean,
    isDemoMode: Boolean,
    onManageClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IntegrationChip(
            title = "GitHub",
            status = if (isGitHubConnected) "Live" else "Not set",
            icon = Icons.Default.Hub,
            accentColor = PrimaryPurple,
            onClick = onManageClick,
            modifier = Modifier.weight(1f)
        )
        IntegrationChip(
            title = "Cloudflare",
            status = if (isCloudflareConnected) "Configured" else "Ready",
            icon = Icons.Default.Cloud,
            accentColor = Color(0xFFF97316),
            onClick = onManageClick,
            modifier = Modifier.weight(1f)
        )
        IntegrationChip(
            title = "HuggingFace",
            status = if (isHuggingFaceConnected) "Configured" else "Ready",
            icon = Icons.Default.Layers,
            accentColor = Color(0xFFFBBF24),
            onClick = onManageClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IntegrationChip(
    title: String,
    status: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = status,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun RecentRepoCard(
    repo: GitHubRepo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (repo.private) Icons.Default.Lock else Icons.Default.Public,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = repo.description ?: "No description provided.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(34.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LanguageBadge(language = repo.language)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        tint = StatusWarning,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = repo.stargazersCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DeploymentSummaryItem(
    deployment: com.example.data.remote.models.UnifiedDeployment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deployment.projectName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${deployment.commitMessage} • ${deployment.updatedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusPill(status = deployment.status)
        }
    }
}

@Composable
private fun EmptyStateCard(
    message: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 14.dp)
            )
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(buttonText)
            }
        }
    }
}
