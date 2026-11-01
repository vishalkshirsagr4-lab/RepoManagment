package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey
    val fileKey: String, // repoFullName:branch:filePath
    val repoFullName: String,
    val branch: String,
    val filePath: String,
    val fileName: String,
    val language: String,
    val lastModified: Long = System.currentTimeMillis(),
    val isDirty: Boolean = false
)
