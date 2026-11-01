package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Base64
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

/**
 * Streams audio/video and renders real PDF pages or text extracted from DOCX/XLSX.
 * Legacy binary .doc/.xls files are identified as unsupported rather than shown as
 * if they were rendered successfully.
 */
@Composable
fun RichFilePreview(
    fileName: String,
    sourceUrl: String,
    token: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extension = fileName.substringAfterLast('.', "").lowercase()
    val isVideo = extension in setOf("mp4", "m4v", "webm", "mkv", "mov", "3gp", "avi")
    val isAudio = extension in setOf("mp3", "m4a", "aac", "wav", "ogg", "flac", "opus")
    if (isVideo || isAudio) {
        MediaFilePreview(sourceUrl, token, isVideo, modifier)
        return
    }

    var loading by remember(sourceUrl) { mutableStateOf(true) }
    var error by remember(sourceUrl) { mutableStateOf<String?>(null) }
    var downloadedFile by remember(sourceUrl) { mutableStateOf<File?>(null) }
    var extractedText by remember(sourceUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(sourceUrl, token, extension) {
        loading = true
        error = null
        downloadedFile = null
        extractedText = null
        try {
            val file = withContext(Dispatchers.IO) {
                val client = OkHttpClient.Builder()
                    .callTimeout(45, TimeUnit.SECONDS)
                    .build()
                val requestBuilder = Request.Builder().url(sourceUrl).get()
                if (!token.isNullOrBlank()) requestBuilder.header("Authorization", "Bearer $token")
                client.newCall(requestBuilder.build()).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("Download failed: HTTP ${response.code}")
                    }
                    val body = response.body ?: throw IllegalStateException("GitHub returned an empty file.")
                    if (body.contentLength() > 30L * 1024L * 1024L) {
                        throw IllegalStateException("Preview is limited to files smaller than 30 MB.")
                    }
                    val bytes = body.bytes()
                    if (bytes.size > 30 * 1024 * 1024) {
                        throw IllegalStateException("Preview is limited to files smaller than 30 MB.")
                    }
                    File(context.cacheDir, "preview_${sourceUrl.hashCode().toUInt().toString(16)}.${extension}")
                        .apply { writeBytes(bytes) }
                }
            }
            downloadedFile = file
            if (extension in setOf("docx", "xlsx", "pptx", "odt", "ods", "odp", "rtf")) {
                extractedText = withContext(Dispatchers.IO) { extractOfficeText(file, extension) }
            } else if (extension != "pdf") {
                error = when (extension) {
                    "doc", "xls", "ppt" -> "Legacy binary Office files are not supported yet. Save this file as DOCX, XLSX, or PPTX to preview its contents."
                    else -> "This document format does not have an in-app renderer yet."
                }
            }
        } catch (e: Exception) {
            error = e.message ?: "Unable to load this file."
        } finally {
            loading = false
        }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkElevatedSurface)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (extension) {
                    "pdf" -> androidx.compose.material3.Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    "xlsx" -> androidx.compose.material3.Icon(Icons.Default.TableChart, contentDescription = null)
                    else -> androidx.compose.material3.Icon(Icons.Default.Description, contentDescription = null)
                }
                Text(fileName, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
            }
            when {
                loading -> Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Text(error!!, color = StatusError, style = MaterialTheme.typography.bodyMedium)
                extension == "pdf" && downloadedFile != null -> PdfPagePreview(downloadedFile!!)
                (extension == "docx" || extension == "xlsx") && !extractedText.isNullOrBlank() ->
                    Text(
                        extractedText!!,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp)
                            .verticalScroll(rememberScrollState())
                    )
                else -> Text("No readable document content was found.", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun MediaFilePreview(
    sourceUrl: String,
    token: String?,
    isVideo: Boolean,
    modifier: Modifier
) {
    val context = LocalContext.current
    var playerError by remember(sourceUrl) { mutableStateOf<String?>(null) }
    val exoPlayer = remember(sourceUrl, token) {
        val httpFactory = DefaultHttpDataSource.Factory().apply {
            if (!token.isNullOrBlank()) {
                setDefaultRequestProperties(mapOf("Authorization" to "Bearer $token"))
            }
        }
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        playerError = error.message ?: "The media format could not be played."
                    }
                })
                setMediaItem(MediaItem.fromUri(sourceUrl))
                prepare()
            }
    }
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isVideo) 230.dp else 88.dp),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = true
                    player = exoPlayer
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            update = { it.player = exoPlayer }
        )
        if (playerError != null) {
            Text(playerError!!, color = StatusError, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PdfPagePreview(file: File) {
    var pageIndex by remember(file) { mutableIntStateOf(0) }
    var pageCount by remember(file) { mutableIntStateOf(0) }
    var pageBitmap by remember(file, pageIndex) { mutableStateOf<Bitmap?>(null) }
    var pageError by remember(file, pageIndex) { mutableStateOf<String?>(null) }
    var loading by remember(file, pageIndex) { mutableStateOf(true) }

    LaunchedEffect(file, pageIndex) {
        loading = true
        pageError = null
        pageBitmap = null
        try {
            val result = withContext(Dispatchers.IO) {
                val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                PdfRenderer(descriptor).use { renderer ->
                    if (pageIndex !in 0 until renderer.pageCount) {
                        throw IllegalArgumentException("PDF page is unavailable.")
                    }
                    val page = renderer.openPage(pageIndex)
                    val scale = minOf(2.0f, 1400f / page.width.coerceAtLeast(1))
                    val bitmap = Bitmap.createBitmap(
                        (page.width * scale).toInt().coerceAtLeast(1),
                        (page.height * scale).toInt().coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888
                    )
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    Pair(renderer.pageCount, bitmap)
                }.also { descriptor.close() }
            }
            pageCount = result.first
            pageBitmap = result.second
        } catch (e: Exception) {
            pageError = e.message ?: "Unable to render PDF page."
        } finally {
            loading = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            loading -> Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            pageError != null -> Text(pageError!!, color = StatusError)
            pageBitmap != null -> Image(
                bitmap = pageBitmap!!.asImageBitmap(),
                contentDescription = "PDF page ${pageIndex + 1}",
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp)
            )
        }
        if (pageCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { pageIndex = (pageIndex - 1).coerceAtLeast(0) }, enabled = pageIndex > 0) {
                    androidx.compose.material3.Icon(Icons.Default.NavigateBefore, contentDescription = "Previous page")
                    Text("Previous")
                }
                Text("Page ${pageIndex + 1} of $pageCount", color = TextSecondary)
                Button(
                    onClick = { pageIndex = (pageIndex + 1).coerceAtMost(pageCount - 1) },
                    enabled = pageIndex < pageCount - 1
                ) {
                    Text("Next")
                    androidx.compose.material3.Icon(Icons.Default.NavigateNext, contentDescription = "Next page")
                }
            }
        }
    }
}

