package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BackupRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupRecordDao {
    @Query("SELECT * FROM backup_records ORDER BY timestamp DESC")
    fun getAllBackups(): Flow<List<BackupRecordEntity>>

    @Query("SELECT * FROM backup_records WHERE repoFullName = :repoFullName ORDER BY timestamp DESC")
    fun getBackupsForRepo(repoFullName: String): Flow<List<BackupRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupRecordEntity): Long

    @Query("DELETE FROM backup_records WHERE id = :id")
    suspend fun deleteBackup(id: Long)

    @Query("DELETE FROM backup_records")
    suspend fun clearAll()
}
