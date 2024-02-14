package com.example.practicafinal.actividades

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.practicafinal.Estado_not
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

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser?=null
    private var email:String=""
    private var password:String=""
    private lateinit var emailEdit: TextInputEditText
    private lateinit var passwordEdit: TextInputEditText
    private lateinit var log: Button
    private lateinit var newintent: Intent
    private lateinit var register: TextView
    private lateinit var forgotpass: TextView
    private lateinit var db_ref: DatabaseReference
    private var androidId: String = ""
    private lateinit var generator: AtomicInteger


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        inicializarVariables()
        log()

        createChannel()
        androidId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        db_ref = FirebaseDatabase.getInstance().reference
        generator = AtomicInteger(0)

        db_ref.child("Pedidos")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val pojo = snapshot.getValue(Pedido::class.java)
                    if (!pojo!!.userNotifications.equals(androidId) && pojo.not_state!!.equals(
                            Estado_not.creado
                        )
                    ) {
                        db_ref.child("Pedidos").child(pojo.id!!)
                            .child("not_state").setValue(Estado_not.notificado)
                        generateNotification(
                            generator.incrementAndGet(),
                            pojo,
                            "Tienes un nuevo pedido!" + pojo.id,
                            "Nuevos datos en la app",
                            PedidosFragmentAdmin::class.java
                        )
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val pojo = snapshot.getValue(Pedido::class.java)
                    if (!pojo!!.userNotifications.equals(androidId) && pojo.not_state!!.equals(
                            Estado_not.modificado
                        )
                    ) {
                        db_ref.child("Pedidos").child(pojo.id!!)
                            .child("not_state").setValue(Estado_not.notificado)
                        if (pojo.id_cliente==auth.currentUser?.uid)
                        generateNotification(
                            generator.incrementAndGet(),
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

    private fun generateNotification(
        id_not: Int,
        pojo: Parcelable,
        content: String,
        tittle: String,
        destiny: Class<*>
    ) {
        val newIntent = Intent(applicationContext, destiny)
        newIntent.putExtra("clinic", pojo)

        var id = "test_channel"
        var pedingIntent =
            PendingIntent.getActivity(this, 0, newIntent, PendingIntent.FLAG_MUTABLE)

        val notification = NotificationCompat.Builder(this, id)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(tittle)
            .setContentText(content)
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
            notify(id_not, notification)
        }
    }
    private fun createChannel() {
        val name = "basic_channel"
        var id = "test_channel"
        val description = "basic notification"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(id, name, importance).apply {
            this.description = description
        }

        val nm: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    private fun inicializarVariables(){
        auth= FirebaseAuth.getInstance()
        user=auth.currentUser
        emailEdit=findViewById<TextInputEditText>(R.id.user)
        passwordEdit=findViewById<TextInputEditText>(R.id.password)
        log=findViewById<Button>(R.id.log)
        register=findViewById<TextView>(R.id.register)
        forgotpass=findViewById<TextView>(R.id.forgotpass)

        register.setPaintFlags(register.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
        forgotpass.setPaintFlags(forgotpass.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

        if (user!=null){
            //Especificar Intent
            newintent= Intent(this, ComprobadorTipo::class.java)
            startActivity(newintent)
        }

        //Falta hacer las animaciones etc

        register.setOnClickListener {
            //Animation.animation(register, 0.98f, 1.0f, 100)
            //register.setTextColor(getColor(R.color.softblack))
            //register.postDelayed({ register.setTextColor(getColor(R.color.black)) }, 300)
            newintent= Intent(this, Register::class.java)
            startActivity(newintent)
        }

//        forgotpass.setOnClickListener {
        //Animation.animation(register, 0.98f, 1.0f, 100)
        //register.setTextColor(getColor(R.color.softblack))
        //register.postDelayed({ register.setTextColor(getColor(R.color.black)) }, 300)
        //newintent=Intent(this, ForgotPass::class.java)
//        }
    }

    private fun log(){
        log.setOnClickListener {
            if (emailEdit.text.isNullOrBlank() || passwordEdit.text.isNullOrBlank()){
                if (emailEdit.text.isNullOrBlank()){
                    emailEdit.setError("Este campo es obligatorio")
                }
                if (passwordEdit.text.isNullOrBlank()){
                    passwordEdit.setError("Este campo es obligatorio")
                }
            }else{
                email=emailEdit.text.toString()
                password=passwordEdit.text.toString()

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        user=auth.currentUser
                        newintent= Intent(this, ComprobadorTipo::class.java)
                        startActivity(newintent)
                    } else {
                        Toast.makeText(this, "Usuario o contraseña incorrectas", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            }
        }
    }

}