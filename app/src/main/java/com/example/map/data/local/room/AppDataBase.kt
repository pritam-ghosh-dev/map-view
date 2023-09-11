package com.example.map.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.map.data.local.room.webViewResponse.ResponseCache
import com.example.map.data.local.room.webViewResponse.ResponseCacheDao

@Database(entities = [ResponseCache::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun responseCacheDao(): ResponseCacheDao

    companion object {
        // Singleton instance of the database
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(applicationContext: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create or retrieve the database instance
                val instance = Room.databaseBuilder(
                    applicationContext, AppDatabase::class.java, "map_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
