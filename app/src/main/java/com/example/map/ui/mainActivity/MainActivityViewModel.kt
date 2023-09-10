package com.example.map.ui.mainActivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.map.common.Constants

class MainActivityViewModel : ViewModel() {
    val currentUIMode: MutableLiveData<String> by lazy {
        MutableLiveData<String>(Constants.UI_MODE_LIGHT)
    }
}