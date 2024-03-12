package com.example.practicafinal

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Carta (
        var id:String="",
        var nombre:String="",
        var repeticiones:String="0.0",
        var categoria:String="",
        var series:String="0",
        var imagen:String="",
) : Parcelable
