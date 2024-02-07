package com.example.practicafinal.activities.administrador

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.practicafinal.R
import com.example.practicafinal.databinding.ActivityInicioAdminBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class InicioAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityInicioAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityInicioAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navViewAdmin

        val navController = findNavController(R.id.nav_host_fragment_activity_navegacion_admin)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        navView.setupWithNavController(navController)
    }

}