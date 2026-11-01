package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BackupRecordDao
import com.example.data.local.dao.RecentFileDao
import com.example.data.local.dao.RecentRepoDao
import com.example.data.local.entity.BackupRecordEntity
import com.example.data.local.entity.RecentFileEntity
import com.example.data.local.entity.RecentRepoEntity

@Database(
    entities = [
        RecentRepoEntity::class,
        BackupRecordEntity::class,
        RecentFileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentRepoDao(): RecentRepoDao
    abstract fun backupRecordDao(): BackupRecordDao
    abstract fun recentFileDao(): RecentFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "repo_manager_pro.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
