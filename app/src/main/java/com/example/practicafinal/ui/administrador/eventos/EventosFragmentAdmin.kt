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
import com.example.practicafinal.activities.administrador.AddEvento
import com.example.practicafinal.Evento
import com.example.practicafinal.EventoAdaptador
import com.example.practicafinal.R
import com.example.practicafinal.activities.Autor
import com.example.practicafinal.activities.MainActivity
import com.example.practicafinal.databinding.FragmentEventosAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventosFragmentAdmin : Fragment() {

    private var _binding: FragmentEventosAdminBinding? = null
    private lateinit var recycler: RecyclerView
    private lateinit var lista: MutableList<Evento>
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

        adaptador = EventoAdaptador(lista)
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
                        // Handle item1 click
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

                    else -> false
                }
            }
            popupMenu.show()
        }

        _binding!!.add.setOnClickListener {

            var newIntent= Intent(context, AddEvento::class.java)
            startActivity(newIntent)

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}