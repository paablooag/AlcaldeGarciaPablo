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
import com.example.practicafinal.actividades.administrador.EditarEvento
import com.example.practicafinal.actividades.administrador.EventoInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EventoAdaptador(private var listaEventos:MutableList<Evento>,private var listaInscripciones:MutableList<Inscripcion>?=null) : RecyclerView.Adapter<EventoAdaptador.EventoViewHolder>(),
    Filterable {

    private lateinit var contexto: Context
    private var listaFiltrada=listaEventos

    class EventoViewHolder(vistaItem: View) : RecyclerView.ViewHolder(vistaItem) {
        val fotoEvento=vistaItem.findViewById<ImageView>(R.id.photo_item_evento)
        val nombreEvento=vistaItem.findViewById<TextView>(R.id.name_item_evento)
        val precioEvento=vistaItem.findViewById<TextView>(R.id.precio_item_evento)
        val fechaEvento=vistaItem.findViewById<TextView>(R.id.fecha_item_evento)
        val aforoMaxEvento=vistaItem.findViewById<TextView>(R.id.aforo_item_evento)
        val aforoActEvento=vistaItem.findViewById<TextView>(R.id.aforo_actual_item_evento)
        val botonApuntarse=vistaItem.findViewById<ImageView>(R.id.estado_evento)
    }
    override fun onCreateViewHolder(grupoPadre: ViewGroup, tipoVista: Int): EventoViewHolder {
        val vistaItem =
            LayoutInflater.from(grupoPadre.context).inflate(R.layout.item_evento, grupoPadre, false)
        contexto = grupoPadre.context
        return EventoViewHolder(vistaItem)
    }

    override fun onBindViewHolder(portadorVista: EventoViewHolder, posicion: Int) {
        val eventoActual=listaFiltrada[posicion]
        var inscripcion:Inscripcion?=Inscripcion()
        if (listaInscripciones!=null){
            inscripcion=listaInscripciones!!.find { it.id_evento.equals(eventoActual.id) }
        }
        portadorVista.nombreEvento.text=eventoActual.nombre
        portadorVista.fechaEvento.text="Fecha: "+eventoActual.fecha
        portadorVista.precioEvento.text="Precio: "+eventoActual.precio
        portadorVista.aforoMaxEvento.text="Aforo Máximo: "+eventoActual.aforo_maximo
        portadorVista.aforoActEvento.text="Aforo Actual: "+eventoActual.aforo

        val URL:String? = when (eventoActual.imagen){
            ""->null
            else->eventoActual.imagen
        }

        var preferenciasCompartidas = PreferenceManager.getDefaultSharedPreferences(contexto)
        var tipoUsuario = preferenciasCompartidas.getString("tipo", "cliente")

        var apuntado=false
        if (tipoUsuario.equals("cliente",true)) {
            portadorVista.itemView.setOnLongClickListener {
                false
            }

            if (inscripcion!=null && inscripcion.id_evento.equals(eventoActual.id,true) && inscripcion.id_ususario.equals(
                    FirebaseAuth.getInstance().currentUser!!.uid,true
                )
            ){
                portadorVista.botonApuntarse.setImageResource(R.drawable.baseline_access_time_filled_24)
            }else {
                portadorVista.botonApuntarse.setOnClickListener {
                    var referenciaBaseDatos = FirebaseDatabase.getInstance().reference
                    var id = referenciaBaseDatos.push().key
                    val inscripcion =
                        Inscripcion(id!!,eventoActual.id, FirebaseAuth.getInstance().currentUser!!.uid)
                    referenciaBaseDatos.child("Inscripciones").child(id!!).setValue(inscripcion)
                    eventoActual.aforo = (eventoActual.aforo.toInt() + 1).toString()
                    referenciaBaseDatos.child("Eventos").child(eventoActual.id).setValue(eventoActual)

                    portadorVista.botonApuntarse.isClickable = false

                }
            }

        }else {

            portadorVista.botonApuntarse.visibility=View.GONE

            portadorVista.itemView.setOnClickListener {
                var nuevoIntento=Intent(contexto, EventoInfo::class.java)
                nuevoIntento.putExtra("evento",eventoActual)
                contexto.startActivity(nuevoIntento)
            }

            portadorVista.itemView.setOnLongClickListener {
                val menuPopup = PopupMenu(contexto, portadorVista.itemView)

                menuPopup.inflate(R.menu.carta_op_menu)

                menuPopup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.editar -> {
                            var nuevoIntento = Intent(contexto, EditarEvento::class.java)
                            nuevoIntento.putExtra("evento", eventoActual)
                            contexto.startActivity(nuevoIntento)
                            true
                        }

                        R.id.eliminar -> {
                            val referenciaBaseDatos = FirebaseDatabase.getInstance().getReference()
                            val referenciaAlmacenamiento = FirebaseStorage.getInstance().getReference()

                            listaFiltrada.remove(eventoActual)
                            referenciaAlmacenamiento.child("Eventos").child("photos").child(eventoActual.id!!)
                                .delete()
                            referenciaBaseDatos.child("Eventos").child(eventoActual.id!!).removeValue()

                            if (listaInscripciones!=null){
                                listaInscripciones!!.remove(inscripcion)
                                referenciaBaseDatos.child("Inscripciones").child(inscripcion!!.id).removeValue()
                            }

                            Toast.makeText(contexto, "Evento borrado con exito", Toast.LENGTH_SHORT)
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

        Glide.with(contexto).load(URL).apply(Utilidades.glideOptions(contexto)).transition(Utilidades.transition).into(portadorVista.fotoEvento)
    }

    override fun getItemCount(): Int = listaFiltrada.size

    override fun getFilter(): Filter {
        TODO("Not implemented")
    }
}