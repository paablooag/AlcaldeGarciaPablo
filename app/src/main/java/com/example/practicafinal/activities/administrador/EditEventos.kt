package com.example.practicafinal.activities.administrador

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.practicafinal.Evento
import com.example.practicafinal.R
import com.example.practicafinal.Utilidades
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class EditEventos : AppCompatActivity() , CoroutineScope {

    private lateinit var evento: Evento
    private lateinit var nombre: TextInputEditText
    private lateinit var precio: TextInputEditText
    private lateinit var fecha: TextInputEditText
    private lateinit var aforo: TextInputEditText
    private lateinit var photo: ImageView
    private lateinit var guardar: Button

    private var url_photo: Uri? = null
    private lateinit var db_ref: DatabaseReference
    private lateinit var st_ref: StorageReference
    private lateinit var evento_list: MutableList<Evento>
    private lateinit var job: Job
    private var beforeName=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_eventos)
        val this_activity = this
        job = Job()

        evento = intent.getParcelableExtra<Evento>("evento")!!
        nombre = findViewById(R.id.add_name)
        nombre.setText(evento.nombre)
        beforeName=nombre.text.toString()
        precio = findViewById(R.id.add_precio)
        precio.setText(evento.precio)
        fecha = findViewById(R.id.add_fecha)
        fecha.setText(evento.fecha)
        aforo = findViewById(R.id.add_aforo)
        aforo.setText(evento.aforo_maximo)
        guardar = findViewById(R.id.guardar)

        photo = findViewById(R.id.add_image)

        Glide.with(applicationContext)
            .load(evento.imagen)
            .apply(Utilidades.glideOptions(applicationContext))
            .transition(Utilidades.transition)
            .into(photo)

        db_ref = FirebaseDatabase.getInstance().getReference()
        st_ref = FirebaseStorage.getInstance().getReference()

        evento_list = Utilidades.obtenerEventos(db_ref)

        guardar.setOnClickListener {

            if (nombre.text.toString().trim().isEmpty() || precio.text.toString().trim().isEmpty() || fecha.text.toString().trim().isEmpty() || aforo.text.toString().trim().isEmpty()) {

                Toast.makeText(
                    applicationContext, "Faltan datos en el " +
                            "formularion", Toast.LENGTH_SHORT
                ).show()

            } else if (Utilidades.existeEvento(
                    evento_list,
                    nombre.text.toString().trim(),
                    fecha.text.toString().trim()
                ) && !nombre.text.toString().trim().equals(beforeName)
            ) {
                Toast.makeText(applicationContext, "Esa Carta ya existe", Toast.LENGTH_SHORT)
                    .show()
            } else {

                //GlobalScope(Dispatchers.IO)
                photo.setOnClickListener {
                    galeryAcces.launch("image/*")
                }

                launch {
                    var url_photo_firebase = String()
                    if (url_photo == null) {
                        url_photo_firebase = evento.imagen!!
                    } else {
                        url_photo_firebase =
                            Utilidades.guardarFoto(evento.id!!, url_photo!!)
                    }

                    var evento = Evento(
                        evento.id!!,
                        nombre.text.toString().trim().capitalize(),
                        fecha.text.toString().trim(),
                        precio.text.toString().trim().capitalize(),
                        aforo.text.toString().trim(),
                        url_photo_firebase,

                        )

                    Utilidades.crearEvento(db_ref, evento)

                    Utilidades.toastCourutine(
                        this_activity,
                        applicationContext,
                        "Evento modificado con exito"
                    )
                    val activity = Intent(applicationContext, InicioAdmin::class.java)
                    startActivity(activity)
                }

                photo.setOnClickListener {
                    galeryAcces.launch("image/*")
                }

            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private val galeryAcces = registerForActivityResult(ActivityResultContracts.GetContent())
    {uri: Uri? ->
        if(uri!=null){
            url_photo = uri
            photo.setImageURI(uri)
        }


    }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

}