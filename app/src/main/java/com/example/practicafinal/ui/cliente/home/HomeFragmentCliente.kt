package com.example.practicafinal.ui.cliente.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practicafinal.Carta
import com.example.practicafinal.CartaAdaptador
import com.example.practicafinal.actividades.MainActivity
import com.example.practicafinal.R
import com.example.practicafinal.actividades.Autor
import com.example.practicafinal.databinding.FragmentHomeClienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject
class HomeFragmentCliente : Fragment() {
    private lateinit var recycler: RecyclerView
    private var _binding: FragmentHomeClienteBinding? = null
    private lateinit var lista: MutableList<Carta>
    private lateinit var adaptador: CartaAdaptador
    private var applicationcontext = this.context
    private lateinit var db_ref: DatabaseReference
    private var isEur = true
    private lateinit var convertButton: Button
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Obtén SharedPreferences y aplica el tema antes de inflar la vista
        val sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("NightMode", false)
        if (isNightMode) {
            requireActivity().setTheme(R.style.AppTheme_Dark)
        } else {
            requireActivity().setTheme(R.style.AppTheme_Light)
        }

        _binding = FragmentHomeClienteBinding.inflate(inflater, container, false)
        db_ref = FirebaseDatabase.getInstance().reference
        val user = FirebaseAuth.getInstance()
        val search = _binding!!.search
        lista = mutableListOf<Carta>()

        cargarCartas()

        search.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adaptador.filter.filter(newText)
                return true
            }
        })

        adaptador = CartaAdaptador(lista)
        recycler = _binding!!.recyclerView
        recycler.adapter = adaptador
        recycler.layoutManager = LinearLayoutManager(applicationcontext)

        _binding!!.filtrarCard.setOnClickListener {
            val popupMenu = PopupMenu(context, it)

            popupMenu.inflate(R.menu.filtrar_menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.nombre -> {
                        cargarCartas(true)
                        true
                    }
                    R.id.proteina -> {
                        filtrarCategoria("proteina")
                        true
                    }
                    R.id.creatina -> {
                        filtrarCategoria("creatina")
                        true
                    }
                    R.id.preentreno -> {
                        filtrarCategoria("preentreno")
                        true
                    }
                    R.id.ropa -> {
                        filtrarCategoria("ropa")
                        true
                    }
                    R.id.otro -> {
                        filtrarCategoria("otro")
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
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("NightMode", !isNightMode)
                        editor.apply()
                        requireActivity().recreate()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        return _binding!!.root
    }

    private fun convertCurrency() {
        dbRef.child("Cartas").get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.children.forEach { childSnapshot ->
                val price = childSnapshot.child("precio").value.toString().toDouble()
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val rate = getConversionRate()
                    val convertedPrice = if (isEur) price * rate else price / rate
                    withContext(Dispatchers.Main) {
                        // Aquí debes actualizar la vista con el nuevo precio
                        isEur = !isEur
                    }
                }
            }
        }.addOnFailureListener {
            // Aquí debes manejar el error
        }
    }

    private suspend fun getConversionRate(): Double {
        val url = URL("https://api.exchangerate-api.com/v4/latest/EUR")
        val resultText = url.readText()
        val jsonObject = JSONObject(resultText)
        return jsonObject.getJSONObject("rates").getDouble("USD")
    }

    private fun filtrarCategoria(categoria: String) {
        db_ref.child("Cartas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lista.clear()
                snapshot.children.forEach { hijo: DataSnapshot? ->
                    val pojo_carta = hijo?.getValue(Carta::class.java)

                    if (pojo_carta!!.stock.toInt() > 0 && pojo_carta.categoria.equals(categoria, true)) {
                        lista.add(pojo_carta)
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

    private fun cargarCartas(boolean: Boolean = false) {
        db_ref.child("Cartas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lista.clear()
                snapshot.children.forEach { hijo: DataSnapshot? ->
                    val pojo_carta = hijo?.getValue(Carta::class.java)

                    if (pojo_carta!!.stock.toInt() > 0) {
                        lista.add(pojo_carta)
                    }
                }
                if (boolean) {
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
