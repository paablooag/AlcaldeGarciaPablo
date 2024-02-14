package com.example.practicafinal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UsuarioAdaptador(private var lista:MutableList<Usuario>) : RecyclerView.Adapter<UsuarioAdaptador.UsuarioViewHolder>(),
    Filterable {

    private lateinit var context: Context
    private var filter_list=lista

    class UsuarioViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val foto=item.findViewById<ImageView>(R.id.img_usuario)
        val nombre=item.findViewById<TextView>(R.id.nombre_usuario)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val item_view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        context = parent.context
        return UsuarioViewHolder(item_view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {

        val actual_item=filter_list[position]
        holder.nombre.text=actual_item.nombre

        val URL:String? = when (actual_item.img){
            ""->null
            else->actual_item.img
        }

        Glide.with(context).load(URL).apply(Utilidades.glideOptions(context)).transition(Utilidades.transition).into(holder.foto)
    }

    override fun getItemCount(): Int = filter_list.size

    override fun getFilter(): Filter {
        TODO("Not implemented")
    }
}