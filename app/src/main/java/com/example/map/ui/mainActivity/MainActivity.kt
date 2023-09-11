package com.example.map.ui.mainActivity

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.map.MainApplication
import com.example.map.R
import com.example.map.common.CommonUtils
import com.example.map.common.Constants
import com.example.map.common.WebViewInterface
import com.example.map.data.local.room.AppDatabase
import com.example.map.data.local.room.webViewResponse.ResponseCache
import com.example.map.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var mapWebView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        setContentView(binding.root)
        mapWebView = binding.webviewMap


        binding.themeCard.strokeColor = ContextCompat.getColor(this, R.color.grey)
        binding.btnRecenter.strokeColor =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey))
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.grey)

        initOnClick()
        subscribeToLiveData()
        setUpWebView()
    }

    private fun subscribeToLiveData() {
        viewModel.currentUIMode.observe(this) { value ->
            when (value) {
                Constants.UI_MODE_LIGHT -> {
                    enableLightTheme()
                }

                Constants.UI_MODE_DARK -> {
                    enableDarkTheme()
                }
            }
        }
    }

    private fun enableDarkTheme() {
        val darkModeStyles = CommonUtils.loadJSONFromAsset("map/dark_mode_style.json", this)
        binding.themeCard.setCardBackgroundColor(
            ContextCompat.getColor(
                this, R.color.grey
            )
        )
        binding.themeImage.setImageDrawable(
            ContextCompat.getDrawable(
                this, R.drawable.ic_day_mode
            )
        )
        binding.btnRecenter.setBackgroundColor(
            ContextCompat.getColor(
                this, R.color.grey
            )
        )
        binding.btnRecenter.setTextColor(ContextCompat.getColor(this, R.color.white))
        mapWebView.loadUrl("javascript:enableDarkMode($darkModeStyles)")
    }

    private fun enableLightTheme() {
        binding.themeCard.setCardBackgroundColor(
            ContextCompat.getColor(
                this, R.color.white
            )
        )
        binding.themeImage.setImageDrawable(
            ContextCompat.getDrawable(
                this, R.drawable.ic_night_mode
            )
        )
        binding.btnRecenter.setBackgroundColor(
            ContextCompat.getColor(
                this, R.color.white
            )
        )
        binding.btnRecenter.setTextColor(ContextCompat.getColor(this, R.color.black))
        mapWebView.loadUrl("javascript:resetStyle()")
    }

    private fun initOnClick() {
        binding.btnRecenter.setOnClickListener {
            mapWebView.loadUrl("javascript:recenterMap()")
        }
        binding.themeCard.setOnClickListener {
            if (viewModel.currentUIMode.value?.equals(Constants.UI_MODE_DARK) == true) viewModel.currentUIMode.value =
                Constants.UI_MODE_LIGHT
            else viewModel.currentUIMode.value = Constants.UI_MODE_DARK
        }
    }

    private fun setUpWebView() {
        mapWebView.settings.javaScriptEnabled = true
        mapWebView.settings.loadWithOverviewMode = true
        mapWebView.settings.databaseEnabled = true
        mapWebView.settings.domStorageEnabled = true
        mapWebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                binding.btnRecenter.visibility = View.VISIBLE
            }

            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {
                val database = AppDatabase.getInstance(MainApplication.appContext)
                val url = request?.url?.toString()
                if (url?.contains("https://maps.googleapis.com", true) == true) {
                    var webResourceResponse: WebResourceResponse? = null

                    if (url.contains("GetViewportInfo", true)) {
                        runBlocking {
                            val job = async(Dispatchers.IO) {
                                val cachedResponse =
                                    database.responseCacheDao().getResponseCacheByUrl(url)
                                if (cachedResponse != null) {
                                    val inputStream: InputStream = ByteArrayInputStream(
                                        cachedResponse.response?.toByteArray(StandardCharsets.UTF_8)
                                    )
                                    WebResourceResponse(
                                        "application/json", "UTF-8", inputStream
                                    )
                                } else {
                                    try {
                                        val urlObj = URL(url)
                                        val connection =
                                            urlObj.openConnection() as HttpURLConnection
                                        connection.useCaches = false
                                        connection.requestMethod = "POST"
                                        var inputStream = connection.inputStream

                                        val reader = BufferedReader(InputStreamReader(inputStream))
                                        val sb = StringBuilder()
                                        var line: String?
                                        while (reader.readLine().also { line = it } != null) {
                                            sb.append(line)
                                        }
                                        var response = sb.toString()

                                        val cache = ResponseCache(
                                            url = url,
                                            filePath = null,
                                            response = response,
                                            mimeType = "application/json",
                                            encoding = "UTF-8"
                                        )
                                        database.responseCacheDao().insertResponseCache(cache)

                                        inputStream = ByteArrayInputStream(
                                            response.toByteArray(StandardCharsets.UTF_8)
                                        )
                                        webResourceResponse = WebResourceResponse(
                                            "application/json", "UTF_8", inputStream
                                        )
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }
                            job.await()
                        }
                    } else {
                        val cleanedUrl = url.replace(Regex("[?&](key|token)=[^&]*"), "")
                        var cachedResponse: ResponseCache? = null
                        runBlocking {
                            val getCachedResponseJob = async(Dispatchers.IO) {
                                cachedResponse =
                                    database.responseCacheDao().getResponseCacheByUrl(cleanedUrl)
                            }
                            getCachedResponseJob.await()
                            if (cachedResponse != null) {
                                val file = cachedResponse?.filePath?.let { File(it) }
                                val fis = withContext(Dispatchers.IO) {
                                    FileInputStream(file)
                                }
                                webResourceResponse = WebResourceResponse(
                                    cachedResponse?.mimeType, cachedResponse?.encoding, fis
                                )
                            } else {
                                val job = async(Dispatchers.IO) {
                                    val urlObj = URL(url)
                                    val connection = urlObj.openConnection() as HttpURLConnection
                                    connection.useCaches = false
                                    connection.requestMethod = "GET"
                                    var inputStream = connection.inputStream

                                    val contentType = connection.contentType
                                    var mimeType: String? = null
                                    val encoding: String = "UTF-8"
                                    if (contentType != null) {
                                        val parts = contentType.split(";")
                                        mimeType = parts[0].trim()
                                    }

                                    // Save the response to a file
                                    val file: File = if(mimeType.equals("text/javascript"))
                                        File(cacheDir, "${url.hashCode()}.js")
                                    else
                                        File(cacheDir, "${url.hashCode()}.png")
                                    FileOutputStream(file).use { fos ->
                                        val buffer = ByteArray(1024)
                                        var len: Int
                                        while (inputStream.read(buffer).also { len = it } != -1) {
                                            fos.write(buffer, 0, len)
                                        }
                                    }
                                    val cache = ResponseCache(
                                        url = cleanedUrl,
                                        response = null,
                                        filePath = file.absolutePath,
                                        mimeType = mimeType,
                                        encoding = encoding
                                    )
                                    database.responseCacheDao().insertResponseCache(cache)
                                    inputStream = FileInputStream(file)
                                    webResourceResponse =
                                        WebResourceResponse(mimeType, encoding, inputStream)
                                }
                                job.await()
                            }
                        }
                    }
                    return webResourceResponse
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        mapWebView.addJavascriptInterface(WebViewInterface(this), "Android")
        mapWebView.loadUrl("file:///android_asset/map/map.html")
    }


}