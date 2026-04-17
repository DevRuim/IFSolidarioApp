package com.ifpr.ifsolidarioapp.baseclasses

data class Campanha(

    var key: String = "",
    var nomeCampanha: String = "",
    var endereco: String = "",
    var descricao: String = "",
    var meta: Double = 0.0,
    var qunatidade_atual: Double = 0.0,
    var criadorId: String = "",
    var criadorNome: String = "",
    var imagemBase64: String = ""


)