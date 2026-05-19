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
import androidx.navigation.fragment.findNavController
import com.ifpr.ifsolidarioapp.R

class DoacaoFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var campanha_id: String = ""

    private var criadorId: String = ""
    private var campanha_nome: String = ""
    private var quantidade_atual: String = ""
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

        criadorId = arguments?.getString("criadorId") ?: ""

        campanha_id = arguments?.getString("campanha_id") ?: ""
        campanha_nome = arguments?.getString("campanha_nome") ?: ""
        quantidade_atual = arguments?.getString("quantidade_atual") ?: ""

        if (campanha_id.isNotEmpty()) {
            binding.buttonAdicionar.visibility = View.GONE
        }

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

        val bundle = Bundle().apply {

            putString("campanha_id", campanha_id)
            putString("campanha_nome", campanha_nome)
            putString("categoria_campanha", arguments?.getString("categoria_campanha"))
            putString("criadorId", criadorId)
            putString("quantidade_atual", quantidade_atual)
        }

        novoFragment.arguments = bundle

        listaFragments.add(novoFragment)

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

        var quantidadeDoada = 0.0

        for (item in lista) {

            quantidadeDoada += item.quantidade

            database
                .child("doacoes")
                .child(uid)
                .push()
                .setValue(item)
        }

        atualizarQuantidadeCampanha(quantidadeDoada)
    }

    private fun atualizarQuantidadeCampanha(
        quantidadeDoada: Double
    ) {

        if (
            campanha_id.isEmpty() ||
            criadorId.isEmpty()
        ) return

        val campanhaRef = database
            .child("campanhas")
            .child(criadorId)
            .child(campanha_id)

        campanhaRef
            .child("quantidade_atual")
            .get()
            .addOnSuccessListener { snapshot ->

                val atual =
                    snapshot.getValue(Double::class.java) ?: 0.0

                val novaQuantidade =
                    atual + quantidadeDoada

                campanhaRef
                    .child("quantidade_atual")
                    .setValue(novaQuantidade)
                    .addOnSuccessListener {

                        findNavController().navigate(
                            R.id.navigation_home
                        )
                    }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}