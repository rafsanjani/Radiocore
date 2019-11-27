package com.foreverrafs.radiocore.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private var _metaData = MutableLiveData<String>("Artist : Title")
    val metaData: LiveData<String> = _metaData

    fun updateStreamMetaData(data: String) {
        _metaData.value = data
    }
}