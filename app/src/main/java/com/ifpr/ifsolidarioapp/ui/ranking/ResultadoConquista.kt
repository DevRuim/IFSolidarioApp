package com.ifpr.ifsolidarioapp.ui.conquista

data class ResultadoConquista(
    val ganhou: Boolean,
    val mensagem: String? = null,
    val lottieResName: String? = null,
    val insigniaDrawable: Int = 0
)