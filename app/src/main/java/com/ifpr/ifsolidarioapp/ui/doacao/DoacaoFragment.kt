package com.ifpr.ifsolidarioapp.ui.doacao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupClicks()

        if (savedInstanceState == null) {
            adicionarFragment()
        }

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

        listaFragments.add(novoFragment) // 🔥 ESSENCIAL

        childFragmentManager.beginTransaction()
            .add(binding.containerFragments.id, novoFragment)
            .commit()
    }

    private fun finalizarDoacao() {

        val listaDoacoes = mutableListOf<DoacaoData>()

        for (fragment in listaFragments) {

            if (fragment.isAdded && fragment.view != null) {

                val dados = fragment.obterDados()

                if (dados != null) {
                    listaDoacoes.add(dados)
                }
            }
        }

        salvarNoBanco(listaDoacoes)
    }

    private fun salvarNoBanco(lista: List<DoacaoData>) {

        val uid = auth.currentUser?.uid ?: return

        for (item in lista) {

            database
                .child("doacoes")
                .child(uid)
                .push()
                .setValue(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}