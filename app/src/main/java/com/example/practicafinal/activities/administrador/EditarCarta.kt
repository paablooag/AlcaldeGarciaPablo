package com.example.practicafinal.activities.administrador

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.practicafinal.Carta
import com.example.practicafinal.R
import com.example.practicafinal.R.array
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

class EditarCarta : AppCompatActivity(), CoroutineScope {

    private lateinit var carta: Carta
    private lateinit var nombre: TextInputEditText
    private lateinit var precio: TextInputEditText
    private lateinit var stock: TextInputEditText
    private lateinit var categoriaLayout: Spinner
    private lateinit var photo: ImageView
    private lateinit var guardar: Button
    private var categoria: String? = null
    private lateinit var  array : Array<String>
    private var pos=0

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
        categoriaLayout = findViewById(R.id.add_categoria)
        array = resources.getStringArray(R.array.categorias)
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

        ArrayAdapter.createFromResource(
            this,
            R.array.categorias,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            categoriaLayout.adapter = adapter
            pos = array.indexOf(carta.categoria)
            categoriaLayout.setSelection(pos)
        }

        categoriaLayout.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                categoria = parent?.getItemAtPosition(position).toString()
                categoriaLayout.setSelection(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        guardar.setOnClickListener {

            if (nombre.text.toString().trim().isEmpty() ||
                categoria==null || precio.text.toString().trim()
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
                        categoria!!,
                        stock.text.toString().trim().capitalize(),
                        url_photo_firebase,

                        )

                    Utilidades.crearCarta(db_ref, carta)

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