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

class PedidosAdaptador(private val lista:MutableList<Pedido>) : RecyclerView.Adapter<PedidosAdaptador.PedidoViewHolder>(),
    Filterable {

    private lateinit var context: Context
    private var filter_list=lista
    class PedidoViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val nombre=item.findViewById<TextView>(R.id.name_item_pedido)
        val precio=item.findViewById<TextView>(R.id.precio_item_pedido)
        val estado=item.findViewById<TextView>(R.id.estado_pedido)
        val estadoFoto=item.findViewById<ImageView>(R.id.estado_foto_pedido)
        val id=item.findViewById<TextView>(R.id.id_pedido)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val item_view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        context = parent.context
        return PedidoViewHolder(item_view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val actual_item=filter_list[position]
        holder.nombre.text="Nombre carta: "+actual_item.nombre
        holder.precio.text="Precio: "+actual_item.precio+" €"
        holder.estado.text="Estado: "+actual_item.estado
        holder.id.text=actual_item.id

        var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var tipo = sharedPreferences.getString("tipo", "cliente")

        if(tipo.equals("cliente") && actual_item.estado=="pendiente") {
            holder.estadoFoto.setImageResource(R.drawable.baseline_access_time_filled_24)
        }else if(tipo.equals("admin") && actual_item.estado=="pendiente") {
            holder.estadoFoto.setImageResource(R.drawable.baseline_check_circle_24)
            holder.estadoFoto.setOnClickListener {
                val androidId= Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                var pedido=Pedido(
                    actual_item.id,
                    actual_item.id_cliente,
                    actual_item.id_producto,
                    "confirmada",
                    actual_item.precio,
                    actual_item.nombre,
                    Estado_not.modificado,
                    androidId
                )
                val database = FirebaseDatabase.getInstance().reference
                Utilidades.crearPedido(database,pedido)
                Toast.makeText(context, "Pedido confirmado", Toast.LENGTH_SHORT).show()
            }
        }else{
            holder.estadoFoto.setImageResource(R.drawable.baseline_check_circle_24)
        }
    }

    override fun getItemCount(): Int = filter_list.size

    override fun getFilter(): Filter {
        TODO("Not implemented")
    }

}