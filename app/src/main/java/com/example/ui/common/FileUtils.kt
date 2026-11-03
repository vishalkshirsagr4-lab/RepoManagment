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

    /**
     * Detect common binary formats from GitHub's Base64 content.
     * This also handles older uploads whose filename has no extension
     * (for example Android providers that supplied "image:12345").
     */
    fun isImageBase64(content: String?): Boolean {
        val bytes = decodeBase64Safely(content) ?: return false
        if (bytes.size < 4) return false
        val knownImageSignature = isPng(bytes) ||
            isJpeg(bytes) ||
            isGif(bytes) ||
            isWebp(bytes) ||
            (bytes[0] == 0x42.toByte() && bytes[1] == 0x4D.toByte()) ||
            (bytes[0] == 0x00.toByte() && bytes[1] == 0x00.toByte() &&
                bytes[2] == 0x01.toByte() && bytes[3] == 0x00.toByte())
        if (knownImageSignature) return true

        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        return options.outWidth > 0 && options.outHeight > 0
    }

    fun isBinaryBase64(content: String?): Boolean {
        val bytes = decodeBase64Safely(content) ?: return false
        if (bytes.isEmpty()) return false
        if (isImageBase64(content)) return true

        val signatures = listOf(
            byteArrayOf(0x25, 0x50, 0x44, 0x46), // PDF
            byteArrayOf(0x50, 0x4B, 0x03, 0x04), // ZIP / Office
            byteArrayOf(0x52, 0x61, 0x72, 0x21), // RAR
            byteArrayOf(0x1F, 0x8B.toByte()), // GZIP
            byteArrayOf(0x37, 0x7A, 0xBC.toByte(), 0xAF.toByte(), 0x27, 0x1C), // 7z
            byteArrayOf(0x49, 0x44, 0x33), // MP3 ID3
            byteArrayOf(0x4F, 0x67, 0x67, 0x53) // OGG
        )
        if (signatures.any { sig -> bytes.size >= sig.size && sig.indices.all { bytes[it] == sig[it] } }) {
            return true
        }
        if (bytes.size >= 12 && String(bytes.copyOfRange(4, 8), Charsets.US_ASCII) == "ftyp") {
            return true // MP4 / HEIF family
        }

        // NUL bytes are not valid in ordinary source/text files.
        return bytes.take(minOf(bytes.size, 8192)).any { it == 0.toByte() }
    }

    private fun decodeBase64Safely(content: String?): ByteArray? {
        if (content.isNullOrBlank()) return null
        return try {
            val clean = content.replace("\n", "").replace("\r", "").trim()
            android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun isPng(b: ByteArray) =
        b.size >= 8 && b[0] == 0x89.toByte() && b[1] == 0x50.toByte() &&
            b[2] == 0x4E.toByte() && b[3] == 0x47.toByte()

    private fun isJpeg(b: ByteArray) =
        b.size >= 3 && b[0] == 0xFF.toByte() && b[1] == 0xD8.toByte() && b[2] == 0xFF.toByte()

    private fun isGif(b: ByteArray) =
        b.size >= 6 && String(b.copyOfRange(0, 3), Charsets.US_ASCII) == "GIF"

    private fun isWebp(b: ByteArray) =
        b.size >= 12 && String(b.copyOfRange(0, 4), Charsets.US_ASCII) == "RIFF" &&
            String(b.copyOfRange(8, 12), Charsets.US_ASCII) == "WEBP"

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
