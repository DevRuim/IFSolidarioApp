package com.ifpr.ifsolidarioapp.ui.ranking

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.*
import com.ifpr.ifsolidarioapp.R
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.ifsolidarioapp.baseclasses.Usuario

class RankingAdapter(
    private val lista: List<Usuario>
) : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val card = view.findViewById<com.google.android.material.card.MaterialCardView>(
            R.id.cardRanking
        )

        val posicao = view.findViewById<TextView>(R.id.txtPosicao)
        val nome = view.findViewById<TextView>(R.id.txtNome)
        val doacoes = view.findViewById<TextView>(R.id.txtDoacoes)
        val foto = view.findViewById<ImageView>(R.id.imgFoto)
        val medalha = view.findViewById<ImageView>(R.id.imgMedalha)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val usuario = lista[position]

        holder.posicao.text = "#${position + 1}"

        holder.nome.text = usuario.nome_usuario

        holder.doacoes.text =
            "Total de ${usuario.total_doacoes} doações"

        when(position){

            0 -> {
                holder.card.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#FFF4CC")
                )
            }

            1 -> {
                holder.card.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#F1F1F1")
                )
            }

            2 -> {
                holder.card.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#FFE6D5")
                )
            }

            else -> {
                holder.card.setCardBackgroundColor(
                    android.graphics.Color.WHITE
                )
            }
        }

        // FOTO BASE64
        if (usuario.imagemBase64.isNotEmpty()) {

            try {

                val bytes = Base64.decode(
                    usuario.imagemBase64,
                    Base64.DEFAULT
                )

                val bitmap = BitmapFactory.decodeByteArray(
                    bytes,
                    0,
                    bytes.size
                )

                holder.foto.setImageBitmap(bitmap)

            } catch (e: Exception) {
                holder.foto.setImageResource(R.drawable.ic_usuario)
            }

        } else {
            holder.foto.setImageResource(R.drawable.ic_usuario)
        }

        when(position){

            0 -> {
                holder.medalha.visibility = View.VISIBLE
                holder.medalha.setImageResource(R.drawable.ic_gold)
            }

            1 -> {
                holder.medalha.visibility = View.VISIBLE
                holder.medalha.setImageResource(R.drawable.ic_silver)
            }

            2 -> {
                holder.medalha.visibility = View.VISIBLE
                holder.medalha.setImageResource(R.drawable.ic_bronze)
            }

            else -> {
                holder.medalha.visibility = View.GONE
            }
        }
    }
}