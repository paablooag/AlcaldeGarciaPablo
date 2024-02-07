package com.example.practicafinal

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        inicializarVariables()
        log()
    }

    private fun inicializarVariables(){
        auth=FirebaseAuth.getInstance()
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
            newintent=Intent(this, ComprobadorTipo::class.java)
            startActivity(newintent)
        }


        register.setOnClickListener {
            newintent=Intent(this, Register::class.java)
            startActivity(newintent)
        }

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
                        newintent=Intent(this, ComprobadorTipo::class.java)
                        startActivity(newintent)
                    } else {
                        Toast.makeText(this, "Usuario o contraseña incorrectas", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    }

}