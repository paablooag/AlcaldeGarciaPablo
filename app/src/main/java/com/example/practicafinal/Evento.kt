package com.example.practicafinal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Evento (
        val id: String="",
        val nombre: String="",
        val fecha: String="",
        val precio: String="",
        val aforo_maximo: String="",
        val imagen: String=""
    ):Parcelable