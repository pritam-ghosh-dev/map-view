package com.example.map.ui.mainActivity.utility

import android.webkit.WebResourceResponse
import java.io.InputStream

class MapWebResourceResponse(mimeType: String?, encoding: String?, data: InputStream?, callback: () -> WebResourceResponse?) :
    WebResourceResponse(mimeType, encoding, data) {
        init {
                val response = callback()
                if(response!=null && response.data!=null){
                    super.setEncoding(response.encoding)
                    super.setMimeType(response.mimeType)
                    super.setData(response.data)
                }

        }
}