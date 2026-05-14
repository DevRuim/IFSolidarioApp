package com.ifpr.ifsolidarioapp.baseclasses

data class Usuario(

    var key: String = "",
    var nome_usuario: String = "",
    var email_usuario: String = "",
    var telefone_usuario: String = "",
    var imagemBase64: String = "",
    var tipo_usuario: String = "Doador",

)