package com.example.ui.common

import java.util.Locale

object FileUtils {
    private val IMAGE_EXTENSIONS = setOf(
        "png", "jpg", "jpeg", "gif", "webp", "svg", "bmp", "ico", "tiff", "heic"
    )

    private val DOCUMENT_EXTENSIONS = setOf(
        "pdf", "docx", "doc", "xlsx", "xls", "pptx", "ppt", "odt", "ods", "odp",
        "rtf", "zip", "tar", "gz", "rar", "7z", "apk", "jar", "bin", "exe", "dmg"
    )

    private val MEDIA_EXTENSIONS = setOf(
        "mp4", "webm", "mov", "m4v", "mkv", "avi", "mp3", "wav", "m4a", "ogg", "flac", "aac"
    )

    fun isImage(fileName: String): Boolean {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return ext in IMAGE_EXTENSIONS
    }

    fun isDocument(fileName: String): Boolean {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return ext in DOCUMENT_EXTENSIONS
    }

    fun isMedia(fileName: String): Boolean {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return ext in MEDIA_EXTENSIONS
    }

    fun isBinary(fileName: String): Boolean {
        return isImage(fileName) || isDocument(fileName) || isMedia(fileName)
    }

    fun getFileTypeLabel(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return when (ext) {
            "png" -> "PNG Image"
            "jpg", "jpeg" -> "JPEG Image"
            "gif" -> "GIF Animation"
            "webp" -> "WebP Image"
            "svg" -> "SVG Vector Graphic"
            "pdf" -> "PDF Document"
            "docx", "doc" -> "Word Document"
            "xlsx", "xls" -> "Excel Spreadsheet"
            "pptx", "ppt" -> "PowerPoint Presentation"
            "zip", "tar", "gz", "rar", "7z" -> "Compressed Archive"
            "apk" -> "Android Application Package"
            "mp4", "webm", "mov" -> "Video Stream"
            "mp3", "wav", "ogg", "m4a" -> "Audio Recording"
            "kt" -> "Kotlin Source"
            "java" -> "Java Source"
            "py" -> "Python Script"
            "js" -> "JavaScript File"
            "ts" -> "TypeScript File"
            "json" -> "JSON Data"
            "md" -> "Markdown Document"
            "html" -> "HTML Page"
            "css" -> "CSS Stylesheet"
            "yaml", "yml" -> "YAML Configuration"
            "xml" -> "XML Document"
            "sql" -> "SQL Query"
            "sh", "bash" -> "Shell Script"
            "" -> "Directory / Binary File"
            else -> "${ext.uppercase(Locale.ROOT)} File"
        }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes <= 0 -> "0 B"
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }

    fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return when (ext) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "pdf" -> "application/pdf"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "doc" -> "application/msword"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "zip" -> "application/zip"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "json" -> "application/json"
            "md" -> "text/markdown"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}
