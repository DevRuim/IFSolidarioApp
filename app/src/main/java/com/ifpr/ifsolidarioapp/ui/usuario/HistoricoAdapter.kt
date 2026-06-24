package com.ifpr.ifsolidarioapp.ui.usuario

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.ifsolidarioapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoricoAdapter(
    private val lista: List<HistoricoDoacao>
) : RecyclerView.Adapter<HistoricoAdapter.ViewHolder>() {

    class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val imagem =
            view.findViewById<ImageView>(R.id.imgDoacao)

        val campanha =
            view.findViewById<TextView>(R.id.txtCampanha)

        val categoria =
            view.findViewById<TextView>(R.id.txtCategoria)

        val quantidade =
            view.findViewById<TextView>(R.id.txtQuantidade)

        val txtDataDoacao =
            view.findViewById<TextView>(R.id.txtDataDoacao)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_historico,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun getItemCount() =
        lista.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = lista[position]

        val formato =
            SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale("pt", "BR")
            )

        holder.txtDataDoacao.text =
            "Doado em: ${
                formato.format(
                    Date(item.dataDoacao)
                )
            }"

        holder.campanha.text =
            item.campanha_nome

        holder.categoria.text =
            "Categoria: ${item.categoria}"

        holder.quantidade.text =
            "Quantidade: ${item.quantidade}"

        if (item.imagens.isNotEmpty()) {

            try {

                val bytes =
                    Base64.decode(
                        item.imagens[0],
                        Base64.DEFAULT
                    )

                val bitmap =
                    BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.size
                    )

                holder.imagem.setImageBitmap(bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}