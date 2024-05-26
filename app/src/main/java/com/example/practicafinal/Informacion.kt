package com.example.practicafinal

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Informacion : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informacion)

        val entradaPeso: EditText = findViewById(R.id.entrada_peso)
        val entradaAltura: EditText = findViewById(R.id.entrada_altura)
        val botonCalcular: Button = findViewById(R.id.boton_calcular)
        val resultadoIMC: TextView = findViewById(R.id.resultado_imc)
        val backButton: ImageView = findViewById(R.id.back)

        botonCalcular.setOnClickListener {
            val pesoStr = entradaPeso.text.toString()
            val alturaStr = entradaAltura.text.toString()

            if (pesoStr.isNotEmpty() && alturaStr.isNotEmpty()) {
                val peso = pesoStr.toFloat()
                val altura = alturaStr.toFloat()

                val imc = peso / (altura * altura)
                val estadoIMC = when {
                    imc < 18.5 -> "Bajo peso"
                    imc in 18.5..24.9 -> "Peso normal"
                    imc in 25.0..29.9 -> "Sobrepeso"
                    else -> "Obesidad"
                }
                resultadoIMC.text = "Tu IMC es: %.2f\nEstado: %s".format(imc, estadoIMC)
            } else {
                resultadoIMC.text = "Por favor, ingresa tu peso y altura."
            }
        }

        backButton.setOnClickListener {
            finish()  // Cierra la actividad actual y vuelve a la anterior
        }
    }
}
