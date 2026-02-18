package com.ifpr.ifsolidarioapp.ui.doacao

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.DoacaoData
import com.ifpr.ifsolidarioapp.ui.dashboard.DashboardFragment

class ActivityDoacao : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var botaoAdicionar: FloatingActionButton
    private lateinit var botaoFinalizar: Button

    private val listaFragments = mutableListOf<DashboardFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doacao)

        container = findViewById(R.id.container_fragments)
        botaoAdicionar = findViewById(R.id.button_adicionar)
        botaoFinalizar = findViewById(R.id.button_finalizar)

        botaoAdicionar.setOnClickListener {
            adicionarFragment()
        }

        botaoFinalizar.setOnClickListener {
            finalizarDoacao()
        }

        // começa com 1 fragment automaticamente
        adicionarFragment()
    }

    private fun adicionarFragment() {

        val novoFragment = DashboardFragment()
        listaFragments.add(novoFragment)

        supportFragmentManager.beginTransaction()
            .add(R.id.container_fragments, novoFragment)
            .commit()
    }

    private fun finalizarDoacao() {

        val listaDados = mutableListOf<DoacaoData>()

        for (fragment in listaFragments) {

            val dados = fragment.obterDados()

            if (dados != null) {
                listaDados.add(dados)
            }
        }

        salvarNoBanco(listaDados)
    }

    private fun salvarNoBanco(lista: List<DoacaoData>) {

        for (item in lista) {
            println("Categoria: ${item.categoria}")
            println("Quantidade: ${item.quantidade}")
        }

        // Aqui você coloca Room / Firebase depois
    }
}
