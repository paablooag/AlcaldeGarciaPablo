package com.example.practicafinal

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
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

        // Funciones de creación
        fun crearUsuario(email:String, password:String, nombre:String,img:String){
            val referenciaBaseDatos = FirebaseDatabase.getInstance().reference
            val usuario = Usuario(FirebaseAuth.getInstance().currentUser!!.uid,nombre, email, password,"cliente",img)
            referenciaBaseDatos.child("Usuarios").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(usuario)
        }

        fun crearCarta(referenciaBaseDatos:DatabaseReference,carta: Carta){
            referenciaBaseDatos.child("Cartas").child(carta.id).setValue(carta)
        }

        fun crearPedido(referenciaBaseDatos:DatabaseReference,pedido: Pedido){
            referenciaBaseDatos.child("Pedidos").child(pedido.id).setValue(pedido)
        }

        fun crearEvento(referenciaBaseDatos:DatabaseReference,evento: Evento){
            referenciaBaseDatos.child("Eventos").child(evento.id).setValue(evento)
        }

        // Funciones de existencia
        fun existeCarta(cartas: List<Carta>, name: String): Boolean {
            return cartas.any { it.nombre!!.lowercase() == name.lowercase() }
        }

        fun existeEvento(eventos: List<Evento>, name: String,fecha:String): Boolean {
            return eventos.any { it.nombre!!.lowercase() == name.lowercase() && it.fecha!! == fecha }
        }

        // Funciones de obtención
        fun obtenerCartas(referenciaBaseDatos:DatabaseReference): MutableList<Carta> {
            val listaCartas = mutableListOf<Carta>()

            referenciaBaseDatos.child("Cartas")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { child: DataSnapshot ->
                            val carta = child.getValue(Carta::class.java)
                            listaCartas.add(carta!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })
            return listaCartas
        }

        fun obtenerEventos(referenciaBaseDatos:DatabaseReference): MutableList<Evento> {
            val listaEventos = mutableListOf<Evento>()

            referenciaBaseDatos.child("Eventos")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { child: DataSnapshot ->
                            val evento = child.getValue(Evento::class.java)
                            listaEventos.add(evento!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.message)
                    }
                })
            return listaEventos
        }

        suspend fun obtenerUsuario(referenciaBaseDatos: DatabaseReference):Usuario{
            var usuario:Usuario?=null
            try {
                val dataSnapshot = referenciaBaseDatos.child("Usuarios").child(FirebaseAuth.getInstance().currentUser!!.uid).get().await()
                usuario = dataSnapshot.getValue(Usuario::class.java)
            } catch (e: Exception) {

            }
            return usuario!!
        }

        // Funciones de guardado de fotos
        suspend fun guardarFotoCarta(id: String, image: Uri): String {
            lateinit var url_photo_firebase: Uri
            val referenciaAlmacenamiento: StorageReference = FirebaseStorage.getInstance().reference
            url_photo_firebase = referenciaAlmacenamiento.child("Cartas").child("photos").child(id)
                .putFile(image).await().storage.downloadUrl.await()

            return url_photo_firebase.toString()
        }

        suspend fun guardarFotoEvento(id: String, image: Uri): String {
            lateinit var url_photo_firebase: Uri
            val referenciaAlmacenamiento: StorageReference = FirebaseStorage.getInstance().reference
            url_photo_firebase = referenciaAlmacenamiento.child("Eventos").child("photos").child(id)
                .putFile(image).await().storage.downloadUrl.await()

            return url_photo_firebase.toString()
        }

        suspend fun guardarFotoUsuario(image: Uri): String {
            lateinit var url_photo_firebase: Uri
            val referenciaAlmacenamiento: StorageReference = FirebaseStorage.getInstance().reference
            url_photo_firebase = referenciaAlmacenamiento.child("Usuarios").child("photos").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .putFile(image).await().storage.downloadUrl.await()

            return url_photo_firebase.toString()
        }

        // Funciones de utilidad
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