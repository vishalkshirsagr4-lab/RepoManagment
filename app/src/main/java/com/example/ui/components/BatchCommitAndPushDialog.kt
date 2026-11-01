package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.models.StagedChange
import com.example.data.remote.models.StagedChangeType
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

@Composable
fun BatchCommitAndPushDialog(
    repoFullName: String,
    initialBranch: String = "main",
    availableBranches: List<String> = listOf("main", "develop", "feature"),
    stagedChanges: List<StagedChange>,
    isCommitting: Boolean,
    onUnstage: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
    onCommitAndPush: (commitTitle: String, commitDescription: String?, branch: String) -> Unit
) {
    var commitTitle by remember { mutableStateOf("") }
    var commitDescription by remember { mutableStateOf("") }
    var selectedBranch by remember { mutableStateOf(initialBranch) }
    var branchMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isCommitting) onDismiss() },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "One-Time Commit & Push",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Atomic single commit for all staged files",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                if (!isCommitting) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Repo and Branch Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkElevatedSurface)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Repository",
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                        Text(
                            text = repoFullName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Branch dropdown
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                .clickableWithFeedback { branchMenuOpen = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedBranch,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryCyan
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = SecondaryCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = branchMenuOpen,
                            onDismissRequest = { branchMenuOpen = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            val branches = if (availableBranches.isEmpty()) listOf("main") else availableBranches
                            branches.forEach { br ->
                                DropdownMenuItem(
                                    text = { Text(br, color = TextPrimary, fontSize = 12.sp) },
                                    trailingIcon = {
                                        if (br == selectedBranch) {
                                            Icon(Icons.Default.Check, null, tint = PrimaryPurple)
                                        }
                                    },
                                    onClick = {
                                        selectedBranch = br
                                        branchMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Staged files list section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Staged Changes (${stagedChanges.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (stagedChanges.isNotEmpty() && !isCommitting) {
                        TextButton(
                            onClick = onClearAll,
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.ClearAll, null, modifier = Modifier.size(14.dp), tint = StatusError)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear all", fontSize = 11.sp, color = StatusError)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (stagedChanges.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkElevatedSurface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No files staged. Stage files from editor or repo first.",
                            fontSize = 12.sp,
                            color = TextTertiary
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkElevatedSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                            items(stagedChanges) { item ->
                                StagedChangeRow(
                                    item = item,
                                    canRemove = !isCommitting,
                                    onRemove = { onUnstage(item.filePath) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Commit Name / Title TextField
                OutlinedTextField(
                    value = commitTitle,
                    onValueChange = { commitTitle = it },
                    label = { Text("Commit Name / Summary *") },
                    placeholder = { Text("e.g. feat: implement multi-file logic and assets") },
                    singleLine = true,
                    enabled = !isCommitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("batch_commit_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedLabelColor = PrimaryPurple,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Commit Description (Optional)
                OutlinedTextField(
                    value = commitDescription,
                    onValueChange = { commitDescription = it },
                    label = { Text("Extended Description (Optional)") },
                    placeholder = { Text("Add detail, ticket refs, or notes...") },
                    minLines = 2,
                    maxLines = 3,
                    enabled = !isCommitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("batch_commit_desc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderSubtle,
                        focusedLabelColor = PrimaryPurple,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = DarkElevatedSurface,
                        unfocusedContainerColor = DarkElevatedSurface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCommitAndPush(commitTitle, commitDescription.takeIf { it.isNotBlank() }, selectedBranch)
                },
                enabled = stagedChanges.isNotEmpty() && !isCommitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    disabledContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("batch_commit_confirm_btn")
            ) {
                if (isCommitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pushing to GitHub...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Commit & Push (${stagedChanges.size} ${if (stagedChanges.size == 1) "file" else "files"})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            if (!isCommitting) {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    )
}

@Composable
private fun StagedChangeRow(
    item: StagedChange,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    val (typeColor, typeText, icon) = when (item.type) {
        StagedChangeType.MODIFIED -> Triple(SecondaryCyan, "MODIFIED", Icons.Default.Edit)
        StagedChangeType.CREATED -> Triple(StatusSuccess, "NEW FILE", Icons.Default.Add)
        StagedChangeType.UPLOADED -> Triple(PrimaryPurple, "ASSET", Icons.Default.UploadFile)
        StagedChangeType.DELETED -> Triple(StatusError, "DELETED", Icons.Default.Delete)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(typeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = typeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = item.filePath,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.fileSizeFormatted.isNotBlank()) {
                    Text(
                        text = item.fileSizeFormatted,
                        fontSize = 9.sp,
                        color = TextTertiary
                    )
                }
            }
        }

        if (canRemove) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Unstage",
                    tint = TextTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// Small extension for clickable helper
private fun Modifier.clickableWithFeedback(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)
