package com.ifpr.ifsolidarioapp.ui.usuario

data class HistoricoDoacao(
    var campanha_nome: String = "",
    var categoria: String = "",
    var quantidade: Int = 0,
    var imagens: List<String> = emptyList(),
    var dataDoacao: Long = 0L
)