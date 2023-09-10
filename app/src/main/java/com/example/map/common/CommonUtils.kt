package com.example.map.common

import android.content.Context
import java.io.IOException
import java.io.InputStream

object CommonUtils {
    fun loadJSONFromAsset(filename: String, context: Context): String {
        val json: String
        try {
            val inputStream: InputStream = context.assets.open(filename)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ""
        }
        return json
    }
}