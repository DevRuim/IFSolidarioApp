package com.ifpr.ifsolidarioapp.ui.usuario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.ifpr.ifsolidarioapp.R

class HistoricoFragment : Fragment() {

    private lateinit var recycler: RecyclerView

    private val lista =
        mutableListOf<HistoricoDoacao>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view =
            inflater.inflate(
                R.layout.fragment_historico,
                container,
                false
            )


        recycler =
            view.findViewById(R.id.recyclerHistorico)

        recycler.layoutManager =
            LinearLayoutManager(context)

        carregarHistorico()

        view.findViewById<FrameLayout>(R.id.btnVoltar)
            .setOnClickListener {

                findNavController().navigateUp()

            }

        return view
    }

    private fun carregarHistorico() {

        val uid =
            FirebaseAuth.getInstance()
                .currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("doacoes")
            .child(uid)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        lista.clear()

                        for (doacaoSnapshot in snapshot.children) {

                            val doacao =
                                doacaoSnapshot.getValue(
                                    HistoricoDoacao::class.java
                                )

                            if (doacao != null) {

                                lista.add(doacao)
                            }
                        }

                        recycler.adapter =
                            HistoricoAdapter(lista)
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {}
                })
    }
}