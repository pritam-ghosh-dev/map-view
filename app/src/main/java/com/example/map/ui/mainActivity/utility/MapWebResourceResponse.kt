package com.example.map.ui.mainActivity.utility

import android.webkit.WebResourceResponse
import java.io.InputStream

class MapWebResourceResponse(mimeType: String?, encoding: String?, data: InputStream?, val callback: () -> WebResourceResponse?) :
    WebResourceResponse(mimeType, encoding, data) {
    override fun getMimeType(): String {
        val response = callback()
        if (response != null && response.data != null) {
            super.setEncoding(response.encoding)
            super.setMimeType(response.mimeType)
            super.setData(response.data)
            return response.mimeType
        }
        return super.getMimeType()
    }

}