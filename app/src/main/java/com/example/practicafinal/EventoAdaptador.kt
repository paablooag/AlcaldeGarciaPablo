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
import com.example.practicafinal.actividades.administrador.EditEventos
import com.example.practicafinal.actividades.administrador.EventoInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EventoAdaptador(private var lista:MutableList<Evento>,private var inscripciones:MutableList<Inscripcion>?=null) : RecyclerView.Adapter<EventoAdaptador.EventoViewHolder>(),
    Filterable {

    private lateinit var context: Context
    private var filter_list=lista
    class EventoViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val foto=item.findViewById<ImageView>(R.id.photo_item_evento)
        val nombre=item.findViewById<TextView>(R.id.name_item_evento)
        val precio=item.findViewById<TextView>(R.id.precio_item_evento)
        val fecha=item.findViewById<TextView>(R.id.fecha_item_evento)
        val aforoMax=item.findViewById<TextView>(R.id.aforo_item_evento)
        val aforoAct=item.findViewById<TextView>(R.id.aforo_actual_item_evento)
        val apuntarse=item.findViewById<ImageView>(R.id.estado_evento)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val item_view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        context = parent.context
        return EventoViewHolder(item_view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val actual_item=filter_list[position]
        var inscripcion:Inscripcion?=Inscripcion()
        if (inscripciones!=null){
            inscripcion=inscripciones!!.find { it.id_evento.equals(actual_item.id) }
        }
        holder.nombre.text=actual_item.nombre
        holder.fecha.text="Fecha: "+actual_item.fecha
        holder.precio.text="Precio: "+actual_item.precio
        holder.aforoMax.text="Aforo Máximo: "+actual_item.aforo_maximo
        holder.aforoAct.text="Aforo Actual: "+actual_item.aforo

        val URL:String? = when (actual_item.imagen){
            ""->null
            else->actual_item.imagen
        }

        var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var tipo = sharedPreferences.getString("tipo", "cliente")

        if (tipo.equals("cliente",true)) {
            holder.itemView.setOnLongClickListener {
                false
            }

            if (inscripcion!=null && inscripcion.id_evento.equals(actual_item.id,true) && inscripcion.id_ususario.equals(
                    FirebaseAuth.getInstance().currentUser!!.uid,true
                )
            ){
                holder.apuntarse.setImageResource(R.drawable.baseline_check_circle_24)
        }else {
                holder.apuntarse.setOnClickListener {
                    var db_ref = FirebaseDatabase.getInstance().reference
                    var id = db_ref.push().key
                    val inscripcion =
                        Inscripcion(id!!,actual_item.id, FirebaseAuth.getInstance().currentUser!!.uid)
                    db_ref.child("Inscripciones").child(id!!).setValue(inscripcion)
                    actual_item.aforo = (actual_item.aforo.toInt() + 1).toString()
                    db_ref.child("Eventos").child(actual_item.id).setValue(actual_item)
                    holder.apuntarse.setImageResource(R.drawable.baseline_check_circle_24)
                }
            }


        }else {

            holder.apuntarse.visibility=View.GONE

            holder.itemView.setOnClickListener {
                var newIntent=Intent(context, EventoInfo::class.java)
                newIntent.putExtra("evento",actual_item)
                context.startActivity(newIntent)
            }

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

                            if (inscripciones!=null){
                                inscripciones!!.remove(inscripcion)
                                db_ref.child("Inscripciones").child(inscripcion!!.id).removeValue()
                            }

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