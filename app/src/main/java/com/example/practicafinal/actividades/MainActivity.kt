package com.example.practicafinal.actividades

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.practicafinal.EstadoNoti
import com.example.practicafinal.Pedido
import com.example.practicafinal.R
import com.example.practicafinal.ui.administrador.pedidos.PedidosFragmentAdmin
import com.example.practicafinal.ui.cliente.pedidos.PedidosFragmentCliente
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {

    private lateinit var autenticacion: FirebaseAuth
    private var usuario: FirebaseUser?=null
    private var correo:String=""
    private var contrasena:String=""
    private lateinit var correoEdit: TextInputEditText
    private lateinit var contrasenaEdit: TextInputEditText
    private lateinit var iniciarSesion: Button
    private lateinit var nuevoIntento: Intent
    private lateinit var registro: TextView
    private lateinit var olvidoContrasena: TextView
    private lateinit var referenciaDB: DatabaseReference
    private var idAndroid: String = ""
    private lateinit var generador: AtomicInteger


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        inicializarVariables()
        iniciarSesion()

        crearCanal()
        idAndroid = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        referenciaDB = FirebaseDatabase.getInstance().reference
        generador = AtomicInteger(0)

        referenciaDB.child("Pedidos")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val pojo = snapshot.getValue(Pedido::class.java)
                    if (!pojo!!.userNotifications.equals(idAndroid) && pojo.not_state!!.equals(
                            EstadoNoti.creado
                        )
                    ) {
                        referenciaDB.child("Pedidos").child(pojo.id!!)
                            .child("not_state").setValue(EstadoNoti.notificado)
                        generarNotificacion(
                            generador.incrementAndGet(),
                            pojo,
                            "Tienes un nuevo pedido!" + pojo.id,
                            "Nuevos datos en la app",
                            PedidosFragmentAdmin::class.java
                        )
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val pojo = snapshot.getValue(Pedido::class.java)
                    if (!pojo!!.userNotifications.equals(idAndroid) && pojo.not_state!!.equals(
                            EstadoNoti.modificado
                        )
                    ) {
                        referenciaDB.child("Pedidos").child(pojo.id!!)
                            .child("not_state").setValue(EstadoNoti.notificado)
                        if (pojo.id_cliente==autenticacion.currentUser?.uid)
                            generarNotificacion(
                                generador.incrementAndGet(),
                                pojo,
                                "Se ha aceptado tu  pedido" + pojo.id,
                                "Datos modificados en la app",
                                PedidosFragmentCliente::class.java
                            )
                    }
                }


                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun generarNotificacion(
        id_not: Int,
        pojo: Parcelable,
        contenido: String,
        titulo: String,
        destino: Class<*>
    ) {
        val nuevoIntento = Intent(applicationContext, destino)
        nuevoIntento.putExtra("clinic", pojo)

        var id = "test_channel"
        var pedingIntent =
            PendingIntent.getActivity(this, 0, nuevoIntento, PendingIntent.FLAG_MUTABLE)

        val notificacion = NotificationCompat.Builder(this, id)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setSubText("sistema de informacion")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pedingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(this)) {

            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
            }
            notify(id_not, notificacion)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun crearCanal() {
        val nombre = "basic_channel"
        var id = "test_channel"
        val descripcion = "basic notification"
        val importancia = NotificationManager.IMPORTANCE_DEFAULT

        val canal = NotificationChannel(id, nombre, importancia).apply {
            this.description = descripcion
        }

        val nm: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(canal)
    }

    private fun inicializarVariables(){
        autenticacion= FirebaseAuth.getInstance()
        usuario=autenticacion.currentUser
        correoEdit=findViewById<TextInputEditText>(R.id.user)
        contrasenaEdit=findViewById<TextInputEditText>(R.id.password)
        iniciarSesion=findViewById<Button>(R.id.log)
        registro=findViewById<TextView>(R.id.register)
        olvidoContrasena=findViewById<TextView>(R.id.forgotpass)

        registro.setPaintFlags(registro.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
        olvidoContrasena.setPaintFlags(olvidoContrasena.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

        if (usuario!=null){
           nuevoIntento= Intent(this, ComprobadorTipo::class.java)
            startActivity(nuevoIntento)
        }


        registro.setOnClickListener {
        nuevoIntento= Intent(this, Register::class.java)
            startActivity(nuevoIntento)
        }

    }

    private fun iniciarSesion(){
        iniciarSesion.setOnClickListener {
            if (correoEdit.text.isNullOrBlank() || contrasenaEdit.text.isNullOrBlank()){
                if (correoEdit.text.isNullOrBlank()){
                    correoEdit.setError("Este campo es obligatorio")
                }
                if (contrasenaEdit.text.isNullOrBlank()){
                    contrasenaEdit.setError("Este campo es obligatorio")
                }
            }else{
                correo=correoEdit.text.toString()
                contrasena=contrasenaEdit.text.toString()

                autenticacion.signInWithEmailAndPassword(correo, contrasena).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        usuario=autenticacion.currentUser
                        nuevoIntento= Intent(this, ComprobadorTipo::class.java)
                        startActivity(nuevoIntento)
                    } else {
                        Toast.makeText(this, "Usuario o contraseña incorrectas", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            }
        }
    }

}
