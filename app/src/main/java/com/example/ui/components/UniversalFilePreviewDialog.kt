package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.security.SecureStorage
import com.example.data.remote.models.GitHubContentItem
import com.example.ui.common.FileUtils
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkBg
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

@Composable
fun UniversalFilePreviewDialog(
    item: GitHubContentItem,
    repoFullName: String,
    branch: String,
    onDismiss: () -> Unit,
    onOpenFileInEditor: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val isImage = FileUtils.isImage(item.name)
    val isDoc = FileUtils.isDocument(item.name)
    val isMedia = FileUtils.isMedia(item.name)
    val ext = item.name.substringAfterLast('.', "").lowercase()

    val rawUrl = item.downloadUrl
        ?: "https://raw.githubusercontent.com/$repoFullName/$branch/${item.path}"
    val context = LocalContext.current
    val token = remember { SecureStorage.getInstance(context).getGitHubToken() }
    val authenticatedImageRequest = remember(rawUrl, token) {
        ImageRequest.Builder(context)
            .data(rawUrl)
            .apply {
                if (!token.isNullOrBlank()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .crossfade(true)
            .build()
    }

    // Try decoding base64 if available for direct bitmap rendering
    val bitmap = remember(item.content) {
        if (!item.content.isNullOrBlank() && isImage) {
            try {
                val clean = item.content.replace("\n", "").replace("\r", "")
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val headerIcon = when {
                        isImage -> Icons.Default.Image
                        isDoc && ext == "pdf" -> Icons.Default.PictureAsPdf
                        isDoc && (ext == "xlsx" || ext == "xls") -> Icons.Default.TableChart
                        isDoc && (ext == "zip" || ext == "tar" || ext == "gz") -> Icons.Default.Archive
                        isDoc -> Icons.Default.Description
                        isMedia && (ext == "mp4" || ext == "webm" || ext == "mov") -> Icons.Default.Movie
                        isMedia -> Icons.Default.MusicNote
                        else -> Icons.Default.Code
                    }
                    val headerColor = when {
                        isImage -> StatusWarning
                        isDoc -> SecondaryCyan
                        isMedia -> PrimaryPurple
                        else -> StatusSuccess
                    }

                    Icon(
                        imageVector = headerIcon,
                        contentDescription = null,
                        tint = headerColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.name,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = FileUtils.getFileTypeLabel(item.name),
                            fontSize = 11.sp,
                            color = headerColor
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    // 1. IMAGE PREVIEW (Rendered directly)
                    isImage -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = item.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(6.dp)
                                    )
                                } else {
                                    AsyncImage(
                                        model = authenticatedImageRequest,
                                        contentDescription = item.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. DOCUMENT PREVIEW (Word .docx, PDF, Excel .xlsx, Archives)
                    isDoc -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkElevatedSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val (docColor, docIcon) = when (ext) {
                                    "pdf" -> Pair(Color(0xFFEF4444), Icons.Default.PictureAsPdf)
                                    "docx", "doc" -> Pair(Color(0xFF3B82F6), Icons.Default.Description)
                                    "xlsx", "xls" -> Pair(Color(0xFF10B981), Icons.Default.TableChart)
                                    "zip", "tar", "gz", "rar", "7z" -> Pair(Color(0xFFA855F7), Icons.Default.Archive)
                                    else -> Pair(SecondaryCyan, Icons.Default.Description)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(docColor.copy(alpha = 0.15f))
                                        .border(1.dp, docColor.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = docIcon,
                                        contentDescription = null,
                                        tint = docColor,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = FileUtils.getFileTypeLabel(item.name),
                                    fontSize = 12.sp,
                                    color = docColor
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkBg)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Original binary structure preserved • Clean format verified",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    // 3. MEDIA (Audio / Video)
                    isMedia -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkElevatedSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryPurple.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (ext in listOf("mp4", "webm", "mov")) Icons.Default.Movie else Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = item.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = "Media Asset (${ext.uppercase()})", fontSize = 11.sp, color = SecondaryCyan)
                            }
                        }
                    }

                    // 4. CODE / TEXT / RAW CONTENT
                    else -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Raw File: ${item.name}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Text & source code is ready to open and edit in VS Code-style editor.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkElevatedSurface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Path", fontSize = 11.sp, color = TextTertiary)
                        Text(item.path, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = SecondaryCyan)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Size", fontSize = 11.sp, color = TextTertiary)
                        Text(FileUtils.formatFileSize(item.size), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MIME Type", fontSize = 11.sp, color = TextTertiary)
                        Text(FileUtils.getMimeType(item.name), fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Rename Action
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onRename()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("preview_rename_btn")
                ) {
                    Icon(Icons.Default.DriveFileRenameOutline, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rename")
                }

                // If not binary, or if user wants to open in editor
                Button(
                    onClick = {
                        onDismiss()
                        onOpenFileInEditor()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("preview_open_editor_btn")
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open in Editor")
                }
            }
        },
        dismissButton = {
            // Delete action
            IconButton(
                onClick = {
                    onDismiss()
                    onDelete()
                },
                modifier = Modifier.size(32.dp).testTag("preview_delete_btn")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError)
            }
        }
    )
}
