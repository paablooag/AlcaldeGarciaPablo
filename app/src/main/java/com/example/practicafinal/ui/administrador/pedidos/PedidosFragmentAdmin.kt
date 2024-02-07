package com.example.practicafinal.ui.administrador.pedidos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.example.practicafinal.R
import com.example.practicafinal.activities.Autor
import com.example.practicafinal.activities.MainActivity
import com.example.practicafinal.databinding.FragmentPedidosAdminBinding
import com.google.firebase.auth.FirebaseAuth

class PedidosFragmentAdmin : Fragment(){

    private var _binding: FragmentPedidosAdminBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPedidosAdminBinding.inflate(inflater, container, false)

        var user = FirebaseAuth.getInstance()

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

        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}