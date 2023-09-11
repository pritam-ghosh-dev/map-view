package com.example.map.data.local.room.webViewResponse

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ResponseCacheDao {
    @Query("SELECT * FROM web_response_cache")
    suspend fun getAllResponses(): List<ResponseCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponseCache(responseCache: ResponseCache)

    @Query("SELECT * FROM web_response_cache WHERE url = :url")
    suspend fun getResponseCacheByUrl(url: String): ResponseCache?

}