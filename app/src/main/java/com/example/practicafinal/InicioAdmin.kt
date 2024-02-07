package com.example.practicafinal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.practicafinal.databinding.ActivityInicioAdminBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class InicioAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityInicioAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding =ActivityInicioAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_navegacion)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        navView.setupWithNavController(navController)
    }

}