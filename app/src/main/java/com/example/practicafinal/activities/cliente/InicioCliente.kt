package com.example.practicafinal.activities.cliente

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.practicafinal.R
import com.example.practicafinal.databinding.ActivityInicioClienteBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class InicioCliente : AppCompatActivity() {
    private lateinit var binding: ActivityInicioClienteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInicioClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navViewCliente

        val navController = findNavController(R.id.nav_host_fragment_activity_navegacion_cliente)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        navView.setupWithNavController(navController)
    }
}