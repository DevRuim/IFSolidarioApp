package com.ifpr.ifsolidarioapp.ui.ranking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Usuario

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    private val lista = mutableListOf<Usuario>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: ConstraintLayout    = view.findViewById(R.id.cardRanking)
        val posicao: TextView         = view.findViewById(R.id.txtPosicao)
        val nome: TextView            = view.findViewById(R.id.txtNome)
        val doacoes: TextView         = view.findViewById(R.id.txtDoacoes)
        val foto: ShapeableImageView  = view.findViewById(R.id.imgFoto)
        val medalha: ImageView        = view.findViewById(R.id.imgMedalha)
    }

    fun atualizarLista(novaLista: List<Usuario>) {
        lista.clear()
        lista.addAll(ArrayList(novaLista))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = lista[position]

        holder.posicao.text = "#${position + 4}"
        holder.nome.text    = usuario.nome_usuario ?: ""
        holder.doacoes.text = "Total de ${usuario.total_doacoes.toInt()} doações"

        holder.root.setBackgroundColor(
            if (position % 2 == 0) android.graphics.Color.WHITE
            else android.graphics.Color.parseColor("#FBF7FF")
        )

        carregarFotoCircular(usuario.imagemBase64, holder.foto, holder.itemView.context)

        holder.medalha.visibility = View.GONE
    }

    // ── Foto circular via ShapeableImageView ────────────────────────────────

    fun carregarFotoCircular(base64: String?, imgView: ShapeableImageView, context: Context) {
        if (!base64.isNullOrEmpty()) {
            try {
                val bytes  = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgView.setImageBitmap(bitmap)  // ShapeableImageView + CircleImageView style aplica o clip circular
                return
            } catch (_: Exception) {}
        }
        imgView.setImageResource(R.drawable.ic_usuario)
    }
}