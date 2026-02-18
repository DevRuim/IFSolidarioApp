package com.ifpr.ifsolidarioapp.baseclasses
import android.net.Uri

data class DoacaoData(
    var categoria: String? = null,
    var quantidade: Double? = null,
    val imagens: List<Uri>
)
