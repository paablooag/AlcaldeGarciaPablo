package com.example.practicafinal

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
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

class EditarCarta : AppCompatActivity(), CoroutineScope {

    private lateinit var carta: Carta
    private lateinit var nombre: TextInputEditText
    private lateinit var precio: TextInputEditText
    private lateinit var stock: TextInputEditText
    private lateinit var categoria: TextInputEditText
    private lateinit var photo: ImageView
    private lateinit var guardar: Button

    private var url_photo: Uri? = null
    private lateinit var db_ref: DatabaseReference
    private lateinit var st_ref: StorageReference
    private lateinit var carta_list: MutableList<Carta>
    private lateinit var job: Job
    private var beforeName=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_carta)

        val this_activity = this
        job = Job()

        carta = intent.getParcelableExtra<Carta>("carta")!!
        nombre = findViewById(R.id.add_name)
        nombre.setText(carta.nombre)
        beforeName=nombre.text.toString()
        precio = findViewById(R.id.add_precio)
        precio.setText(carta.precio)
        stock = findViewById(R.id.add_stock)
        stock.setText(carta.stock)
        categoria = findViewById(R.id.add_categoria)
        categoria.setText(carta.categoria)
        guardar = findViewById(R.id.guardar)

        photo = findViewById(R.id.add_image)

        Glide.with(applicationContext)
            .load(carta.imagen)
            .apply(Utilidades.glideOptions(applicationContext))
            .transition(Utilidades.transition)
            .into(photo)

        db_ref = FirebaseDatabase.getInstance().getReference()
        st_ref = FirebaseStorage.getInstance().getReference()

        carta_list = Utilidades.obtenerCartas(db_ref)

        guardar.setOnClickListener {

            if (nombre.text.toString().trim().isEmpty() ||
                categoria.text.toString().trim().isEmpty() || precio.text.toString().trim()
                    .isEmpty() || stock.text.toString().trim().isEmpty()
            ) {

                Toast.makeText(
                    applicationContext, "Faltan datos en el " +
                            "formularion", Toast.LENGTH_SHORT
                ).show()

            } else if (Utilidades.existeCarta(
                    carta_list,
                    nombre.text.toString().trim()
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
                        url_photo_firebase = carta.imagen!!
                    } else {
                        url_photo_firebase =
                            Utilidades.guardarFoto(carta.id!!, url_photo!!)
                    }

                    var carta = Carta(
                        carta.id!!,
                        nombre.text.toString().trim().capitalize(),
                        precio.text.toString().trim().capitalize(),
                        stock.text.toString().trim().capitalize(),
                        categoria.text.toString().trim().capitalize(),
                        url_photo_firebase,

                    )

                    Utilidades.crearCarta(db_ref,carta)

                    Utilidades.toastCourutine(
                        this_activity,
                        applicationContext,
                        "Carta modificado con exito"
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