package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.data.security.SecureStorage
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.TableChart
import com.example.ui.common.FileUtils
import com.example.ui.components.BatchCommitAndPushDialog
import com.example.ui.components.PushStatusDialog
import com.example.ui.components.RenameItemDialog
import com.example.ui.components.getFileIcon
import com.example.ui.editor.DiffLine
import com.example.ui.editor.DiffType
import com.example.ui.editor.SyntaxHighlighter
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
import com.example.ui.viewmodels.EditorTab
import com.example.ui.viewmodels.EditorViewModel

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeTab = uiState.activeTab
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var repoMenuOpen by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(DarkBg)
            .testTag("editor_screen")
    ) {
        // 1. Top Bar: Repo Selector, Git Compare Toggle, Save & Commit Action
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Repo Switcher dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkElevatedSurface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { repoMenuOpen = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.currentRepo.substringAfterLast('/'),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = repoMenuOpen,
                        onDismissRequest = { repoMenuOpen = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        val repos = if (uiState.availableRepos.isEmpty()) {
                            listOf("octodev/repomanager-core", "octodev/cloud-edge-api", "octodev/llm-space-explorer")
                        } else {
                            uiState.availableRepos.map { it.fullName }
                        }
                        repos.forEach { rName ->
                            DropdownMenuItem(
                                text = { Text(rName, color = TextPrimary, fontSize = 12.sp) },
                                trailingIcon = {
                                    if (rName == uiState.currentRepo) {
                                        Icon(Icons.Default.Check, null, tint = PrimaryPurple)
                                    }
                                },
                                onClick = {
                                    viewModel.selectRepo(rName)
                                    repoMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Center/Right: Git Compare, Stage File, and Batch Commit & Push
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Git Compare Button
                    Button(
                        onClick = { viewModel.toggleCompareMode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isCompareMode) SecondaryCyan else DarkElevatedSurface,
                            contentColor = if (uiState.isCompareMode) DarkBg else TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("git_compare_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Difference,
                            contentDescription = "Git Compare",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isCompareMode) "Editor" else "Compare",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Stage Active File Button (Stores change to batch commit without committing immediately)
                    if (activeTab != null) {
                        Button(
                            onClick = { viewModel.stageActiveFile() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTab.isDirty) SecondaryCyan.copy(alpha = 0.2f) else DarkElevatedSurface,
                                contentColor = if (activeTab.isDirty) SecondaryCyan else TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("stage_file_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Stage File",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (activeTab.isDirty) "Stage *" else "Stage",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // One-Time Batch Commit & Push or Single-file Commit Button
                    if (uiState.stagedChanges.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.showBatchCommitDialog(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryPurple,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("batch_push_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Batch Push",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Push (${uiState.stagedChanges.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.showCommitDialog(true) },
                            enabled = activeTab != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTab?.isDirty == true) PrimaryPurple else DarkElevatedSurface,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("commit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Commit,
                                contentDescription = "Commit",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (activeTab?.isDirty == true) "Commit *" else "Commit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Staged Changes Banner
        if (uiState.stagedChanges.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryPurple.copy(alpha = 0.15f))
                    .border(androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f)))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.stagedChanges.size} ${if (uiState.stagedChanges.size == 1) "file" else "files"} staged for one-time push",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Review & Push",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryCyan,
                        modifier = Modifier
                            .clickable { viewModel.showBatchCommitDialog(true) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clear",
                        fontSize = 11.sp,
                        color = StatusError,
                        modifier = Modifier
                            .clickable { viewModel.clearAllStaged() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // 2. VS Code File Tabs Bar
        if (uiState.tabs.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .border(1.dp, BorderSubtle)
            ) {
                items(uiState.tabs) { tab ->
                    val isActive = tab.id == uiState.activeTabId
                    Row(
                        modifier = Modifier
                            .background(if (isActive) DarkBg else DarkSurfaceVariant)
                            .border(1.dp, if (isActive) PrimaryPurple.copy(alpha = 0.5f) else BorderSubtle)
                            .clickable { viewModel.selectTab(tab.id) }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getFileIcon(tab.fileName),
                            contentDescription = null,
                            tint = if (isActive) PrimaryPurple else SecondaryCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.fileName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) TextPrimary else TextSecondary
                        )

                        if (tab.isDirty) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StatusWarning)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close tab",
                            tint = TextTertiary,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { viewModel.closeTab(tab.id) }
                        )
                    }
                }
            }
        }

        // 3. Quick Code Actions Bar (VS Code Mobile Toolbar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Snippet insertion chips
            val snippets = listOf("{ }", "( )", "[ ]", "\" \"", "=>", "==", "::", "fun ", "val ", "if ()")
            snippets.forEach { snip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkElevatedSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .clickable { viewModel.insertSnippet(snip) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = snip,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SecondaryCyan
                    )
                }
            }

            // Quick actions
            if (activeTab != null) {
                IconButton(
                    onClick = { showRenameDialog = true },
                    modifier = Modifier.size(28.dp).testTag("editor_toolbar_rename_btn")
                ) {
                    Icon(
                        Icons.Default.DriveFileRenameOutline,
                        contentDescription = "Rename File",
                        tint = SecondaryCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            IconButton(
                onClick = { viewModel.undo() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Undo, contentDescription = "Undo", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = { viewModel.toggleSearch(!uiState.isSearching) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Find",
                    tint = if (uiState.isSearching) SecondaryCyan else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = { viewModel.toggleReplace() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.FindReplace,
                    contentDescription = "Replace",
                    tint = if (uiState.isReplacing) PrimaryPurple else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = { viewModel.toggleWordWrap() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.WrapText,
                    contentDescription = "Word Wrap",
                    tint = if (uiState.isWordWrap) PrimaryPurple else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = {
                    if (activeTab != null) {
                        clipboardManager.setText(AnnotatedString(activeTab.content))
                    }
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = { keyboardController?.hide() },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.KeyboardHide, contentDescription = "Hide Keyboard", tint = SecondaryCyan, modifier = Modifier.size(16.dp))
            }
        }

        // 4. In-File Find and Replace Bar (VS Code Engine)
        if (uiState.isSearching) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Header row with match toggles and match count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.FindReplace,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Search & Replace",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            // VS Code Search Modifier Chips
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (uiState.isMatchCase) SecondaryCyan.copy(alpha = 0.25f) else DarkElevatedSurface)
                                    .border(1.dp, if (uiState.isMatchCase) SecondaryCyan else BorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.toggleMatchCase() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Aa",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isMatchCase) SecondaryCyan else TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (uiState.isWholeWord) SecondaryCyan.copy(alpha = 0.25f) else DarkElevatedSurface)
                                    .border(1.dp, if (uiState.isWholeWord) SecondaryCyan else BorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.toggleWholeWord() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "\\b",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isWholeWord) SecondaryCyan else TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (uiState.isRegex) SecondaryCyan.copy(alpha = 0.25f) else DarkElevatedSurface)
                                    .border(1.dp, if (uiState.isRegex) SecondaryCyan else BorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.toggleRegex() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = ".*",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isRegex) SecondaryCyan else TextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Multiline expander
                            Text(
                                text = if (uiState.isMultilineExpanded) "Compact" else "Block",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier
                                    .clickable { viewModel.toggleMultilineExpanded() }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            IconButton(
                                onClick = { viewModel.toggleSearch(false) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, "Close search", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Original Code / Find Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "ORIGINAL CODE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBg)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("search_in_file_input"),
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                cursorBrush = SolidColor(SecondaryCyan),
                                maxLines = if (uiState.isMultilineExpanded) 6 else 1,
                                singleLine = !uiState.isMultilineExpanded,
                                decorationBox = { innerTextField ->
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text("Enter original code to find...", color = TextTertiary, fontSize = 12.sp)
                                    }
                                    innerTextField()
                                }
                            )

                            if (uiState.searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.searchMatchCount > 0)
                                        "${uiState.currentMatchIndex}/${uiState.searchMatchCount}"
                                    else "0 found",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (uiState.searchMatchCount > 0) StatusSuccess else StatusError
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { viewModel.prevSearchMatch() }, modifier = Modifier.size(22.dp)) {
                                    Icon(Icons.Default.ArrowUpward, "Previous match", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                }
                                IconButton(onClick = { viewModel.nextSearchMatch() }, modifier = Modifier.size(22.dp)) {
                                    Icon(Icons.Default.ArrowDownward, "Next match", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    // Replacement Code Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "REPLACE WITH OTHER CODE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBg)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = uiState.replaceQuery,
                                onValueChange = { viewModel.onReplaceQueryChange(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("replace_in_file_input"),
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                cursorBrush = SolidColor(PrimaryPurple),
                                maxLines = if (uiState.isMultilineExpanded) 6 else 1,
                                singleLine = !uiState.isMultilineExpanded,
                                decorationBox = { innerTextField ->
                                    if (uiState.replaceQuery.isEmpty()) {
                                        Text("Enter new replacement code...", color = TextTertiary, fontSize = 12.sp)
                                    }
                                    innerTextField()
                                }
                            )

                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { viewModel.replaceCurrent() },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkElevatedSurface),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("replace_one_button")
                            ) {
                                Text("Replace", fontSize = 11.sp, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = { viewModel.replaceAll() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("replace_all_button")
                            ) {
                                Text("All", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Status banner if present
        if (!uiState.statusMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryPurple.copy(alpha = 0.15f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = uiState.statusMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryPurple
                )
            }
        }

        // 5. Main Content: Edit Canvas vs Git Diff / Compare Mode
        if (uiState.isCompareMode) {
            GitDiffCanvas(
                diffSummary = uiState.diffSummary,
                fileName = activeTab?.fileName ?: "File",
                onClose = { viewModel.toggleCompareMode() },
                modifier = Modifier.weight(1f)
            )
        } else if (activeTab == null) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No file currently open. Open a file from Repositories.", color = TextSecondary)
            }
        } else if (FileUtils.isBinary(activeTab.fileName)) {
            BinaryAssetEditorPane(
                tab = activeTab,
                onRename = { showRenameDialog = true },
                onStage = { viewModel.stageActiveFile() },
                onCommit = { viewModel.showCommitDialog(true) },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        } else {
            // Edit Canvas
            val lines = remember(activeTab.content) {
                val count = activeTab.content.count { it == '\n' } + 1
                (1..count).toList()
            }
            val scrollState = rememberScrollState()
            val horizontalScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(DarkBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Line Numbers Gutter
                    if (uiState.showLineNumbers) {
                        Column(
                            modifier = Modifier
                                .background(DarkSurface)
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                .width(IntrinsicSize.Min),
                            horizontalAlignment = Alignment.End
                        ) {
                            lines.forEach { lineNum ->
                                Text(
                                    text = lineNum.toString(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = uiState.fontSizeSp.sp,
                                    lineHeight = (uiState.fontSizeSp * 1.5f).sp,
                                    color = TextTertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(280.dp))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(BorderSubtle)
                        )
                    }

                    // Text Field Column with scrolling headroom
                    val textModifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .testTag("code_editor_input")
                        .then(
                            if (!uiState.isWordWrap) Modifier.horizontalScroll(horizontalScrollState)
                            else Modifier
                        )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = activeTab.content,
                            onValueChange = { viewModel.onContentChange(it) },
                            modifier = textModifier,
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = uiState.fontSizeSp.sp,
                                lineHeight = (uiState.fontSizeSp * 1.5f).sp,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(SecondaryCyan),
                            visualTransformation = { text ->
                                val annotated = SyntaxHighlighter.highlight(
                                    code = text.text,
                                    language = activeTab.language,
                                    searchQuery = uiState.searchQuery
                                )
                                androidx.compose.ui.text.input.TransformedText(
                                    text = annotated,
                                    offsetMapping = androidx.compose.ui.text.input.OffsetMapping.Identity
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(280.dp))
                    }
                }
            }
        }

        // 6. VS Code-Style Bottom Status Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Git branch & dirty pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkElevatedSurface)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeTab?.branch ?: "main",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }

                    if (activeTab?.isDirty == true) {
                        Text(text = "● Unsaved edits", fontSize = 10.sp, color = StatusWarning)
                    } else {
                        Text(text = "Clean", fontSize = 10.sp, color = StatusSuccess)
                    }
                }

                // Right: Ln/Col, UTF-8, Language
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val lineCount = activeTab?.content?.count { it == '\n' }?.plus(1) ?: 1
                    Text(text = "Lines: $lineCount", fontSize = 10.sp, color = TextSecondary)
                    Text(text = "Spaces: 4", fontSize = 10.sp, color = TextSecondary)
                    Text(text = "UTF-8", fontSize = 10.sp, color = TextSecondary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PrimaryPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = (activeTab?.language ?: "kt").uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                }
            }
        }

        // Commit Changes Modal Dialog
        if (uiState.commitDialogVisible && activeTab != null) {
            CommitDialog(
                filePath = activeTab.filePath,
                branch = activeTab.branch,
                isCommitting = uiState.isCommitting,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.showCommitDialog(false) },
                onConfirm = { message, targetBranch ->
                    viewModel.commitChanges(message, targetBranch)
                }
            )
        }

        // Discard Changes Confirmation Dialog
        if (uiState.discardDialogVisible && activeTab != null) {
            AlertDialog(
                onDismissRequest = { viewModel.showDiscardDialog(false) },
                containerColor = DarkSurface,
                title = { Text("Discard Unsaved Changes?", color = TextPrimary) },
                text = {
                    Text(
                        "Are you sure you want to revert all changes made to ${activeTab.fileName}? Unsaved edits will be lost.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.discardChanges() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                    ) {
                        Text("Discard")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDiscardDialog(false) }) {
                        Text("Keep Editing", color = TextSecondary)
                    }
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
        if (uiState.batchCommitDialogVisible) {
            BatchCommitAndPushDialog(
                repoFullName = uiState.currentRepo,
                initialBranch = activeTab?.branch ?: "main",
                availableBranches = listOf(activeTab?.branch ?: "main", "main", "develop"),
                stagedChanges = uiState.stagedChanges,
                isCommitting = uiState.isBatchCommitting,
                onUnstage = { viewModel.unstageFile(it) },
                onClearAll = { viewModel.clearAllStaged() },
                onDismiss = { viewModel.showBatchCommitDialog(false) },
                onCommitAndPush = { title, desc, branch ->
                    viewModel.executeBatchCommitAndPush(title, desc, branch)
                }
            )
        }

        // Rename Active File Dialog
        if (showRenameDialog && activeTab != null) {
            RenameItemDialog(
                currentName = activeTab.fileName,
                currentPath = activeTab.filePath,
                isFolder = false,
                onDismiss = { showRenameDialog = false },
                onStageRename = { newName ->
                    viewModel.stageRenameActiveTab(newName)
                    showRenameDialog = false
                },
                onConfirmRename = { newName, commitMsg ->
                    viewModel.renameActiveTab(newName, commitMsg)
                    showRenameDialog = false
                }
            )
        }
    }
}

@Composable
private fun BinaryAssetEditorPane(
    tab: EditorTab,
    onRename: () -> Unit,
    onStage: () -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isImage = FileUtils.isImage(tab.fileName)
    val isDoc = FileUtils.isDocument(tab.fileName)
    val isMedia = FileUtils.isMedia(tab.fileName)
    val typeLabel = FileUtils.getFileTypeLabel(tab.fileName)

    val rawUrl = "https://raw.githubusercontent.com/${tab.repoFullName}/${tab.branch}/${tab.filePath}"
    val context = LocalContext.current
    val githubToken = remember { SecureStorage.getInstance(context).getGitHubToken() }
    val authenticatedImageRequest = remember(rawUrl, githubToken) {
        ImageRequest.Builder(context)
            .data(rawUrl)
            .apply {
                if (!githubToken.isNullOrBlank()) {
                    addHeader("Authorization", "Bearer $githubToken")
                }
            }
            .crossfade(true)
            .build()
    }

    // Decode bitmap from Base64 if available
    val bitmap = remember(tab.content) {
        if (isImage && tab.content.isNotBlank()) {
            try {
                val clean = tab.content.replace("\n", "").replace("\r", "")
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner: Format & Security info
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
                            .clip(CircleShape)
                            .background(
                                if (isImage) StatusWarning.copy(alpha = 0.2f)
                                else if (isDoc) StatusSuccess.copy(alpha = 0.2f)
                                else SecondaryCyan.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isImage -> Icons.Default.Image
                                tab.fileName.endsWith(".pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
                                tab.fileName.endsWith(".xlsx", ignoreCase = true) || tab.fileName.endsWith(".xls", ignoreCase = true) -> Icons.Default.TableChart
                                tab.fileName.endsWith(".zip", ignoreCase = true) || tab.fileName.endsWith(".tar", ignoreCase = true) -> Icons.Default.Archive
                                isDoc -> Icons.Default.Description
                                tab.fileName.endsWith(".mp4", ignoreCase = true) -> Icons.Default.Movie
                                isMedia -> Icons.Default.MusicNote
                                else -> Icons.Default.Description
                            },
                            contentDescription = null,
                            tint = if (isImage) StatusWarning else if (isDoc) StatusSuccess else SecondaryCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tab.fileName,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryCyan
                        )
                    }
                }

                // Quick Rename button
                Button(
                    onClick = onRename,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkElevatedSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("binary_pane_rename_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DriveFileRenameOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = SecondaryCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rename", fontSize = 11.sp, color = SecondaryCyan)
                }
            }
        }

        // Preview Area: Image rendering OR Document Display Card
        if (isImage) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = tab.fileName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = authenticatedImageRequest,
                            contentDescription = tab.fileName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        } else {
            // Document / Media / Binary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDoc) StatusSuccess.copy(alpha = 0.15f)
                                else SecondaryCyan.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                tab.fileName.endsWith(".pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
                                tab.fileName.endsWith(".xlsx", ignoreCase = true) || tab.fileName.endsWith(".xls", ignoreCase = true) -> Icons.Default.TableChart
                                tab.fileName.endsWith(".zip", ignoreCase = true) || tab.fileName.endsWith(".tar", ignoreCase = true) -> Icons.Default.Archive
                                isDoc -> Icons.Default.Description
                                tab.fileName.endsWith(".mp4", ignoreCase = true) -> Icons.Default.Movie
                                isMedia -> Icons.Default.MusicNote
                                else -> Icons.Default.Description
                            },
                            contentDescription = null,
                            tint = if (isDoc) StatusSuccess else SecondaryCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = tab.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Native Binary & Formatted Document",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryCyan
                    )

                    Text(
                        text = "This file is handled as clean binary data to protect document integrity. Editing as raw text is prevented to avoid corrupting document signatures or encoding.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // File Metadata Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkElevatedSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "FILE DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Repository", color = TextSecondary, fontSize = 12.sp)
                    Text(tab.repoFullName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Branch", color = TextSecondary, fontSize = 12.sp)
                    Text(tab.branch, color = SecondaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Path", color = TextSecondary, fontSize = 12.sp)
                    Text(tab.filePath, color = TextPrimary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                if (bitmap != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dimensions", color = TextSecondary, fontSize = 12.sp)
                        Text("${bitmap.width} × ${bitmap.height} px", color = StatusSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Action Buttons Row: Rename, Stage, Commit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onRename,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = SecondaryCyan)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Rename", color = SecondaryCyan)
            }

            Button(
                onClick = onStage,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.BookmarkAdded, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stage File")
            }
        }
    }
}

@Composable
private fun GitDiffCanvas(
    diffSummary: com.example.ui.editor.DiffSummary?,
    fileName: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Diff Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        tint = SecondaryCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Git Diff: $fileName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val add = diffSummary?.additions ?: 0
                    val del = diffSummary?.deletions ?: 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusSuccess.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("+$add lines", fontSize = 11.sp, color = StatusSuccess, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusError.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("-$del lines", fontSize = 11.sp, color = StatusError, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Diff", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Diff Lines List
        val lines = diffSummary?.lines ?: emptyList()
        if (lines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No changes detected between working tree and base version.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                items(lines) { diffLine ->
                    val (bg, textColor, symbol) = when (diffLine.type) {
                        DiffType.INSERT -> Triple(StatusSuccess.copy(alpha = 0.12f), StatusSuccess, "+")
                        DiffType.DELETE -> Triple(StatusError.copy(alpha = 0.12f), StatusError, "-")
                        DiffType.EQUAL -> Triple(Color.Transparent, TextPrimary, " ")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bg)
                            .padding(vertical = 1.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Old line num
                        Text(
                            text = diffLine.oldLineNumber?.toString() ?: "",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextTertiary,
                            modifier = Modifier.width(28.dp)
                        )
                        // New line num
                        Text(
                            text = diffLine.newLineNumber?.toString() ?: "",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextTertiary,
                            modifier = Modifier.width(28.dp)
                        )
                        // Symbol +/-
                        Text(
                            text = symbol,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.width(16.dp)
                        )
                        // Code text
                        Text(
                            text = diffLine.text,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommitDialog(
    filePath: String,
    branch: String,
    isCommitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var commitMessage by remember { mutableStateOf("Update ${filePath.substringAfterLast('/')}") }
    var targetBranch by remember { mutableStateOf(branch) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Commit Changes",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "File: $filePath",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = SecondaryCyan
                )

                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("commit_message_input"),
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

                OutlinedTextField(
                    value = targetBranch,
                    onValueChange = { targetBranch = it },
                    label = { Text("Target Branch") },
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
                    singleLine = true
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(text = errorMessage, color = StatusError, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(commitMessage.trim(), targetBranch.trim()) },
                enabled = commitMessage.isNotBlank() && !isCommitting,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                modifier = Modifier.testTag("confirm_commit_btn")
            ) {
                if (isCommitting) {
                    CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(16.dp))
                } else {
                    Text("Commit Changes")
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
