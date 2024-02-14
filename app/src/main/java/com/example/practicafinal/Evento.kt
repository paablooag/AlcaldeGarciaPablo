package com.example.practicafinal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Evento (
        val id: String="",
        val nombre: String="",
        val fecha: String="",
        val precio: String="",
        var aforo:String="0",
        val aforo_maximo: String="0",
        val imagen: String=""
    ):Parcelable