package com.ifpr.ifsolidarioapp.ui.conquista

import android.content.Context
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Conquista

object ConquistasManager {

    private val filaConquistas       = mutableListOf<Conquista>()
    private val conquistasPendentes  = mutableSetOf<String>()
    private var verificandoConquistas = false
    private var exibindoConquista     = false
    private var desbloqueouNovaConquista = false

    // ── Nova função usada pela ConquistaActivity ──────────────────────────────
    /** Retorna true se ainda há conquistas na fila esperando para ser exibidas */
    fun temConquistasPendentes(): Boolean = filaConquistas.isNotEmpty()

    // ─────────────────────────────────────────────────────────────────────────

    fun verificarConquistas(
        context: Context,
        uid: String,
        alimentos: Int,
        roupas: Int,
        brinquedos: Int,
        total: Int,
        callback: (Boolean) -> Unit
    ) {
        if (verificandoConquistas) return
        verificandoConquistas = true

        filaConquistas.clear()
        conquistasPendentes.clear()
        desbloqueouNovaConquista = false

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

                conquistaJaDesbloqueada(uid, conquista.key) { jaDesbloqueada ->

                    if (!jaDesbloqueada) {

                        desbloquearConquista(uid, conquista) { sucesso ->

                            if (sucesso && conquistasPendentes.add(conquista.key)) {
                                desbloqueouNovaConquista = true
                                filaConquistas.add(conquista)
                            }

                            verificacoesRestantes--
                            if (verificacoesRestantes == 0) {
                                verificandoConquistas = false
                                callback(desbloqueouNovaConquista)
                                // Só começa a exibir após o callback —
                                // assim o DoacaoFragment sabe se vai para
                                // Ranking (sem conquista) antes da Activity abrir
                                if (desbloqueouNovaConquista) {
                                    exibirProximaConquista(context)
                                }
                            }
                        }

                    } else {
                        verificacoesRestantes--
                        if (verificacoesRestantes == 0) {
                            verificandoConquistas = false
                            callback(desbloqueouNovaConquista)
                        }
                    }
                }

            } else {
                verificacoesRestantes--
                if (verificacoesRestantes == 0) {
                    verificandoConquistas = false
                    callback(desbloqueouNovaConquista)
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    fun conquistaFinalizada(context: Context) {
        exibindoConquista = false
        // Se há mais na fila, exibe a próxima
        if (filaConquistas.isNotEmpty()) {
            exibirProximaConquista(context)
        }
    }

    private fun exibirProximaConquista(context: Context) {
        if (exibindoConquista || filaConquistas.isEmpty()) return
        exibindoConquista = true

        val conquista = filaConquistas.removeAt(0)
        conquistasPendentes.remove(conquista.key)
        mostrarConquista(context, conquista)
    }

    private fun mostrarConquista(context: Context, conquista: Conquista) {
        val intent = Intent(context, ConquistaActivity::class.java).apply {
            putExtra("mensagem",         "Parabéns, você conquistou ${conquista.titulo}!")
            putExtra("lottieResName",    conquista.lottieAnimation)
            putExtra("insigniaDrawable", conquista.insigniaHabilitada)
            putExtra("tempoDuracao",     5000L)
            // FLAG_ACTIVITY_NEW_TASK necessário pois o context pode ser
            // de um Fragment/Application e não de uma Activity
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ─────────────────────────────────────────────────────────────────────────

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
            .addOnSuccessListener { callback(it.exists()) }
            .addOnFailureListener { callback(false) }
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
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lista de conquistas
    // ─────────────────────────────────────────────────────────────────────────

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