package com.example.practicafinal.actividades.administrador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.practicafinal.Evento
import com.example.practicafinal.Inscripcion
import com.example.practicafinal.R
import com.example.practicafinal.Usuario
import com.example.practicafinal.UsuarioAdaptador
import com.example.practicafinal.Utilidades
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventoInfo : AppCompatActivity() {

    private lateinit var adaptador: UsuarioAdaptador
    private lateinit var recycler: RecyclerView
    private var inscripcion: Inscripcion? = null
    private lateinit var listaUsuarios: MutableList<Usuario>

    override fun onCreate(savedInstanceState: Bundle?) {
        // Configura el tema antes de llamar a super.onCreate y setContentView
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("NightMode", false)
        setTheme(if (isNightMode) R.style.AppTheme_Dark else R.style.AppTheme_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento_info)

        val evento = intent.getParcelableExtra<Evento>("evento")
        val nombreEvento = findViewById<TextView>(R.id.nombre_evento)
        val precioEvento = findViewById<TextView>(R.id.precio_evento)
        val aforoMax = findViewById<TextView>(R.id.aforo_max_evento)
        val aforoAct = findViewById<TextView>(R.id.aforo_act_evemto)
        val imagenEvento = findViewById<ImageView>(R.id.imagen_evento)
        val fechaEvento = findViewById<TextView>(R.id.fecha_evento)

        var db_ref = FirebaseDatabase.getInstance().reference
        listaUsuarios = mutableListOf()

        nombreEvento.text = evento!!.nombre
        precioEvento.text = evento.precio
        aforoAct.text = evento.aforo
        aforoMax.text = evento.aforo_maximo
        fechaEvento.text = evento.fecha

        val URL: String? = when (evento.imagen) {
            "" -> null
            else -> evento.imagen
        }

        Glide.with(this).load(URL).apply(Utilidades.glideOptions(this)).transition(Utilidades.transition).into(imagenEvento)

        db_ref.child("Inscripciones")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { hijo: DataSnapshot? ->
                        val pojo_inscripcion = hijo?.getValue(Inscripcion::class.java)
                        if (pojo_inscripcion!!.id_evento.equals(evento.id, true)) {
                            inscripcion = pojo_inscripcion!!
                        }
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })

        db_ref.child("Usuarios")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaUsuarios.clear()
                    snapshot.children.forEach { hijo: DataSnapshot? ->
                        val pojo_usuario = hijo?.getValue(Usuario::class.java)
                        if (inscripcion != null) {
                            if (inscripcion!!.id_ususario.equals(pojo_usuario!!.id, true)) {
                                println(pojo_usuario)
                                listaUsuarios.add(pojo_usuario!!)
                            }
                        }
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })

        adaptador = UsuarioAdaptador(listaUsuarios)
        recycler = findViewById<RecyclerView>(R.id.recyclerViewEvento)
        recycler.adapter = adaptador
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.setHasFixedSize(true)
    }
}
