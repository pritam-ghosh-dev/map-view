package com.example.map.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebViewInterface (private val mContext: Context) {

    @JavascriptInterface
    fun recenterMap() {
        Toast.makeText(mContext, "Re-centered",Toast.LENGTH_SHORT).show();
    }

    /**
    * JS Interface function to redirect the user to Google Maps with the selected location
    * */
    @JavascriptInterface
    fun openMap(lat: Double, long: Double) {
        val gmmIntentUri = Uri.parse("geo:$lat,$long?q=$lat,$long")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(mContext.packageManager) != null) {
            mContext.startActivity(mapIntent)
        } else {
            // If no suitable activity is found to handle the intent, prompt the user to install Google Maps from the Play Store.
            val playStoreUri = Uri.parse("market://details?id=com.google.android.apps.maps")
            val playStoreIntent = Intent(Intent.ACTION_VIEW, playStoreUri)
            try {
                mContext.startActivity(playStoreIntent)
            } catch (e: ActivityNotFoundException) {
                // When Play Store is not available on the device open maps on browser.
                val mapsWebUri = Uri.parse("https://www.google.com/maps?q=$lat,$long")
                val mapsWebIntent = Intent(Intent.ACTION_VIEW, mapsWebUri)
                // Specify that the user can choose the browser to open the link.
                mapsWebIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                try {
                    mContext.startActivity(mapsWebIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(mContext, "Kindly update device to latest version and install web browser", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}