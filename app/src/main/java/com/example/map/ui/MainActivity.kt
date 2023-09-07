package com.example.map.ui

import android.os.Bundle
import android.view.View
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
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL


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
        recenterBtn.setOnClickListener { mapWebView.loadUrl("javascript:recenterMap()") }
    }

    private fun setUpWebView() {
        mapWebView.settings.javaScriptEnabled = true
        mapWebView.settings.loadWithOverviewMode = true
        mapWebView.settings.databaseEnabled = true
        mapWebView.settings.domStorageEnabled = true
        mapWebView.settings.cacheMode= WebSettings.LOAD_NO_CACHE

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

    fun loadUrlIntoWebView() {
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
                mapWebView.loadDataWithBaseURL("file:///android_asset/map/map.html", htmlString,
                    "text/html", "base64", null)
            }
        }
    }
}