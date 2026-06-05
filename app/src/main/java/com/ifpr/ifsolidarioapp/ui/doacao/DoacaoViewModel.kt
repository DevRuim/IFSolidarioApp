package com.ifpr.ifsolidarioapp.ui.doacao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DoacaoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is doacao Fragment"
    }
    val text: LiveData<String> = _text
}