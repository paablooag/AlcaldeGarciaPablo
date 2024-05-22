package com.example.practicafinal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicafinal.actividades.administrador.EditarEvento
import com.example.practicafinal.actividades.administrador.EventoInfo
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

class MensajeActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var lista: MutableList<Mensaje>
    private lateinit var db_ref: DatabaseReference
    private lateinit var mensaje_enviado: EditText
    private lateinit var boton_enviar: Button
    private var usuario_actual: Carta? = null
    private var last_pos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // Obtén el SharedPreferences y aplica el tema antes de llamar a super.onCreate
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("NightMode", false)
        if (isNightMode) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensaje)

        usuario_actual = intent.getParcelableExtra<Carta>("USUARIO")
        if (usuario_actual != null) {
            // Resto del código
        } else {
            // Maneja el caso en que club_actual es null aquí
        }

        last_pos = intent.getIntExtra("LAST_POS", 100000)
        Log.d("LASTTT_POS_LLEGAMOS", last_pos.toString())
        db_ref = FirebaseDatabase.getInstance().getReference()
        lista = mutableListOf()
        mensaje_enviado = findViewById(R.id.texto_mensaje)
        boton_enviar = findViewById(R.id.boton_enviar)

        boton_enviar.setOnClickListener {
            last_pos = 1
            val mensaje = mensaje_enviado.text.toString().trim()

            if (mensaje.trim().isNotEmpty()) {
                val hoy: Calendar = Calendar.getInstance()
                val formateador = SimpleDateFormat("YYYY-MM-dd HH:mm:ss", Locale.getDefault())
                val fecha_hora = formateador.format(hoy.time)

                val id_mensaje = db_ref.child("chat").child("mensajes").push().key!!
                val nuevo_mensaje = Mensaje(
                    id_mensaje,
                    usuario_actual?.id,
                    "",
                    "",
                    mensaje,
                    fecha_hora
                )
                db_ref.child("chat").child("mensajes").child(id_mensaje).setValue(nuevo_mensaje)
                mensaje_enviado.setText("")
            } else {
                Toast.makeText(applicationContext, "Escribe algo", Toast.LENGTH_SHORT).show()
            }
        }

        db_ref.child("chat").child("mensajes").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                GlobalScope.launch(Dispatchers.IO) {
                    val pojo_mensaje = snapshot.getValue(Mensaje::class.java)
                    pojo_mensaje!!.id_receptor = usuario_actual?.id
                    if (pojo_mensaje.id_receptor == pojo_mensaje.id_emisor) {
                        pojo_mensaje.imagen_emisor = usuario_actual?.imagen
                    } else {
                        val semaforo = CountDownLatch(1)

                        db_ref.child("usuarios").child(pojo_mensaje.id_emisor!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val club = snapshot.getValue(Carta::class.java)
                                    pojo_mensaje.imagen_emisor = club!!.imagen
                                    semaforo.countDown()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    println(error.message)
                                }
                            })
                        semaforo.await()
                    }

                    runOnUiThread {
                        lista.add(pojo_mensaje)
                        lista.sortBy { it.fecha_hora }
                        recycler.adapter!!.notifyDataSetChanged()
                        if (last_pos < lista.size && last_pos != 1 && last_pos != 100000) {
                            recycler.scrollToPosition(last_pos)
                        } else {
                            recycler.scrollToPosition(lista.size - 1)
                        }
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })

        recycler = findViewById(R.id.rview_mensajes)
        recycler.adapter = MensajeAdaptador(lista, last_pos)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.setHasFixedSize(true)

        val imagenAtras = findViewById<ImageView>(R.id.back)
        imagenAtras.setOnClickListener {
            val intent = Intent(this, EventoInfo::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        val actividad = Intent(applicationContext, EventoInfo::class.java)
        last_pos = lista.size
        actividad.putExtra("LAST_POS", last_pos)
        Log.d("LASTTT_POS_ATRAS", last_pos.toString())
        startActivity(actividad)
    }
}
