package com.example.practicafinal.ui.administrador.eventos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicafinal.actividades.administrador.AnadirEvento
import com.example.practicafinal.Evento
import com.example.practicafinal.EventoAdaptador
import com.example.practicafinal.Inscripcion
import com.example.practicafinal.R
import com.example.practicafinal.actividades.Autor
import com.example.practicafinal.actividades.MainActivity
import com.example.practicafinal.databinding.FragmentEventosAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.appcompat.app.AppCompatDelegate
import android.content.res.Configuration
import android.view.MenuItem
class EventosFragmentAdmin : Fragment() {

    private var _binding: FragmentEventosAdminBinding? = null
    private lateinit var recycler: RecyclerView
    private lateinit var lista: MutableList<Evento>
    private lateinit var inscripcioines: MutableList<Inscripcion>
    private lateinit var adaptador: EventoAdaptador
    private var applicationcontext = this.context
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventosAdminBinding.inflate(inflater, container, false)
        val root: View = _binding!!.root

        var user=FirebaseAuth.getInstance()
        var db_ref= FirebaseDatabase.getInstance().reference
        lista= mutableListOf<Evento>()
        inscripcioines = mutableListOf()

        db_ref.child("Eventos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    snapshot.children.forEach { hijo: DataSnapshot?
                        ->
                        val pojo_evento = hijo?.getValue(Evento::class.java)
                        lista.add(pojo_evento!!)
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }

            })

        db_ref.child("Inscripciones")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    inscripcioines.clear()
                    snapshot.children.forEach { hijo: DataSnapshot?
                        ->
                        val pojo_inscripcion = hijo?.getValue(Inscripcion::class.java)
                        inscripcioines.add(pojo_inscripcion!!)
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }

            })

        adaptador = EventoAdaptador(lista,inscripcioines)
        recycler = _binding!!.recyclerViewEventos
        recycler.adapter = adaptador
        recycler.layoutManager = LinearLayoutManager(applicationcontext)
        recycler.setHasFixedSize(true)

        _binding!!.settings.setOnClickListener {
            val popupMenu = PopupMenu(context, it)

            popupMenu.inflate(R.menu.popup_menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.log_out -> {
                        user.signOut()
                        var newIntent= Intent(context, MainActivity::class.java)
                        startActivity(newIntent)
                        true
                    }

                    R.id.autor -> {
                        var newIntent= Intent(context, Autor::class.java)
                        startActivity(newIntent)
                        true
                    }

                    R.id.modo_dia_noche -> {
                        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            // Actualiza los colores de la interfaz para el modo claro
                            context?.setTheme(R.style.AppTheme_Light)
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            // Actualiza los colores de la interfaz para el modo oscuro
                            context?.setTheme(R.style.AppTheme_Dark)
                        }
                        // Reinicia la actividad para que los cambios surtan efecto
                        activity?.recreate()
                        true
                    }


                    else -> false
                }
            }
            popupMenu.show()
        }

        _binding!!.addCarta.setOnClickListener {

            var newIntent= Intent(context, AnadirEvento::class.java)
            startActivity(newIntent)

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}