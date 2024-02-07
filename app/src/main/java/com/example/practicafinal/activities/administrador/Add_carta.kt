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
import com.example.practicafinal.Carta
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

class Add_carta : AppCompatActivity(), CoroutineScope {

    private lateinit var nombreLayout: TextInputEditText
    private lateinit var categoriaLayout: Spinner
    private lateinit var precioLayout: TextInputEditText
    private lateinit var stockLayout: TextInputEditText
    private lateinit var photo: ImageView
    private lateinit var add: Button
    private var categoria: String?=null

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

        ArrayAdapter.createFromResource(
            this,
            R.array.categorias,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            categoriaLayout.adapter = adapter
        }

        categoriaLayout.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                categoria = parent?.getItemAtPosition(position).toString()
                categoriaLayout.setSelection(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // write code to perform some action
            }
        }

        add.setOnClickListener {

            if (nombreLayout.text.toString().trim().isEmpty()||categoria==null || precioLayout.text.toString().trim().isEmpty() || stockLayout.text.toString().trim().isEmpty()){
                Toast.makeText(applicationContext, "Faltan campos por rellenar", Toast.LENGTH_SHORT)
                    .show()
            }else if(url_photo==null){
                Toast.makeText(
                    applicationContext, "Falta seleccionar la foto", Toast.LENGTH_SHORT
                ).show()
            }else if(Utilidades.existeCarta(carta_list, nombreLayout.text.toString().trim())){
                Toast.makeText(applicationContext, "Esa carta ya existe", Toast.LENGTH_SHORT)
                    .show()
            }else{
                var generated_id:String?=db_ref.child("Cartas").push().key


                launch {
                    val url_photo_firebase= Utilidades.guardarFoto(generated_id!!, url_photo!!)

                    var carta= Carta(
                        generated_id,
                        nombreLayout.text.toString().trim().capitalize(),
                        precioLayout.text.toString().trim().capitalize(),
                        categoria!!,
                        stockLayout.text.toString().trim().capitalize(),
                        url_photo_firebase
                    )
                    Utilidades.crearCarta(db_ref, carta)


                    Utilidades.toastCourutine(
                        this_activity,
                        applicationContext,
                        "Carta creada con exito"
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