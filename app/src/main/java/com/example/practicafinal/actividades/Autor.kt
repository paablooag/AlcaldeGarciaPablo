package com.example.practicafinal.actividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.practicafinal.R

class Autor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autor)
    }

    fun volver(view: View) {
        var newIntent= Intent(this, ComprobadorTipo::class.java)
        startActivity(newIntent)
    }
}