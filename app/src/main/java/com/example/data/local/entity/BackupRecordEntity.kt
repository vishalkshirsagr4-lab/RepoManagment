package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_records")
data class BackupRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val repoFullName: String,
    val branch: String,
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "SUCCESS", "FAILED"
    val error: String? = null
)
