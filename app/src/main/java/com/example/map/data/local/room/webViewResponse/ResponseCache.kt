package com.example.map.data.local.room.webViewResponse

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web_response_cache")
data class ResponseCache (
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "response") val response: String?,
    @ColumnInfo(name = "filePath") val filePath: String?,
    @ColumnInfo(name = "mimeType") val mimeType: String?,
    @ColumnInfo(name = "encoding") val encoding: String?,
    )