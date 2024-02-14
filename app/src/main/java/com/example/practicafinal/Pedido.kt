package com.example.practicafinal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pedido (
    var id: String = "",
    var id_cliente: String = "",
    var id_producto: String = "",
    var estado: String = "",
    var precio: String = "",
    var nombre: String = "",
    var not_state:Int?=null,
    var userNotifications:String?=null
):Parcelable