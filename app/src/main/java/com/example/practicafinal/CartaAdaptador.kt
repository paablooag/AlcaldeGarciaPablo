package com.example.practicafinal

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.practicafinal.ui.home.HomeFragment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class CartaAdaptador(private val lista:MutableList<Carta>): RecyclerView.Adapter<CartaAdaptador.CartaViewHolder>(),
    Filterable {

    private lateinit var context: Context
    private var filter_list=lista
    class CartaViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val foto=item.findViewById<ImageView>(R.id.photo_item)
        val nombre=item.findViewById<TextView>(R.id.name_item)
        val precio=item.findViewById<TextView>(R.id.precio_item)
        val categoria=item.findViewById<TextView>(R.id.categoria_item)
        val stock=item.findViewById<TextView>(R.id.stock_item)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartaViewHolder {
        val item_view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_carta, parent, false)
        context = parent.context
        return CartaViewHolder(item_view)
    }

    override fun onBindViewHolder(holder: CartaViewHolder, position: Int) {
        val actual_item=filter_list[position]
        holder.nombre.text=actual_item.nombre
        holder.categoria.text="Categoria: "+actual_item.categoria
        holder.precio.text="Precio: "+actual_item.precio
        holder.stock.text="Stock: "+actual_item.stock

        val URL:String? = when (actual_item.imagen){
            ""->null
            else->actual_item.imagen
        }

        holder.itemView.setOnLongClickListener {
            val popup = PopupMenu(context,holder.itemView)

            popup.inflate(R.menu.carta_op_menu)

            popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editar->{
                        var newIntent= Intent(context, EditarCarta::class.java)
                        newIntent.putExtra("carta",actual_item)
                        context.startActivity(newIntent)
                        true
                    }
                    R.id.eliminar->{
                        val db_ref = FirebaseDatabase.getInstance().getReference()
                        val sto_ref = FirebaseStorage.getInstance().getReference()

                        filter_list.remove(actual_item)
                            sto_ref.child("Cartas").child("photos").child(actual_item.id!!).delete()
                            db_ref.child("Cartas").child(actual_item.id!!).removeValue()
                            Toast.makeText(context,"Carta borrada con exito", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else->false
                }
            }
            popup.show()
            true
        }

        Glide.with(context).load(URL).apply(Utilidades.glideOptions(context)).transition(Utilidades.transition).into(holder.foto)
    }

    override fun getItemCount(): Int = filter_list.size

    override fun getFilter(): Filter {
        return  object : Filter(){
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val search = p0.toString().lowercase()

                if (search.isEmpty()){
                    filter_list = lista
                }else {
                    filter_list = (lista.filter {
                        if (it.nombre.toString().lowercase().contains(search)){
                            it.nombre.toString().lowercase().contains(search)
                        }else{
                            it.categoria.toString().lowercase().contains(search)
                        }
                    }) as MutableList<Carta>
                }

                val filterResults = FilterResults()
                filterResults.values = filter_list
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                notifyDataSetChanged()
            }

        }
    }
}