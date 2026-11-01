package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.models.DeploymentProvider
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

@Composable
fun LanguageBadge(language: String?, modifier: Modifier = Modifier) {
    if (language.isNullOrBlank()) return

    val color = when (language.lowercase()) {
        "kotlin" -> Color(0xFFA855F7)
        "python" -> Color(0xFF38BDF8)
        "typescript" -> Color(0xFF3B82F6)
        "javascript" -> Color(0xFFFBBF24)
        "java" -> Color(0xFFF97316)
        "hcl" -> Color(0xFF6366F1)
        "html" -> Color(0xFFE11D48)
        "css" -> Color(0xFF06B6D4)
        else -> SecondaryCyan
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkElevatedSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = language,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary
        )
    }
}

@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status.uppercase()) {
        "SUCCESS", "RUNNING" -> Triple(
            StatusSuccess.copy(alpha = 0.15f),
            StatusSuccess,
            Icons.Default.CheckCircle
        )
        "BUILDING", "PENDING", "QUEUED" -> Triple(
            StatusWarning.copy(alpha = 0.15f),
            StatusWarning,
            Icons.Default.Pending
        )
        "FAILED", "ERROR" -> Triple(
            StatusError.copy(alpha = 0.15f),
            StatusError,
            Icons.Default.Error
        )
        else -> Triple(
            SecondaryCyan.copy(alpha = 0.15f),
            SecondaryCyan,
            Icons.Default.PlayArrow
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun ProviderBadge(provider: DeploymentProvider, modifier: Modifier = Modifier) {
    val (label, icon, color) = when (provider) {
        DeploymentProvider.GITHUB_ACTIONS -> Triple("GitHub Actions", Icons.Default.Hub, PrimaryPurple)
        DeploymentProvider.CLOUDFLARE -> Triple("Cloudflare", Icons.Default.Cloud, Color(0xFFF97316))
        DeploymentProvider.HUGGING_FACE -> Triple("Hugging Face", Icons.Default.Layers, Color(0xFFFBBF24))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

fun getFileIcon(fileName: String): ImageVector {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "kt", "java", "py", "js", "ts", "json", "html", "css" -> Icons.Default.Code
        "" -> Icons.Default.Folder
        else -> Icons.Default.Code
    }
}
