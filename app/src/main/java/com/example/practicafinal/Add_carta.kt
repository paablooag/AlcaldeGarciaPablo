package com.example.practicafinal

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.practicafinal.ui.home.HomeFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext


class Add_carta : AppCompatActivity(), CoroutineScope {

    private lateinit var nombreLayout: TextInputEditText
    private lateinit var categoriaLayout: TextInputEditText
    private lateinit var precioLayout: TextInputEditText
    private lateinit var stockLayout: TextInputEditText
    private lateinit var photo: ImageView
    private lateinit var add: Button

    private var url_photo: Uri?=null
    private lateinit var db_ref: DatabaseReference
    private lateinit var st_ref: StorageReference
    private lateinit var carta_list: MutableList<Carta>
    private lateinit var job: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_carta)
        val this_activity = this
        job = Job()

        nombreLayout=findViewById(R.id.add_name)
        categoriaLayout=findViewById(R.id.add_categoria)
        precioLayout=findViewById(R.id.add_precio)
        stockLayout=findViewById(R.id.add_stock)
        photo=findViewById(R.id.add_image)

        add=findViewById(R.id.guardar)

        db_ref= FirebaseDatabase.getInstance().reference
        st_ref = FirebaseStorage.getInstance().reference

        carta_list= Utilidades.obtenerCartas(db_ref)


        add.setOnClickListener {

            if (nombreLayout.text.toString().trim().isEmpty()||categoriaLayout.text.toString().trim().isEmpty() || precioLayout.text.toString().trim().isEmpty() || stockLayout.text.toString().trim().isEmpty()){
                Toast.makeText(applicationContext, "Faltan campos por rellenar", Toast.LENGTH_SHORT).show()
            }else if(url_photo==null){
                Toast.makeText(applicationContext, "Falta seleccionar la foto", Toast.LENGTH_SHORT
                ).show()
            }else if(Utilidades.existeCarta(carta_list, nombreLayout.text.toString().trim())){
                Toast.makeText(applicationContext, "Esa carta ya existe", Toast.LENGTH_SHORT)
                    .show()
            }else{
                var generated_id:String?=db_ref.child("Cartas").push().key

                launch {
                    val url_photo_firebase= Utilidades.guardarFoto(generated_id!!, url_photo!!)
                    if (url_photo == null || !File(url_photo!!.path).exists()) {
                        Toast.makeText(applicationContext, "La imagen no existe", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    var carta= Carta(
                        generated_id,
                        nombreLayout.text.toString().trim().capitalize(),
                        precioLayout.text.toString().trim().capitalize(),
                        categoriaLayout.text.toString().trim().capitalize(),
                        stockLayout.text.toString().trim().capitalize(),
                        url_photo_firebase
                    )
                    val result = Utilidades.crearCarta(db_ref, carta)
                    if (!result) {
                        Toast.makeText(applicationContext, "Error al crear la carta", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    Utilidades.toastCourutine(
                        this_activity,
                        applicationContext,
                        "Clinica creada con exito"
                    )

                    val newIntent= Intent(applicationContext, InicioAdmin::class.java)
                    startActivity(newIntent)

                }
            }
        }

        photo.setOnClickListener {
            galeryAcces.launch("image/*")
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val galeryAcces = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if(it!=null){
            url_photo = it
            photo.setImageURI(it)
        }
    }
}

private operator fun Unit.not(): Boolean {
    return false
}
