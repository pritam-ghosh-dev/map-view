package com.example.map.ui

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewClientCompat
import com.example.map.utils.WebViewInterface
import com.example.map.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapWebView: WebView
    private lateinit var recenterBtn: Button
    private var webUrlCoroutineJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapWebView = binding.webviewMap
        recenterBtn = binding.btnRecenter

        initOnClick()
        saveUrlDataToFile()
        setUpWebView()
        loadUrlIntoWebView()
    }

    private fun initOnClick() {
        val darkModeStyles = loadJSONFromAsset("map/dark_mode_style.json")
        recenterBtn.setOnClickListener { mapWebView.loadUrl("javascript:recenterMap()")
            mapWebView.loadUrl("javascript:enableDarkMode($darkModeStyles)")
        }
    }

    private fun setUpWebView() {
        mapWebView.settings.javaScriptEnabled = true
        mapWebView.settings.loadWithOverviewMode = true
        mapWebView.settings.databaseEnabled = true
        mapWebView.settings.domStorageEnabled = true
        mapWebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
/*
        mapWebView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                val action: Int? = event?.actionMasked
                return when (action) {
                    MotionEvent.ACTION_MOVE -> {
                        val javascript =
                            "(function() { return document.documentElement.outerHTML; })();"
                        mapWebView?.evaluateJavascript(javascript) { html ->
                            val htmlContent = StringEscapeUtils.unescapeJava(html)
                            val path: File = filesDir
                            val file = File(path, "offlineFile.txt")
                            file.writeText("")
                            val stream = FileOutputStream(file)
                            stream.use { stream ->
                                stream.write(htmlContent.toByteArray())
                            }
                        }
                        false
                    }
                    else -> false
                }
            }
        })*/
        mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                recenterBtn.visibility = View.VISIBLE
            }

        }
        mapWebView.addJavascriptInterface(WebViewInterface(this), "Android")
        val webViewClient = WebViewClientCompat()
        mapWebView.webViewClient = webViewClient
    }

    private fun loadAndSaveDataFromUrlToFile() {
        val assetManager = assets
        try {
            val inputStream = assetManager.open("map/map.html")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var input: String?
            val stringBuffer = StringBuffer()
            while (bufferedReader.readLine().also { input = it } != null) {
                stringBuffer.append(input)
            }
            bufferedReader.close()

            val htmlData = stringBuffer.toString()
            val path: File = filesDir
            val file = File(path, "offlineFile.txt")
            val stream = FileOutputStream(file)
            stream.use { stream ->
                stream.write(htmlData.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveUrlDataToFile() {
        webUrlCoroutineJob = CoroutineScope(Dispatchers.IO).launch {
            loadAndSaveDataFromUrlToFile()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Loading finished", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun loadUrlIntoWebView() {
        webUrlCoroutineJob = CoroutineScope(Dispatchers.IO).launch {
            val path: File = filesDir
            val file = File(path, "offlineFile.txt")
            val length = file.length().toInt()
            val bytes = ByteArray(length)
            val fileInputStream = FileInputStream(file)
            try {
                fileInputStream.read(bytes)
            } finally {
                fileInputStream.close()
            }
            val htmlString = String(bytes)
            withContext(Dispatchers.Main) {
                mapWebView.loadDataWithBaseURL(
                    "file:///android_asset/map/map.html", htmlString, "text/html", "base64", null
                )
            }
        }
    }

    fun loadJSONFromAsset(filename: String): String {
        val json: String
        try {
            val inputStream: InputStream = assets.open(filename)
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