package com.example.practicafinal

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
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
import com.example.practicafinal.activities.administrador.EditEventos
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EventoAdaptador(private var lista:MutableList<Evento>) : RecyclerView.Adapter<EventoAdaptador.EventoViewHolder>(),
    Filterable {

    private lateinit var context: Context
    private var filter_list=lista
    class EventoViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val foto=item.findViewById<ImageView>(R.id.photo_item_evento)
        val nombre=item.findViewById<TextView>(R.id.name_item_evento)
        val precio=item.findViewById<TextView>(R.id.precio_item_evento)
        val fecha=item.findViewById<TextView>(R.id.fecha_item_evento)
        val aforo=item.findViewById<TextView>(R.id.aforo_item_evento)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val item_view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        context = parent.context
        return EventoViewHolder(item_view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val actual_item=filter_list[position]
        holder.nombre.text=actual_item.nombre
        holder.fecha.text="Fecha: "+actual_item.fecha
        holder.precio.text="Precio: "+actual_item.precio
        holder.aforo.text="Aforo: "+actual_item.aforo_maximo

        val URL:String? = when (actual_item.imagen){
            ""->null
            else->actual_item.imagen
        }

        var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var tipo = sharedPreferences.getString("tipo", "cliente")

        if (tipo.equals("cliente",true)){
            holder.itemView.setOnLongClickListener {
                false
            }
        }else {

            holder.itemView.setOnLongClickListener {
                val popup = PopupMenu(context, holder.itemView)

                popup.inflate(R.menu.carta_op_menu)

                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.editar -> {
                            var newIntent = Intent(context, EditEventos::class.java)
                            newIntent.putExtra("evento", actual_item)
                            context.startActivity(newIntent)
                            true
                        }

                        R.id.eliminar -> {
                            val db_ref = FirebaseDatabase.getInstance().getReference()
                            val sto_ref = FirebaseStorage.getInstance().getReference()

                            filter_list.remove(actual_item)
                            sto_ref.child("Eventos").child("photos").child(actual_item.id!!)
                                .delete()
                            db_ref.child("Eventos").child(actual_item.id!!).removeValue()
                            Toast.makeText(context, "Evento borrado con exito", Toast.LENGTH_SHORT)
                                .show()
                            true
                        }

                        else -> false
                    }
                }
                try {
                    val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                    fieldMPopup.isAccessible = true
                    val mPopup = fieldMPopup.get(popup)
                    mPopup.javaClass
                        .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(mPopup, true)
                } catch (e: Exception) {
                    Log.e("Main", "Error showing menu icons.", e)
                } finally {
                    popup.show()
                }
//            popup.show()
                true
            }
        }

        Glide.with(context).load(URL).apply(Utilidades.glideOptions(context)).transition(Utilidades.transition).into(holder.foto)
    }

    override fun getItemCount(): Int = filter_list.size

    override fun getFilter(): Filter {
        TODO("Not implemented")
    }
}