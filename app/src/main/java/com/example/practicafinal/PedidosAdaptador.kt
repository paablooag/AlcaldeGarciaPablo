package com.example.practicafinal

import android.content.Context
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class PedidosAdaptador(private val listaPedidos:MutableList<Pedido>) : RecyclerView.Adapter<PedidosAdaptador.PedidoViewHolder>(),
    Filterable {

    private lateinit var contexto: Context
    private var listaFiltrada=listaPedidos

    class PedidoViewHolder(vistaItem: View) : RecyclerView.ViewHolder(vistaItem) {
        val nombrePedido=vistaItem.findViewById<TextView>(R.id.name_item_pedido)
        val precioPedido=vistaItem.findViewById<TextView>(R.id.precio_item_pedido)
        val estadoPedido=vistaItem.findViewById<TextView>(R.id.estado_pedido)
        val imagenEstadoPedido=vistaItem.findViewById<ImageView>(R.id.estado_foto_pedido)
        val idPedido=vistaItem.findViewById<TextView>(R.id.id_pedido)
    }

    override fun onCreateViewHolder(grupoPadre: ViewGroup, tipoVista: Int): PedidoViewHolder {
        val vistaItem =
            LayoutInflater.from(grupoPadre.context).inflate(R.layout.item_pedido, grupoPadre, false)
        contexto = grupoPadre.context
        return PedidoViewHolder(vistaItem)
    }

    override fun onBindViewHolder(portadorVista: PedidoViewHolder, posicion: Int) {
        val pedidoActual=listaFiltrada[posicion]
        portadorVista.nombrePedido.text="Nombre carta: "+pedidoActual.nombre
        portadorVista.precioPedido.text="Precio: "+pedidoActual.precio+" €"
        portadorVista.estadoPedido.text="Estado: "+pedidoActual.estado
        portadorVista.idPedido.text=pedidoActual.id

        var preferenciasCompartidas = PreferenceManager.getDefaultSharedPreferences(contexto)
        var tipoUsuario = preferenciasCompartidas.getString("tipo", "cliente")

        if(tipoUsuario.equals("cliente") && pedidoActual.estado=="pendiente") {
            portadorVista.imagenEstadoPedido.setImageResource(R.drawable.baseline_access_time_filled_24)
        }else if(tipoUsuario.equals("admin") && pedidoActual.estado=="pendiente") {
            portadorVista.imagenEstadoPedido.setImageResource(R.drawable.baseline_check_circle_24)
            portadorVista.imagenEstadoPedido.setOnClickListener {
                val idAndroid= Settings.Secure.getString(
                    contexto.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                var pedido=Pedido(
                    pedidoActual.id,
                    pedidoActual.id_cliente,
                    pedidoActual.id_producto,
                    "confirmada",
                    pedidoActual.precio,
                    pedidoActual.nombre,
                    EstadoNoti.modificado,
                    idAndroid
                )
                val baseDatos = FirebaseDatabase.getInstance().reference
                Utilidades.crearPedido(baseDatos,pedido)
                Toast.makeText(contexto, "Pedido confirmado", Toast.LENGTH_SHORT).show()
            }
        }else{
            portadorVista.imagenEstadoPedido.setImageResource(R.drawable.baseline_check_circle_24)
        }
    }

    override fun getItemCount(): Int = listaFiltrada.size

    override fun getFilter(): Filter {
        TODO("Not implemented")
    }

}