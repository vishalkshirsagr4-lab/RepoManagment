package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RenameItemDialog(
    currentName: String,
    currentPath: String,
    isFolder: Boolean,
    onDismiss: () -> Unit,
    onStageRename: (newName: String) -> Unit,
    onConfirmRename: (newName: String, commitMessage: String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var commitMessage by remember {
        mutableStateOf("Rename $currentName via Repo Manager Pro")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DriveFileRenameOutline,
                    contentDescription = null,
                    tint = SecondaryCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFolder) "Rename Folder" else "Rename File / Asset",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Current Path: $currentPath",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = SecondaryCyan
                )

                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        if (commitMessage.startsWith("Rename ")) {
                            commitMessage = "Rename $currentName to $it via Repo Manager Pro"
                        }
                    },
                    label = { Text("New Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("rename_input"),
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
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    label = { Text("Commit Message") },
                    modifier = Modifier.fillMaxWidth().testTag("rename_commit_msg"),
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
                        val trimmed = newName.trim()
                        if (trimmed.isNotBlank() && trimmed != currentName) {
                            onStageRename(trimmed)
                        }
                    },
                    enabled = newName.isNotBlank() && newName.trim() != currentName,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("stage_rename_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stage (Batch)")
                }

                Button(
                    onClick = {
                        val trimmed = newName.trim()
                        if (trimmed.isNotBlank() && trimmed != currentName) {
                            onConfirmRename(trimmed, commitMessage.ifBlank { "Rename $currentName to $trimmed" })
                        }
                    },
                    enabled = newName.isNotBlank() && newName.trim() != currentName,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_rename_btn")
                ) {
                    Text("Rename Now")
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
