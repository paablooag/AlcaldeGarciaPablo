package com.example.practicafinal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Inscripcion (
    val id:String="",
    val id_evento:String="",
    val id_ususario:String=""
):Parcelable