private fun extractOfficeText(file: File, extension: String): String {
    if (extension == "rtf") {
        val source = file.readText(Charsets.UTF_8)
        return source
            .replace(Regex("""\\\\'[0-9a-fA-F]{2}"""), " ")
            .replace(Regex("""\\\\par[d]?"""), "\\n")
            .replace(Regex("""\\\\[a-zA-Z]+-?\\d* ?"""), " ")
            .replace(Regex("[{}]"), "")
            .replace(Regex("[ \\t]+"), " ")
            .trim()
            .ifBlank { "The RTF document contains no readable text." }
    }

    val entries = mutableMapOf<String, String>()
    ZipInputStream(file.inputStream().buffered()).use { zip ->
        while (true) {
            val entry = zip.nextEntry ?: break
            if (!entry.isDirectory && (
                entry.name == "word/document.xml" ||
                entry.name == "xl/sharedStrings.xml" ||
                entry.name.startsWith("xl/worksheets/sheet") && entry.name.endsWith(".xml") ||
                entry.name.startsWith("ppt/slides/slide") && entry.name.endsWith(".xml") ||
                entry.name == "content.xml"
            )) {
                entries[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
            zip.closeEntry()
        }
    }

    fun xmlText(xml: String): String = xml
        .replace(Regex("(?i)</w:p>"), "\n")
        .replace(Regex("(?s)<[^>]+>"), " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace(Regex("[ \\t]+"), " ")
        .trim()

    if (extension == "docx") {
        val xml = entries["word/document.xml"]
            ?: throw IllegalStateException("The DOCX file has no word/document.xml content.")
        return xmlText(xml).ifBlank { "The document contains no readable text." }
    }

    if (extension == "pptx") {
        val slides = entries.filterKeys {
            it.startsWith("ppt/slides/slide") && it.endsWith(".xml")
        }.toSortedMap()
        if (slides.isEmpty()) throw IllegalStateException("The PPTX file contains no readable slides.")
        return slides.entries.joinToString("\\n\\n") { (name, xml) ->
            "${name.substringAfterLast('/').removeSuffix(".xml")}:\\n" + xmlText(xml)
        }
    }

    if (extension in setOf("odt", "ods", "odp")) {
        val xml = entries["content.xml"]
            ?: throw IllegalStateException("The OpenDocument file has no content.xml entry.")
        return xmlText(xml).ifBlank { "The document contains no readable text." }
    }

    val sharedStrings = entries["xl/sharedStrings.xml"]?.let { xml ->
        Regex("(?s)<si[^>]*>(.*?)</si>").findAll(xml).map { xmlText(it.groupValues[1]) }.toList()
    } ?: emptyList()
    val sheets = entries.filterKeys { it.startsWith("xl/worksheets/sheet") }.toSortedMap()
    if (sheets.isEmpty()) throw IllegalStateException("The XLSX workbook has no readable worksheet.")
    return sheets.entries.joinToString("\n\n") { (sheetName, xml) ->
        val rows = Regex("(?s)<row[^>]*>(.*?)</row>").findAll(xml).map { row ->
            Regex("(?s)<c([^>]*)>(.*?)</c>").findAll(row.groupValues[1]).map { cell ->
                val attrs = cell.groupValues[1]
                val body = cell.groupValues[2]
                val value = Regex("<v[^>]*>(.*?)</v>").find(body)?.groupValues?.get(1)
                val type = Regex("t=\\\"([^\\\"]+)\\\"").find(attrs)?.groupValues?.get(1)
                when {
                    type == "s" && value != null -> sharedStrings.getOrNull(value.toIntOrNull() ?: -1) ?: ""
                    type == "inlineStr" -> xmlText(body)
                    value != null -> value
                    else -> ""
                }
            }.joinToString(" | ")
        }.toList()
        "${sheetName.substringAfterLast('/')}:\n" + rows.joinToString("\n")
    }.ifBlank { "The workbook contains no readable cell values." }
}
