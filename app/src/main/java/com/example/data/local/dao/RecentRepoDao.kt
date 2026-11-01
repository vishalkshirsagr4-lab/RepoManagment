package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.RecentRepoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentRepoDao {
    @Query("SELECT * FROM recent_repos ORDER BY lastAccessed DESC")
    fun getAllRecentRepos(): Flow<List<RecentRepoEntity>>

    @Query("SELECT * FROM recent_repos WHERE fullName = :fullName LIMIT 1")
    suspend fun getRepo(fullName: String): RecentRepoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepo(repo: RecentRepoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repos: List<RecentRepoEntity>)

    @Query("DELETE FROM recent_repos WHERE fullName = :fullName")
    suspend fun deleteRepo(fullName: String)

    @Query("DELETE FROM recent_repos")
    suspend fun clearAll()
}
