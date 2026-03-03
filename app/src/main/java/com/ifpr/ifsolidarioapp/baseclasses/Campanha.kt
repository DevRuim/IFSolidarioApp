package com.ifpr.ifsolidarioapp.baseclasses

data class Campanha(

    var key: String? = null,
    var nomeCampanha: String = "",
    var endereco: String = "",
    var descricao: String = "",
    var meta: Double = 0.0,
    var criadorId: String = ""

)