package com.example.map.ui.mainActivity

import android.content.res.ColorStateList
import android.content.res.Configuration
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
import androidx.lifecycle.lifecycleScope
import com.example.map.R
import com.example.map.common.CommonUtils
import com.example.map.common.Constants
import com.example.map.common.WebViewInterface
import com.example.map.data.local.room.webViewResponse.ResponseCache
import com.example.map.databinding.ActivityMainBinding
import com.example.map.ui.mainActivity.utility.MapWebResourceResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
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


    override fun onResume() {
        super.onResume()
        val systemTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if(systemTheme == Configuration.UI_MODE_NIGHT_YES){
            enableDarkTheme()
        }else{
            if(viewModel.currentUIMode.value.equals(Constants.UI_MODE_DARK))
                enableDarkTheme()
            else
                enableLightTheme()
        }
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
                val url = request?.url?.toString()
                if ((url?.contains(
                        "https://maps.googleapis.com/maps/vt", true
                    ) == true) || (url?.contains("https://maps.gstatic.com/mapfiles/api-3/images/", true) == true)
                ) {

                    val mapWebResourceResponse = MapWebResourceResponse("application/json", "UTF-8", null, callback = {
                            // remove key and token from the url
                            val cleanedUrl = url.replace(Regex("[?&](key|token)=[^&]*"), "")
                            var cachedResponse: ResponseCache? = null
                            runBlocking {
                                val getCachedResponseJob = async {
                                    cachedResponse = viewModel.getCachedResponse(cleanedUrl)
                                }
                                getCachedResponseJob.await()
                            }
                            if (cachedResponse != null) {
                                val file = cachedResponse?.filePath?.let { File(it) }
                                val fis = FileInputStream(file)
                                return@MapWebResourceResponse WebResourceResponse(
                                    cachedResponse?.mimeType, cachedResponse?.encoding, fis
                                )
                            } else {
                                lifecycleScope.launch {
                                    val cache = viewModel.getImageResponseCacheData(url)
                                    if(cache.filePath!= null)
                                        viewModel.insertResponseCache(cache)
                                }
                                return@MapWebResourceResponse WebResourceResponse("application/json", "UTF-8", null)
                            }
                    })

                    return mapWebResourceResponse
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        mapWebView.addJavascriptInterface(WebViewInterface(this), "Android")
        mapWebView.loadUrl("file:///android_asset/map/map.html")

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // change ui mode listening to device configuration change
        when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                viewModel.currentUIMode.value = Constants.UI_MODE_DARK
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                viewModel.currentUIMode.value = Constants.UI_MODE_LIGHT
            }
        }

    }
}