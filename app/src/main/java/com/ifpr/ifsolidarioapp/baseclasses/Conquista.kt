package com.ifpr.ifsolidarioapp.baseclasses

data class Conquista(
    var key: String = "",
    val titulo: String,
    val descricao: String,
    val meta: Int,
    val categoria: String,
    val insigniaHabilitada: Int,
    val insigniaDesabilitada: Int,
    val lottieAnimation: String
)