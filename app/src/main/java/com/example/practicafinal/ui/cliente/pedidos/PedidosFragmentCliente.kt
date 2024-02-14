package com.example.practicafinal.ui.cliente.pedidos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicafinal.Pedido
import com.example.practicafinal.PedidosAdaptador
import com.example.practicafinal.R
import com.example.practicafinal.actividades.Autor
import com.example.practicafinal.actividades.MainActivity
import com.example.practicafinal.databinding.FragmentPedidosClienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PedidosFragmentCliente : Fragment(){

    private var _binding: FragmentPedidosClienteBinding? = null
    private lateinit var recycler: RecyclerView
    private lateinit var listaPedidos: MutableList<Pedido>
    private lateinit var adaptador: PedidosAdaptador
    private var applicationcontext = this.context
    private lateinit var db_ref: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosClienteBinding.inflate(inflater, container, false)

        var user = FirebaseAuth.getInstance()
        db_ref= FirebaseDatabase.getInstance().reference
        listaPedidos = mutableListOf()

        db_ref.child("Pedidos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaPedidos.clear()
                    snapshot.children.forEach { hijo: DataSnapshot?
                        ->
                        val pojo_pedido = hijo?.getValue(Pedido::class.java)
                        println(pojo_pedido!!.id_cliente)
                        println(user.currentUser?.uid.toString())
                        if (pojo_pedido!!.id_cliente.equals(user.currentUser?.uid.toString().trim())) {
                            println("entra")
                            listaPedidos.add(pojo_pedido)
                        }else{
                            println("no entra")

                        }
                    }
                    println(listaPedidos)
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }

            })

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

        adaptador = PedidosAdaptador(listaPedidos)
        recycler = _binding!!.recyclerView
        recycler.adapter = adaptador
        recycler.layoutManager = LinearLayoutManager(applicationcontext)

        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}