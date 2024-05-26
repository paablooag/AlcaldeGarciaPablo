package com.example.practicafinal

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.practicafinal.actividades.administrador.EditarCarta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class CartaAdaptador(private val listaCartas:MutableList<Carta>): RecyclerView.Adapter<CartaAdaptador.CartaViewHolder>(),
    Filterable {

    private var isEur = true
    private lateinit var contexto: Context
    private var listaFiltrada=listaCartas
    class CartaViewHolder(itemVista: View) : RecyclerView.ViewHolder(itemVista) {
        val fotoCarta=itemVista.findViewById<ImageView>(R.id.photo_item)
        val nombreCarta=itemVista.findViewById<TextView>(R.id.name_item)
        val precioCarta=itemVista.findViewById<TextView>(R.id.precio_item)
        val categoriaCarta=itemVista.findViewById<TextView>(R.id.categoria_item)
        val stockCarta=itemVista.findViewById<TextView>(R.id.stock_item)
        val añadirCarrito=itemVista.findViewById<CardView>(R.id.añadir_carrito)
        val boton_convertir = itemVista.findViewById<Button>(R.id.btn_convert)
        val informacion = itemVista.findViewById<ImageView>(R.id.info)
    }
    override fun onCreateViewHolder(grupoPadre: ViewGroup, tipoVista: Int): CartaViewHolder {
        val vistaItem =
            LayoutInflater.from(grupoPadre.context).inflate(R.layout.item_carta, grupoPadre, false)
        contexto = grupoPadre.context
        return CartaViewHolder(vistaItem)
    }

    override fun onBindViewHolder(portadorVista: CartaViewHolder, posicion: Int) {
        val cartaActual=listaFiltrada[posicion]
        portadorVista.nombreCarta.text=cartaActual.nombre
        portadorVista.categoriaCarta.text="Categoria: "+cartaActual.categoria
        portadorVista.stockCarta.text="Stock: "+cartaActual.stock
        portadorVista.precioCarta.text="Precio: "+cartaActual.precio+"€"

        portadorVista.boton_convertir.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val rate = getConversionRate()
                val convertedPrice = if (isEur) cartaActual.precio.toDouble() * rate else cartaActual.precio.toDouble() / rate
                cartaActual.precio = convertedPrice.toString()
                val roundedPrice = String.format("%.2f", convertedPrice)
                withContext(Dispatchers.Main) {
                    var currencySymbol = if (isEur) "$" else "€"
                    portadorVista.precioCarta.text="Precio: "+roundedPrice + currencySymbol
                    isEur = !isEur
                }
            }
        }

        val intent = Intent(contexto, Informacion::class.java)
        portadorVista.informacion.setOnClickListener {
            contexto.startActivity(intent)
        }

        val URL:String? = when (cartaActual.imagen){
            ""->null
            else->cartaActual.imagen
        }

        var preferenciasCompartidas = PreferenceManager.getDefaultSharedPreferences(contexto)
        var tipoUsuario = preferenciasCompartidas.getString("tipo", "cliente")

        if (tipoUsuario.equals("cliente",true)){

            portadorVista.añadirCarrito.visibility = View.VISIBLE

            portadorVista.itemView.setOnLongClickListener {
                false
            }

            portadorVista.añadirCarrito.setOnClickListener {
                val referenciaBaseDatos = FirebaseDatabase.getInstance().getReference()
                val idUsuario = FirebaseAuth.getInstance().currentUser?.uid
                val id = referenciaBaseDatos.push().key
                cartaActual.stock=(cartaActual.stock.toInt()-1).toString()
                val idAndroid= Settings.Secure.getString(
                    contexto.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                referenciaBaseDatos.child("Cartas").child(cartaActual.id).setValue(cartaActual)
                val pedido=Pedido(id!!, idUsuario!!, cartaActual.id, "pendiente", cartaActual.precio, cartaActual.nombre,EstadoNoti.creado,idAndroid)
                Utilidades.crearPedido(referenciaBaseDatos, pedido)
                val imageView = portadorVista.añadirCarrito.findViewById<ImageView>(R.id.carrito_item)
                imageView.setImageResource(R.drawable.baseline_check_circle_24)

            }

        }else{

            portadorVista.itemView.setOnLongClickListener {
                val menuPopup = PopupMenu(contexto, portadorVista.itemView)

                menuPopup.inflate(R.menu.carta_op_menu)

                menuPopup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.editar -> {
                            var nuevoIntento = Intent(contexto, EditarCarta::class.java)
                            nuevoIntento.putExtra("carta", cartaActual)
                            contexto.startActivity(nuevoIntento)
                            true
                        }

                        R.id.eliminar -> {
                            val referenciaBaseDatos = FirebaseDatabase.getInstance().getReference()
                            val referenciaAlmacenamiento = FirebaseStorage.getInstance().getReference()

                            listaFiltrada.remove(cartaActual)
                            referenciaAlmacenamiento.child("Cartas").child("photos").child(cartaActual.id!!).delete()
                            referenciaBaseDatos.child("Cartas").child(cartaActual.id!!).removeValue()
                            Toast.makeText(contexto, "Carta borrada con exito", Toast.LENGTH_SHORT)
                                .show()
                            true
                        }

                        else -> false
                    }
                }
                try {
                    val campoMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                    campoMPopup.isAccessible = true
                    val mPopup = campoMPopup.get(menuPopup)
                    mPopup.javaClass
                        .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(mPopup, true)
                } catch (e: Exception) {
                    Log.e("Main", "Error mostrando iconos del menu.", e)
                } finally {
                    menuPopup.show()
                }
                true
            }
        }
        Glide.with(contexto).load(URL).apply(Utilidades.glideOptions(contexto)).transition(Utilidades.transition).into(portadorVista.fotoCarta)
    }
    private suspend fun getConversionRate(): Double {
        val url = URL("https://api.exchangerate-api.com/v4/latest/EUR")
        val resultText = url.readText()
        val jsonObject = JSONObject(resultText)
        return jsonObject.getJSONObject("rates").getDouble("USD")
    }
    override fun getItemCount(): Int = listaFiltrada.size

    override fun getFilter(): Filter {
        return  object : Filter(){
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val busqueda = p0.toString().lowercase()

                if (busqueda.isEmpty()){
                    listaFiltrada = listaCartas
                }else {
                    listaFiltrada = (listaCartas.filter {
                        if (it.nombre.toString().lowercase().contains(busqueda)){
                            it.nombre.toString().lowercase().contains(busqueda)
                        }else{
                            it.categoria.toString().lowercase().contains(busqueda)
                        }
                    }) as MutableList<Carta>
                }

                val resultadosFiltro = FilterResults()
                resultadosFiltro.values = listaFiltrada
                return resultadosFiltro
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                notifyDataSetChanged()
            }

        }
    }
}