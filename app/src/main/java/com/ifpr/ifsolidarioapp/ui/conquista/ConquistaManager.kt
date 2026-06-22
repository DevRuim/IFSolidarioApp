package com.ifpr.ifsolidarioapp.ui.conquista

import android.content.Context
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.MainActivity
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Conquista

object ConquistasManager {

    private val filaConquistas = mutableListOf<Conquista>()
    private var verificandoConquistas = false
    private val conquistasPendentes = mutableSetOf<String>()
    private var exibindoConquista = false
    private var abriuConquistaNestaDoacao = false


    fun verificarConquistas(
        context: Context,
        uid: String,
        alimentos: Int,
        roupas: Int,
        brinquedos: Int,
        total: Int
    ) {
        abriuConquistaNestaDoacao = false

        if (verificandoConquistas) return

        verificandoConquistas = true

        filaConquistas.clear()
        conquistasPendentes.clear()

        var verificacoesRestantes = listaConquistas.size

        listaConquistas.forEach { conquista ->

            val progresso = when (conquista.categoria) {
                "ALIMENTO" -> alimentos
                "ROUPA" -> roupas
                "BRINQUEDO" -> brinquedos
                "TOTAL" -> total
                else -> 0
            }

            if (progresso >= conquista.meta) {

                conquistaJaDesbloqueada(
                    uid,
                    conquista.key
                ) { desbloqueada ->

                    if (!desbloqueada) {

                        desbloquearConquista(
                            uid,
                            conquista
                        ) { sucesso ->

                            if (!sucesso) {
                                verificacoesRestantes--

                                if (verificacoesRestantes == 0) {
                                    verificandoConquistas = false
                                    exibirProximaConquista(context)
                                }

                                return@desbloquearConquista
                            }

                            if (conquistasPendentes.add(conquista.key)) {
                                filaConquistas.add(conquista)
                                abriuConquistaNestaDoacao = true
                            }

                            verificacoesRestantes--

                            if (verificacoesRestantes == 0) {

                                verificandoConquistas = false

                                exibirProximaConquista(context)
                            }
                        }

                    } else {

                        verificacoesRestantes--

                        if (verificacoesRestantes == 0) {

                            verificandoConquistas = false

                            exibirProximaConquista(context)
                        }
                    }
                }

            } else {

                verificacoesRestantes--

                if (verificacoesRestantes == 0) {

                    verificandoConquistas = false

                    exibirProximaConquista(context)
                }
            }
        }
    }

    private fun exibirProximaConquista(
        context: Context
    ) {

        if (exibindoConquista) {
            return
        }

        if (filaConquistas.isEmpty()) {
            return
        }

        exibindoConquista = true

        val conquista =
            filaConquistas.removeAt(0)

        conquistasPendentes.remove(
            conquista.key
        )

        mostrarConquista(
            context,
            conquista
        )
    }

