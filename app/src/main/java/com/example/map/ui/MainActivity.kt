package com.example.map.ui

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.map.utils.WebViewInterface
import com.example.map.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapWebView: WebView
    private lateinit var recenterBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapWebView = binding.webviewMap
        recenterBtn = binding.btnRecenter

        initOnClick()
        setUpWebView()
    }

    private fun initOnClick() {
        recenterBtn.setOnClickListener { mapWebView.loadUrl("javascript:recenterMap()") }
    }

    private fun setUpWebView() {
        mapWebView.settings.javaScriptEnabled = true
        mapWebView.loadUrl("file:///android_asset/map/map.html");
        mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                recenterBtn.visibility = View.VISIBLE
            }

        }
        mapWebView.addJavascriptInterface(WebViewInterface(this), "Android")
    }
}