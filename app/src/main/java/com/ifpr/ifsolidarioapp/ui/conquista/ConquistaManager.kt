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


    fun verificarConquistas(
        context: Context,
        uid: String,
        alimentos: Int,
        roupas: Int,
        brinquedos: Int,
        total: Int
    ) {
        if (verificandoConquistas) {
            return
        }

        verificandoConquistas = true

        filaConquistas.clear()
        conquistasPendentes.clear()

        var verificacoesRestantes = listaConquistas.size

        listaConquistas.forEach { conquista ->

            val progresso = when (conquista.categoria) {
                "ALIMENTO"  -> alimentos
                "ROUPA"     -> roupas
                "BRINQUEDO" -> brinquedos
                "TOTAL"     -> total
                else        -> 0
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
                                    finalizarVerificacao(context)
                                }

                                return@desbloquearConquista
                            }

                            if (conquistasPendentes.add(conquista.key)) {

                                filaConquistas.add(conquista)

                            }

                            verificacoesRestantes--

                            if (verificacoesRestantes == 0) {
                                finalizarVerificacao(context)
                            }
                        }

                    } else {

                        verificacoesRestantes--

                        if (verificacoesRestantes == 0) {
                            finalizarVerificacao(context)
                        }
                    }
                }

            } else {

                verificacoesRestantes--

                if (verificacoesRestantes == 0) {
                    finalizarVerificacao(context)
                }
            }
        }
    }

    private fun finalizarVerificacao(context: Context) {

        verificandoConquistas = false

        if (filaConquistas.isEmpty()) {

            abrirRanking(context)

        } else {

            exibirProximaConquista(context)

        }
    }

    private fun abrirRanking(context: Context) {

        val intent = Intent(
            context,
            MainActivity::class.java
        )

        intent.putExtra(
            "abrirRanking",
            true
        )

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )

        context.startActivity(intent)
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
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )

        context.startActivity(intent)
    }

    fun conquistaFinalizada(context: Context) {

        exibindoConquista = false

        if (filaConquistas.isNotEmpty()) {

            exibirProximaConquista(context)

            return
        }

        verificandoConquistas = false

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
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )

        context.startActivity(intent)
    }

    private val listaConquistas = listOf(

        Conquista(
            key                 = "primeira_doacao",
            titulo              = "Primeira Doação",
            descricao           = "Realizou sua primeira doação",
            meta                = 1,
            categoria           = "TOTAL",
            insigniaHabilitada  = R.drawable.ic_primeira_doacao_enabled,
            insigniaDesabilitada = R.drawable.ic_primeira_doacao_disabled,
            lottieAnimation     = "success_congrats"
        ),

        // ALIMENTOS
        Conquista("alimento_5",  "5 Alimentos",  "Doe 5 alimentos",  5,  "ALIMENTO",
            R.drawable.ic_alimento_5_enabled,  R.drawable.ic_alimento_5_disabled,  "success_congrats"),
        Conquista("alimento_10", "10 Alimentos", "Doe 10 alimentos", 10, "ALIMENTO",
            R.drawable.ic_alimento_10_enabled, R.drawable.ic_alimento_10_disabled, "success_congrats"),
        Conquista("alimento_20", "20 Alimentos", "Doe 20 alimentos", 20, "ALIMENTO",
            R.drawable.ic_alimento_20_enabled, R.drawable.ic_alimento_20_disabled, "success_congrats"),
        Conquista("alimento_40", "40 Alimentos", "Doe 40 alimentos", 40, "ALIMENTO",
            R.drawable.ic_alimento_40_enabled, R.drawable.ic_alimento_40_disabled, "success_congrats"),

        // BRINQUEDOS
        Conquista("brinquedo_5",  "5 Brinquedos",  "Doe 5 brinquedos",  5,  "BRINQUEDO",
            R.drawable.ic_brinquedo_5_enabled,  R.drawable.ic_brinquedo_5_disabled,  "success_congrats"),
        Conquista("brinquedo_10", "10 Brinquedos", "Doe 10 brinquedos", 10, "BRINQUEDO",
            R.drawable.ic_brinquedo_10_enabled, R.drawable.ic_brinquedo_10_disabled, "success_congrats"),
        Conquista("brinquedo_20", "20 Brinquedos", "Doe 20 brinquedos", 20, "BRINQUEDO",
            R.drawable.ic_brinquedo_20_enabled, R.drawable.ic_brinquedo_20_disabled, "success_congrats"),
        Conquista("brinquedo_40", "40 Brinquedos", "Doe 40 brinquedos", 40, "BRINQUEDO",
            R.drawable.ic_brinquedo_40_enabled, R.drawable.ic_brinquedo_40_disabled, "success_congrats"),

        // ROUPAS
        Conquista("roupa_5",  "5 Roupas",  "Doe 5 roupas",  5,  "ROUPA",
            R.drawable.ic_roupa_5_enabled,  R.drawable.ic_roupa_5_disabled,  "success_congrats"),
        Conquista("roupa_10", "10 Roupas", "Doe 10 roupas", 10, "ROUPA",
            R.drawable.ic_roupa_10_enabled, R.drawable.ic_roupa_10_disabled, "success_congrats"),
        Conquista("roupa_20", "20 Roupas", "Doe 20 roupas", 20, "ROUPA",
            R.drawable.ic_roupa_20_enabled, R.drawable.ic_roupa_20_disabled, "success_congrats"),
        Conquista("roupa_40", "40 Roupas", "Doe 40 roupas", 40, "ROUPA",
            R.drawable.ic_roupa_40_enabled, R.drawable.ic_roupa_40_disabled, "success_congrats")
    )
}