    private fun conquistaJaDesbloqueada(
        uid: String,
        conquistaId: String,
        callback: (Boolean) -> Unit
    ) {

        FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(uid)
            .child("conquistas_desbloqueadas")
            .child(conquistaId)
            .get()
            .addOnSuccessListener {
                callback(it.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    private fun desbloquearConquista(
        uid: String,
        conquista: Conquista,
        callback: (Boolean) -> Unit
    ) {


        FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(uid)
            .child("conquistas_desbloqueadas")
            .child(conquista.key)
            .setValue(true)

            .addOnSuccessListener {
                callback(true)
            }

            .addOnFailureListener {
                callback(false)
            }
    }

    private fun mostrarConquista(
        context: Context,
        conquista: Conquista
    ) {

        val intent = Intent(
            context,
            ConquistaActivity::class.java
        )

        intent.putExtra(
            "mensagem",
            "Parabéns, você conquistou ${conquista.titulo}!"
        )

        intent.putExtra(
            "lottieResName",
            conquista.lottieAnimation
        )

        intent.putExtra(
            "insigniaDrawable",
            conquista.insigniaHabilitada
        )

        intent.putExtra(
            "tempoDuracao",
            5000L
        )

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )

        context.startActivity(intent)
    }

    fun conquistaFinalizada(
        context: Context
    ) {

        exibindoConquista = false

        if (filaConquistas.isEmpty()) {

            verificandoConquistas = false

            if (abriuConquistaNestaDoacao) {

                val intent = Intent(
                    context,
                    MainActivity::class.java
                )

                intent.putExtra(
                    "abrirConquistas",
                    true
                )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                )

                context.startActivity(intent)

                abriuConquistaNestaDoacao = false
            }

            return
        }

        exibirProximaConquista(context)
    }

    private val listaConquistas = listOf(

        Conquista(
            key = "primeira_doacao",
            titulo = "Primeira Doação",
            descricao = "Realizou sua primeira doação",
            meta = 1,
            categoria = "TOTAL",
            insigniaHabilitada = R.drawable.ic_primeira_doacao_enabled,
            insigniaDesabilitada = R.drawable.ic_primeira_doacao_disabled,
            lottieAnimation = "success_congrats"
        ),

        // ALIMENTOS

        Conquista(
            key = "alimento_5",
            titulo = "5 Alimentos",
            descricao = "Doe 5 alimentos",
            meta = 5,
            categoria = "ALIMENTO",
            insigniaHabilitada = R.drawable.ic_alimento_5_enabled,
            insigniaDesabilitada = R.drawable.ic_alimento_5_disabled,
            lottieAnimation = "success_congrats"
        ),

        Conquista(
            key = "alimento_10",
            titulo = "10 Alimentos",
            descricao = "Doe 10 alimentos",
            meta = 10,
            categoria = "ALIMENTO",
            insigniaHabilitada = R.drawable.ic_alimento_10_enabled,
            insigniaDesabilitada = R.drawable.ic_alimento_10_disabled,
            lottieAnimation = "success_congrats"
        ),

        Conquista(
            key = "alimento_20",
            titulo = "20 Alimentos",
            descricao = "Doe 20 alimentos",
            meta = 20,
            categoria = "ALIMENTO",
            insigniaHabilitada = R.drawable.ic_alimento_20_enabled,
            insigniaDesabilitada = R.drawable.ic_alimento_20_disabled,
            lottieAnimation = "success_congrats"
        ),

        Conquista(
            key = "alimento_40",
            titulo = "40 Alimentos",
            descricao = "Doe 40 alimentos",
            meta = 40,
            categoria = "ALIMENTO",
            insigniaHabilitada = R.drawable.ic_alimento_40_enabled,
            insigniaDesabilitada = R.drawable.ic_alimento_40_disabled,
            lottieAnimation = "success_congrats"
        ),

        // BRINQUEDOS

        Conquista(
            key = "brinquedo_5",
            titulo = "5 Brinquedos",
            descricao = "Doe 5 brinquedos",
            meta = 5,
            categoria = "BRINQUEDO",
            insigniaHabilitada = R.drawable.ic_brinquedo_5_enabled,
            insigniaDesabilitada = R.drawable.ic_brinquedo_5_disabled,
            lottieAnimation = "success_congrats"
        ),

        Conquista(
            key = "brinquedo_10",
            titulo = "10 Brinquedos",
            descricao = "Doe 10 brinquedos",
            meta = 10,
            categoria = "BRINQUEDO",
            insigniaHabilitada = R.drawable.ic_brinquedo_10_enabled,
            insigniaDesabilitada = R.drawable.ic_brinquedo_10_disabled,
            lottieAnimation = "success_congrats"
        ),

        Conquista(
            key = "brinquedo_20",
            titulo = "20 Brinquedos",
            descricao = "Doe 20 brinquedos",
            meta = 20,
            categoria = "BRINQUEDO",
            insigniaHabilitada = R.drawable.ic_brinquedo_20_enabled,
            insigniaDesabilitada = R.drawable.ic_brinquedo_20_disabled,
            lottieAnimation = "success_congrats"
        ),

        Conquista(
            key = "brinquedo_40",
            titulo = "40 Brinquedos",
            descricao = "Doe 40 brinquedos",
            meta = 40,
            categoria = "BRINQUEDO",
            insigniaHabilitada = R.drawable.ic_brinquedo_40_enabled,
            insigniaDesabilitada = R.drawable.ic_brinquedo_40_disabled,
            lottieAnimation = "success_congrats"
        ),

        // ROUPAS

        Conquista(
            key = "roupa_5",
            titulo = "5 Roupas",
            descricao = "Doe 5 roupas",
            meta = 5,
            categoria = "ROUPA",
            insigniaHabilitada = R.drawable.ic_roupa_5_enabled,
            insigniaDesabilitada = R.drawable.ic_roupa_5_disabled,
            lottieAnimation = "success_congrats"
        ),

        Conquista(
            key = "roupa_10",
            titulo = "10 Roupas",
            descricao = "Doe 10 roupas",
            meta = 10,
            categoria = "ROUPA",
            insigniaHabilitada = R.drawable.ic_roupa_10_enabled,
            insigniaDesabilitada = R.drawable.ic_roupa_10_disabled,
            lottieAnimation = "success_congrats"
        ),

        Conquista(
            key = "roupa_20",
            titulo = "20 Roupas",
            descricao = "Doe 20 roupas",
            meta = 20,
            categoria = "ROUPA",
            insigniaHabilitada = R.drawable.ic_roupa_20_enabled,
            insigniaDesabilitada = R.drawable.ic_roupa_20_disabled,
            lottieAnimation = "success_congrats"
        ),

        Conquista(
            key = "roupa_40",
            titulo = "40 Roupas",
            descricao = "Doe 40 roupas",
            meta = 40,
            categoria = "ROUPA",
            insigniaHabilitada = R.drawable.ic_roupa_40_enabled,
            insigniaDesabilitada = R.drawable.ic_roupa_40_disabled,
            lottieAnimation = "success_congrats"
        )
    )
}
