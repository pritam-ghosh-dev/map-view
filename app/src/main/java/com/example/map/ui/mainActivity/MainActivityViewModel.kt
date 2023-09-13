package com.example.map.ui.mainActivity

import android.webkit.WebResourceRequest
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.map.MainApplication
import com.example.map.common.Constants
import com.example.map.data.local.room.AppDatabase
import com.example.map.data.local.room.webViewResponse.ResponseCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivityViewModel : ViewModel() {
    private val database = AppDatabase.getInstance(MainApplication.appContext)

    suspend fun getCachedResponse(url: String): ResponseCache? {
        return withContext(Dispatchers.IO) {
            database.responseCacheDao().getResponseCacheByUrl(url)
        }
    }

    suspend fun insertResponseCache(cache: ResponseCache) {
        withContext(Dispatchers.IO) {
            database.responseCacheDao().insertResponseCache(cache)
        }
    }

    // function to call an API and return the JSON response Stringified
    suspend fun getConvertJsonToString(url: String, request: WebResourceRequest?): String {
        return withContext(Dispatchers.IO) {
            val urlObj = URL(url)
            val connection = urlObj.openConnection() as HttpURLConnection
            connection.useCaches = false
            connection.requestMethod = "POST"
            request?.let { connection.requestMethod = it.method }
            val inputStream = connection.inputStream

            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            sb.toString()
        }
    }

    // function to get image file with url provided,
    // storing them in local cache directory and return object of ResponseCache

    suspend fun getImageResponseCacheData(url: String): ResponseCache {
        val cleanedUrl = url.replace(Regex("[?&](key|token)=[^&]*"), "")
        return withContext(Dispatchers.IO) {
            val urlObj = URL(url)
            val connection = urlObj.openConnection() as HttpURLConnection
            connection.useCaches = false
            connection.requestMethod = "GET"
            val inputStream = connection.inputStream

            val contentType = connection.contentType
            var mimeType: String? = null
            val encoding = "UTF-8"
            if (contentType != null) {
                val parts = contentType.split(";")
                mimeType = parts[0].trim()
            }

            // Save the response to a file
            val file: File = if (mimeType.equals("text/javascript")) File(
                MainApplication.appContext.cacheDir,
                "${url.hashCode()}.js"
            )
            else File(MainApplication.appContext.cacheDir, "${url.hashCode()}.png")
            FileOutputStream(file).use { fos ->
                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    fos.write(buffer, 0, len)
                }
            }
            ResponseCache(
                url = cleanedUrl,
                response = null,
                filePath = file.absolutePath,
                mimeType = mimeType,
                encoding = encoding
            )
        }
    }

    val currentUIMode: MutableLiveData<String> by lazy {
        MutableLiveData<String>(Constants.UI_MODE_LIGHT)
    }
}