package com.ifpr.ifsolidarioapp.ui.ranking

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Usuario

class RankingAdapter(
    private val uidAtual: String
) : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    private val lista = mutableListOf<Usuario>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val root: ConstraintLayout =
            view.findViewById(R.id.cardRanking)

        val posicao: TextView =
            view.findViewById(R.id.txtPosicao)

        val nome: TextView =
            view.findViewById(R.id.txtNome)

        val doacoes: TextView =
            view.findViewById(R.id.txtDoacoes)

        val foto: ShapeableImageView =
            view.findViewById(R.id.imgFoto)

        val tvVoce: TextView =
            view.findViewById(R.id.tvVoce)

        val medalha: ImageView =
            view.findViewById(R.id.imgMedalha)
    }

    fun atualizarLista(novaLista: List<Usuario>) {
        lista.clear()
        lista.addAll(ArrayList(novaLista))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_ranking,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val usuario = lista[position]

        // COMPARA COM A CHAVE DO FIREBASE
        val eUsuario = usuario.key == uidAtual

        holder.posicao.text =
            "#${usuario.posicaoRanking}"

        holder.nome.text =
            usuario.nome_usuario

        holder.doacoes.text =
            "Total de ${usuario.total_doacoes} doações"

        holder.tvVoce.visibility =
            if (eUsuario) View.VISIBLE
            else View.GONE

        holder.root.setBackgroundColor(
            when {
                eUsuario ->
                    android.graphics.Color.parseColor("#F5EEFF")

                position % 2 == 0 ->
                    android.graphics.Color.WHITE

                else ->
                    android.graphics.Color.parseColor("#FBF7FF")
            }
        )

        holder.foto.strokeColor =
            ContextCompat.getColorStateList(
                holder.itemView.context,
                if (eUsuario)
                    R.color.ranking_blue
                else
                    R.color.ranking_stroke_default
            )

        holder.foto.strokeWidth =
            if (eUsuario)
                2.5f.dpToPx(holder.itemView.context)
            else
                1.5f.dpToPx(holder.itemView.context)

        carregarFotoCircular(
            usuario.imagemBase64,
            holder.foto
        )

        holder.medalha.visibility =
            View.GONE
    }

    private fun carregarFotoCircular(
        base64: String?,
        imgView: ShapeableImageView
    ) {

        if (!base64.isNullOrEmpty()) {

            try {

                val bytes = Base64.decode(
                    base64,
                    Base64.DEFAULT
                )

                val bitmap =
                    BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.size
                    )

                imgView.setImageBitmap(bitmap)

                return

            } catch (_: Exception) {
            }
        }

        imgView.setImageResource(
            R.drawable.ic_usuario
        )
    }

    private fun Float.dpToPx(
        context: Context
    ): Float {

        return this *
                context.resources
                    .displayMetrics
                    .density
    }
}