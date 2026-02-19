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

class DoacaoFragment : Fragment() {

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

        adicionarFragment()

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
            .add(R.id.container_fragments, novoFragment, "fragment_${listaFragments.size}")

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

        for (item in lista) {
            println("Categoria: ${item.categoria}")
            println("Quantidade: ${item.quantidade}")
        }

        // Aqui depois você integra Firebase
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
