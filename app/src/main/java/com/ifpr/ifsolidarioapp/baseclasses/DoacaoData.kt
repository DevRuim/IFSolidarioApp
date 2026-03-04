package com.ifpr.ifsolidarioapp.baseclasses

data class DoacaoData(
    var categoria: String = "",
    var quantidade: Double = 0.0,
    var imagens: List<String> = emptyList()
)