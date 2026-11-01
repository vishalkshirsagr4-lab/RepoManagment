package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.RecentFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY lastModified DESC LIMIT 20")
    fun getRecentFiles(): Flow<List<RecentFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecentFile(file: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE fileKey = :fileKey")
    suspend fun deleteRecentFile(fileKey: String)

    @Query("DELETE FROM recent_files")
    suspend fun clearAll()
}
