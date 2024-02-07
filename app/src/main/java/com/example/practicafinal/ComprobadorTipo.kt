package com.example.practicafinal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ComprobadorTipo : AppCompatActivity() {
    var user:Usuario?=null
    private lateinit var dtb_ref: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comprobador_tipo)

        var sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this)

        dtb_ref= FirebaseDatabase.getInstance().reference
        CoroutineScope(Dispatchers.IO).launch {
            user = Utilidades.obtenerUsuario(dtb_ref)
            sharedPreferences.edit().apply(){
                putString("nombre", user!!.nombre)
                putString("email", user!!.email)
                putString("contraseña", user!!.contraseña)
                putString("tipo", user!!.tipo)
                apply()
            }
            withContext(Dispatchers.Main){
                if (user!!.tipo.equals("admin")) {
                    val newIntent = Intent(this@ComprobadorTipo, InicioAdmin::class.java)
                    startActivity(newIntent)
                }else{
                    val newIntent = Intent(this@ComprobadorTipo, InicioCliente::class.java)
                    startActivity(newIntent)
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}