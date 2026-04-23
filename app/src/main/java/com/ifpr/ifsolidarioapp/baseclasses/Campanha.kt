package com.ifpr.ifsolidarioapp.baseclasses

data class Campanha(

    var key: String = "",
    var nome_campanha: String = "",
    var endereco: String = "",
    var descricao: String = "",
    var meta: Double = 0.0,
    var quantidade_atual: Double = 0.0,
    var categoria_campanha: String = "",
    var criadorId: String = "",
    var criador_nome: String = "",
    var imagemBase64: String = ""


)