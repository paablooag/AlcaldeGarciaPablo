package com.example.practicafinal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Usuario(
        var nombre:String="",
        var email:String="",
        var contraseña:String="",
        var tipo:String="cliente"
 ):Parcelable
