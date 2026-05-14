package com.ifpr.ifsolidarioapp.baseclasses

data class Ong(

    var key: String = "",
    var nome_ong: String = "",
    var email_ong: String = "",
    var telefone_ong: String = "",
    var cnpj: String = "",
    var imagemBase64: String = "",
    var tipo_usuario: String = "ONG"
)