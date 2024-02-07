package com.example.practicafinal.ui.cliente.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicafinal.Carta
import com.example.practicafinal.CartaAdaptador
import com.example.practicafinal.activities.MainActivity
import com.example.practicafinal.R
import com.example.practicafinal.activities.Autor
import com.example.practicafinal.databinding.FragmentHomeClienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragmentCliente : Fragment() {
    private lateinit var recycler: RecyclerView
    private var _binding: FragmentHomeClienteBinding? = null
    private lateinit var lista: MutableList<Carta>
    private lateinit var adaptador: CartaAdaptador
    private var applicationcontext = this.context
    private lateinit var db_ref: DatabaseReference
    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeClienteBinding.inflate(inflater, container, false)
        db_ref= FirebaseDatabase.getInstance().reference
        var user = FirebaseAuth.getInstance()
        lista= mutableListOf<Carta>()

        cargarCartas()

        adaptador = CartaAdaptador(lista)
        recycler = _binding!!.recyclerView
        recycler.adapter = adaptador
        recycler.layoutManager = LinearLayoutManager(applicationcontext)

        _binding!!.filtrarCard.setOnClickListener {
            val popupMenu = PopupMenu(context, it)

            popupMenu.inflate(R.menu.filtrar_menu)

            popupMenu.setOnMenuItemClickListener {item ->
                when(item.itemId){
                    R.id.nombre -> {
                        cargarCartas(true)
                        true
                    }

                    R.id.azul -> {
                        filtrarCategoria("azul")
                        true
                    }

                    R.id.blanco -> {
                        filtrarCategoria("blanco")
                        true
                    }

                    R.id.negro ->{
                        filtrarCategoria("negro")
                        true
                    }

                    R.id.rojo ->{
                        filtrarCategoria("rojo")
                        true
                    }

                    R.id.verde ->{
                        filtrarCategoria("verde")
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }

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

        return _binding!!.root
    }

    private fun filtrarCategoria(categoria: String){
        db_ref.child("Cartas")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    snapshot.children.forEach { hijo: DataSnapshot?
                        ->
                        val pojo_carta = hijo?.getValue(Carta::class.java)

                        if (pojo_carta!!.stock.toInt() > 0 && pojo_carta.categoria.equals(categoria,true)) {
                            lista.add(pojo_carta!!)
                        }
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }

            })
        recycler.adapter?.notifyDataSetChanged()
    }

    private fun cargarCartas(boolean: Boolean=false){
        db_ref.child("Cartas")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    snapshot.children.forEach { hijo: DataSnapshot?
                        ->
                        val pojo_carta = hijo?.getValue(Carta::class.java)

                        if (pojo_carta!!.stock.toInt() > 0) {
                            lista.add(pojo_carta!!)
                        }
                    }
                    if (boolean){
                        lista.sortBy { it.nombre }
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }

            })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}