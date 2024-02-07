package com.example.practicafinal.activities.administrador

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

class AddEvento : AppCompatActivity(), CoroutineScope {

    private lateinit var nombreLayout: TextInputEditText
    private lateinit var fechaLayout: TextInputEditText
    private lateinit var precioLayout: TextInputEditText
    private lateinit var aforoLayout: TextInputEditText
    private lateinit var photo: ImageView
    private lateinit var add: Button

    private var url_photo: Uri?=null
    private lateinit var db_ref: DatabaseReference
    private lateinit var st_ref: StorageReference
    private lateinit var evento_list: MutableList<Evento>
    private lateinit var job: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_evento)
        val this_activity = this
        job = Job()

        nombreLayout=findViewById(R.id.add_name)
        fechaLayout=findViewById(R.id.add_fecha)
        precioLayout=findViewById(R.id.add_precio)
        aforoLayout=findViewById(R.id.add_aforo)
        photo=findViewById(R.id.add_image)

        add=findViewById(R.id.guardar)

        db_ref= FirebaseDatabase.getInstance().reference
        st_ref = FirebaseStorage.getInstance().reference

        evento_list= Utilidades.obtenerEventos(db_ref)


        add.setOnClickListener {

            if (nombreLayout.text.toString().trim().isEmpty()||fechaLayout.text.toString().trim().isEmpty() || precioLayout.text.toString().trim().isEmpty() || aforoLayout.text.toString().trim().isEmpty()){
                Toast.makeText(applicationContext, "Faltan campos por rellenar", Toast.LENGTH_SHORT)
                    .show()
            }else if(url_photo==null){
                Toast.makeText(
                    applicationContext, "Falta seleccionar la foto", Toast.LENGTH_SHORT
                ).show()
            }else if(Utilidades.existeEvento(evento_list, nombreLayout.text.toString().trim(),fechaLayout.text.toString().trim())){
                Toast.makeText(applicationContext, "Ese evento ya existe", Toast.LENGTH_SHORT)
                    .show()
            }else{
                var generated_id:String?=db_ref.child("Eventos").push().key


                launch {
                    val url_photo_firebase=
                        Utilidades.guardarFotoEvento(generated_id!!, url_photo!!)

                    var evento= Evento(
                        generated_id,
                        nombreLayout.text.toString().trim().capitalize(),
                        fechaLayout.text.toString().trim().capitalize(),
                        precioLayout.text.toString().trim().capitalize(),
                        aforoLayout.text.toString().trim().capitalize(),
                        url_photo_firebase
                    )
                    Utilidades.crearEvento(db_ref, evento)


                    Utilidades.toastCourutine(
                        this_activity,
                        applicationContext,
                        "Evento creada con exito"
                    )

                    val newIntent= Intent(applicationContext, InicioAdmin::class.java)
                    startActivity(newIntent)

                }
            }
        }

        photo.setOnClickListener {
            galeryAcces.launch(arrayOf<String>("image/*"))
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val galeryAcces = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if(it!=null){
            url_photo = it
            photo.setImageURI(it)
        }
    }
}