package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_repos")
data class RecentRepoEntity(
    @PrimaryKey
    val fullName: String,
    val name: String,
    val owner: String,
    val description: String?,
    val language: String?,
    val stars: Int,
    val forks: Int,
    val isPrivate: Boolean,
    val defaultBranch: String,
    val updatedAt: String,
    val lastAccessed: Long = System.currentTimeMillis()
)
