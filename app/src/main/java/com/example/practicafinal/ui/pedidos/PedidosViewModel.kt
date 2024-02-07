package com.example.practicafinal.ui.pedidos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PedidosViewModel {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

}