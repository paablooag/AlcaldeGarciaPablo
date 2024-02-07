package com.example.practicafinal.ui.administrador.pedidos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PedidosViewModelAdmin {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

}