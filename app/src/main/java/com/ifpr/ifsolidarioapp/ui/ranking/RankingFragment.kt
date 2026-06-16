package com.ifpr.ifsolidarioapp.ui.ranking

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.ifpr.ifsolidarioapp.R
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.ifsolidarioapp.baseclasses.Usuario

class RankingFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RankingAdapter

    private val listaUsuarios = mutableListOf<Usuario>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.ranking_fragment,
            container,
            false
        )

        recycler = view.findViewById(R.id.recyclerRanking)

        recycler.layoutManager =
            LinearLayoutManager(requireContext())

        adapter = RankingAdapter(listaUsuarios)

        recycler.adapter = adapter

        carregarRanking()

        return view
    }

    private fun carregarRanking() {

        FirebaseDatabase
            .getInstance()
            .getReference("usuarios")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        listaUsuarios.clear()

                        for (item in snapshot.children) {

                            val usuario =
                                item.getValue(
                                    Usuario::class.java
                                )

                            if (usuario != null) {

                                listaUsuarios.add(usuario)
                            }
                        }

                        listaUsuarios.sortByDescending {
                            it.total_doacoes
                        }

                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {
                    }
                }
            )
    }
}