package com.ifpr.ifsolidarioapp.ui.doacao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.DoacaoData
import com.ifpr.ifsolidarioapp.databinding.FragmentDoacaoBinding
import com.ifpr.ifsolidarioapp.ui.dashboard.DashboardFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth


class DoacaoFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentDoacaoBinding? = null
    private val binding get() = _binding!!

    private val listaFragments = mutableListOf<DashboardFragment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDoacaoBinding.inflate(inflater, container, false)

        setupClicks()

        if (savedInstanceState == null) {
            adicionarFragment()
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        return binding.root
    }

    private fun setupClicks() {

        binding.buttonAdicionar.setOnClickListener {
            adicionarFragment()
        }

        binding.buttonFinalizar.setOnClickListener {
            finalizarDoacao()
        }
    }

    private fun adicionarFragment() {

        val novoFragment = DashboardFragment()
        listaFragments.add(novoFragment)

        childFragmentManager.beginTransaction()
            .add(R.id.container_fragments, novoFragment)
            .commit()

    }

    private fun finalizarDoacao() {

        val listaDados = mutableListOf<DoacaoData>()

        for (fragment in listaFragments) {
            fragment.obterDados()?.let {
                listaDados.add(it)
            }
        }

        salvarNoBanco(listaDados)
    }

    private fun salvarNoBanco(lista: List<DoacaoData>) {

        val userId = auth.currentUser?.uid

        if (userId == null) {
            println("Usuário não logado")
            return
        }

        val doacoesRef = database
            .child("doacoes")
            .child(userId)

        for (item in lista) {

            val novaDoacaoRef = doacoesRef.push()

            val dadosMap = hashMapOf(
                "categoria" to item.categoria,
                "quantidade" to item.quantidade,
                "imagens" to item.imagens.map { it.toString() }
            )

            novaDoacaoRef.setValue(dadosMap)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
