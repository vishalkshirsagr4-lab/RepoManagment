package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BackupsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeploymentsScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.RepositoriesScreen
import com.example.ui.screens.PollinationsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodels.BackupsViewModel
import com.example.ui.viewmodels.DashboardViewModel
import com.example.ui.viewmodels.DeploymentsViewModel
import com.example.ui.viewmodels.EditorViewModel
import com.example.ui.viewmodels.RepoViewModel
import com.example.ui.viewmodels.SettingsViewModel

enum class NavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Default.Home, "nav_home"),
    REPOSITORIES("Repositories", Icons.Default.Folder, "nav_repos"),
    EDITOR("Editor", Icons.Default.Code, "nav_editor"),
    DEPLOYMENTS("Deployments", Icons.Default.RocketLaunch, "nav_deployments"),
    BACKUPS("Backups", Icons.Default.Archive, "nav_backups"),
    AI("AI", Icons.Default.AutoAwesome, "nav_ai"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }

    // Shared ViewModels
    val dashboardViewModel: DashboardViewModel = viewModel()
    val repoViewModel: RepoViewModel = viewModel()
    val editorViewModel: EditorViewModel = viewModel()
    val deploymentsViewModel: DeploymentsViewModel = viewModel()
    val backupsViewModel: BackupsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    // Handle Android system back button when on sub-destinations
    BackHandler(enabled = currentDestination != NavDestination.HOME) {
        currentDestination = NavDestination.HOME
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Tablet / Foldable Layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize().background(DarkBg)) {
                NavigationRail(
                    containerColor = DarkSurface,
                    contentColor = TextSecondary,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    NavDestination.entries.forEach { dest ->
                        val isSelected = currentDestination == dest
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { currentDestination = dest },
                            icon = {
                                Icon(
                                    imageVector = dest.icon,
                                    contentDescription = dest.title,
                                    tint = if (isSelected) PrimaryPurple else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = dest.title,
                                    fontSize = 11.sp,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = DarkElevatedSurface
                            ),
                            modifier = Modifier.testTag(dest.testTag)
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(
                        destination = currentDestination,
                        dashboardViewModel = dashboardViewModel,
                        repoViewModel = repoViewModel,
                        editorViewModel = editorViewModel,
                        deploymentsViewModel = deploymentsViewModel,
                        backupsViewModel = backupsViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateTo = { currentDestination = it }
                    )
                }
            }
        } else {
            // Handheld Phone Layout with Bottom Navigation Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = DarkBg,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkSurface,
                        contentColor = TextSecondary,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("bottom_nav_bar")
                    ) {
                        // 5 main tabs for compact screens
                        val compactTabs = listOf(
                            NavDestination.HOME,
                            NavDestination.REPOSITORIES,
                            NavDestination.EDITOR,
                            NavDestination.DEPLOYMENTS,
                            NavDestination.AI,
                            NavDestination.SETTINGS
                        )

                        compactTabs.forEach { dest ->
                            val isSelected = currentDestination == dest
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.title,
                                        tint = if (isSelected) PrimaryPurple else TextTertiary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) TextPrimary else TextTertiary
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = DarkElevatedSurface
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    ScreenContent(
                        destination = currentDestination,
                        dashboardViewModel = dashboardViewModel,
                        repoViewModel = repoViewModel,
                        editorViewModel = editorViewModel,
                        deploymentsViewModel = deploymentsViewModel,
                        backupsViewModel = backupsViewModel,
                        settingsViewModel = settingsViewModel,
                        onNavigateTo = { currentDestination = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    destination: NavDestination,
    dashboardViewModel: DashboardViewModel,
    repoViewModel: RepoViewModel,
    editorViewModel: EditorViewModel,
    deploymentsViewModel: DeploymentsViewModel,
    backupsViewModel: BackupsViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateTo: (NavDestination) -> Unit
) {
    when (destination) {
        NavDestination.HOME -> DashboardScreen(
            viewModel = dashboardViewModel,
            onNavigateToRepos = { onNavigateTo(NavDestination.REPOSITORIES) },
            onNavigateToEditor = { repoFullName, path, branch ->
                editorViewModel.openFile(repoFullName, path, branch)
                onNavigateTo(NavDestination.EDITOR)
            },
            onNavigateToDeployments = { onNavigateTo(NavDestination.DEPLOYMENTS) },
            onNavigateToBackups = { onNavigateTo(NavDestination.BACKUPS) },
            onNavigateToSettings = { onNavigateTo(NavDestination.SETTINGS) },
            onOpenCreateRepo = {
                onNavigateTo(NavDestination.REPOSITORIES)
            },
            onSelectRepo = { repo ->
                repoViewModel.selectRepo(repo)
            }
        )
        NavDestination.REPOSITORIES -> RepositoriesScreen(
            viewModel = repoViewModel,
            onOpenFileInEditor = { repoFullName, path, branch ->
                editorViewModel.openFile(repoFullName, path, branch)
                onNavigateTo(NavDestination.EDITOR)
            }
        )
        NavDestination.EDITOR -> EditorScreen(
            viewModel = editorViewModel
        )
        NavDestination.DEPLOYMENTS -> DeploymentsScreen(
            viewModel = deploymentsViewModel,
            onNavigateToSettings = { onNavigateTo(NavDestination.SETTINGS) }
        )
        NavDestination.BACKUPS -> BackupsScreen(
            viewModel = backupsViewModel
        )
        NavDestination.AI -> PollinationsScreen()
        NavDestination.SETTINGS -> SettingsScreen(
            viewModel = settingsViewModel
        )
    }
}
