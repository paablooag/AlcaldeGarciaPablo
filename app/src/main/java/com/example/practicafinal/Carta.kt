package com.example.practicafinal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Carta (
        var id:String="",
        var nombre:String="",
        var precio:String="",
        var categoria:String="",
        var stock:String="",
        var imagen:String=""
) : Parcelable
