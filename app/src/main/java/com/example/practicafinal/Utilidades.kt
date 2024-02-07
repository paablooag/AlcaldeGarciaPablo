package com.example.practicafinal

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await


class Utilidades {

    companion object{

        fun crearUsuario(email:String, password:String, nombre:String){
            var dtb_ref= FirebaseDatabase.getInstance().reference
            val usuario=Usuario(nombre, email, password)
            dtb_ref.child("Usuarios").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(usuario)
        }

        fun existeCarta(clinic: List<Carta>, name: String): Boolean {
            return clinic.any { it.nombre!!.lowercase() == name.lowercase() }
        }

        fun obtenerCartas(dtb_ref:DatabaseReference): MutableList<Carta> {
            var list = mutableListOf<Carta>()

            dtb_ref.child("Cartas")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { child: DataSnapshot ->
                            val pojo_carta = child.getValue(Carta::class.java)
                            list.add(pojo_carta!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })
            return list
        }

        fun crearCarta(dtb_ref:DatabaseReference,carta: Carta){
            dtb_ref.child("Cartas").child(carta.id).setValue(carta)
        }

        suspend fun guardarFoto(id: String, image: Uri): String {
            lateinit var url_photo_firebase: Uri
            var sto_ref: StorageReference = FirebaseStorage.getInstance().reference
            url_photo_firebase = sto_ref.child("Cartas").child("photos").child(id)
                .putFile(image).await().storage.downloadUrl.await()

            return url_photo_firebase.toString()
        }

       suspend fun obtenerUsuario(dtb_ref: DatabaseReference):Usuario{
            var usuario:Usuario?=null
           try {
               val dataSnapshot = dtb_ref.child("Usuarios").child(FirebaseAuth.getInstance().currentUser!!.uid).get().await()
               usuario = dataSnapshot.getValue(Usuario::class.java)
           } catch (e: Exception) {
               // Manejar excepción
           }
            return usuario!!
        }

        fun toastCourutine(activity: Activity, contex: Context, text: String) {
            activity.runOnUiThread {
                Toast.makeText(contex, text, Toast.LENGTH_SHORT).show()
            }
        }

        fun load_animation(contex: Context): CircularProgressDrawable {
            val animation = CircularProgressDrawable(contex)
            animation.strokeWidth = 5f
            animation.centerRadius = 30f
            animation.start()
            return animation
        }

        val transition = DrawableTransitionOptions.withCrossFade(500)
        fun glideOptions(contex: Context): RequestOptions {
            val options = RequestOptions().placeholder(load_animation(contex))
                .fallback(R.drawable.logo)
                .error(R.drawable.baseline_error_24)
            return options
        }

    }
}