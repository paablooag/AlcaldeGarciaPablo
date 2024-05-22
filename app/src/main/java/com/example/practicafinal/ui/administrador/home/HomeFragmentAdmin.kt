package com.example.practicafinal.ui.administrador.home

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicafinal.actividades.administrador.AnadirCarta
import com.example.practicafinal.Carta
import com.example.practicafinal.CartaAdaptador
import com.example.practicafinal.actividades.MainActivity
import com.example.practicafinal.R
import com.example.practicafinal.actividades.Autor
import com.example.practicafinal.databinding.FragmentHomeAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragmentAdmin : Fragment() {
    private lateinit var recycler: RecyclerView
    private var _binding: FragmentHomeAdminBinding? = null
    private lateinit var lista: MutableList<Carta>
    private lateinit var adaptador: CartaAdaptador
    private var applicationcontext = this.context
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()  // Get the context
        val sharedPreferences = context.getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("NightMode", false)

        val themeContext = ContextThemeWrapper(context, if (isNightMode) R.style.AppTheme_Dark else R.style.AppTheme_Light)
        val themedInflater = inflater.cloneInContext(themeContext)

        _binding = FragmentHomeAdminBinding.inflate(themedInflater, container, false)
        val rootView = _binding!!.root

        var db_ref = FirebaseDatabase.getInstance().reference
        var user = FirebaseAuth.getInstance()
        lista = mutableListOf<Carta>()

        db_ref.child("Cartas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lista.clear()
                snapshot.children.forEach { hijo: DataSnapshot? ->
                    val pojo_carta = hijo?.getValue(Carta::class.java)
                    lista.add(pojo_carta!!)
                }
                recycler.adapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.message)
            }
        })

        adaptador = CartaAdaptador(lista)
        recycler = _binding!!.recyclerView
        recycler.adapter = adaptador
        recycler.layoutManager = LinearLayoutManager(context)

        _binding!!.settings.setOnClickListener {
            val popupMenu = PopupMenu(context, it)

            popupMenu.inflate(R.menu.popup_menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.log_out -> {
                        // Handle item1 click
                        user.signOut()
                        val newIntent = Intent(context, MainActivity::class.java)
                        startActivity(newIntent)
                        true
                    }

                    R.id.autor -> {
                        val newIntent = Intent(context, Autor::class.java)
                        startActivity(newIntent)
                        true
                    }

                    R.id.modo_dia_noche -> {
                        // Toggle theme
                        val isNightMode = sharedPreferences.getBoolean("NightMode", false)
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("NightMode", !isNightMode)
                        editor.apply()

                        // Restart activity to apply the new theme
                        val intent = activity?.intent
                        activity?.finish()
                        if (intent != null) {
                            startActivity(intent)
                        }
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }

        _binding!!.addCarta.setOnClickListener {
            val newIntent = Intent(context, AnadirCarta::class.java)
            startActivity(newIntent)
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
