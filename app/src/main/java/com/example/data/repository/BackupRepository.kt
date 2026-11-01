package com.example.data.repository

import android.content.Context
import android.os.Environment
import com.example.data.demo.DemoData
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BackupRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupRepository(
    private val context: Context,
    private val repoRepository: RepoRepository,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val backupDao = database.backupRecordDao()

    fun getAllBackups(): Flow<List<BackupRecordEntity>> = backupDao.getAllBackups()

    suspend fun createRepositoryBackup(
        repoFullName: String,
        branch: String,
        onProgress: (Float) -> Unit = {}
    ): Result<BackupRecordEntity> = withContext(Dispatchers.IO) {
        val parts = repoFullName.split("/")
        val owner = parts.getOrNull(0) ?: "repo"
        val repo = parts.getOrNull(1) ?: "repo"
        val timestamp = System.currentTimeMillis()
        val sanitizedBranch = branch.replace("/", "_")
        val fileName = "${repo}-${sanitizedBranch}-${timestamp}.zip"

        val destinationDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        if (!destinationDir.exists()) destinationDir.mkdirs()
        val destinationFile = File(destinationDir, fileName)

        try {
            onProgress(0.1f)
            if (repoRepository.isDemoMode()) {
                // Generate a real valid ZIP file with sample repository contents
                onProgress(0.3f)
                val fos = FileOutputStream(destinationFile)
                val zos = ZipOutputStream(fos)

                val filesToZip = mapOf(
                    "README.md" to DemoData.getDemoFileCode("README.md"),
                    "build.gradle.kts" to DemoData.getDemoFileCode("build.gradle.kts"),
                    "settings.json" to DemoData.getDemoFileCode("settings.json"),
                    "src/main/kotlin/RepositoryManager.kt" to DemoData.getDemoFileCode("src/main/kotlin/RepositoryManager.kt"),
                    "worker.ts" to DemoData.getDemoFileCode("worker.ts"),
                    "app.py" to DemoData.getDemoFileCode("app.py")
                )

                var count = 0
                for ((entryPath, text) in filesToZip) {
                    val entry = ZipEntry(entryPath)
                    zos.putNextEntry(entry)
                    zos.write(text.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                    count++
                    onProgress(0.3f + (0.6f * (count.toFloat() / filesToZip.size)))
                }
                zos.close()
                fos.close()
            } else {
                // Fetch real zipball stream from GitHub API
                onProgress(0.3f)
                val streamResult = repoRepository.downloadZipStream(owner, repo, branch)
                if (streamResult.isFailure) {
                    val errorMsg = streamResult.exceptionOrNull()?.message ?: "Failed to download zip stream"
                    val failedRecord = BackupRecordEntity(
                        repoFullName = repoFullName,
                        branch = branch,
                        fileName = fileName,
                        filePath = destinationFile.absolutePath,
                        fileSizeBytes = 0,
                        status = "FAILED",
                        error = errorMsg
                    )
                    backupDao.insertBackup(failedRecord)
                    return@withContext Result.failure(Exception(errorMsg))
                }

                val inputStream = streamResult.getOrThrow()
                val outputStream = FileOutputStream(destinationFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytes: Long = 0

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()
            }

            onProgress(1.0f)
            val fileSize = destinationFile.length()
            val record = BackupRecordEntity(
                repoFullName = repoFullName,
                branch = branch,
                fileName = fileName,
                filePath = destinationFile.absolutePath,
                fileSizeBytes = fileSize,
                timestamp = timestamp,
                status = "SUCCESS",
                error = null
            )
            val recordId = backupDao.insertBackup(record)
            Result.success(record.copy(id = recordId))
        } catch (e: Exception) {
            val failedRecord = BackupRecordEntity(
                repoFullName = repoFullName,
                branch = branch,
                fileName = fileName,
                filePath = destinationFile.absolutePath,
                fileSizeBytes = destinationFile.length(),
                timestamp = timestamp,
                status = "FAILED",
                error = e.message
            )
            backupDao.insertBackup(failedRecord)
            Result.failure(e)
        }
    }

    suspend fun deleteBackup(backup: BackupRecordEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(backup.filePath)
            if (file.exists()) file.delete()
            backupDao.deleteBackup(backup.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
