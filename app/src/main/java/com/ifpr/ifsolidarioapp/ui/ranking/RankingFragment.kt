package com.ifpr.ifsolidarioapp.ui.ranking

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.*
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.Usuario

class RankingFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RankingAdapter

    private val listaUsuarios = mutableListOf<Usuario>()

    private lateinit var layoutPodio: LinearLayout
    private lateinit var imgPodio1: ShapeableImageView
    private lateinit var imgPodio2: ShapeableImageView
    private lateinit var imgPodio3: ShapeableImageView
    private lateinit var tvNomePodio1: TextView
    private lateinit var tvNomePodio2: TextView
    private lateinit var tvNomePodio3: TextView
    private lateinit var tvDoacoesPodio1: TextView
    private lateinit var tvDoacoesPodio2: TextView
    private lateinit var tvDoacoesPodio3: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.ranking_fragment, container, false)

        recycler = view.findViewById(R.id.recyclerRanking)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = RankingAdapter()
        recycler.adapter = adapter

        layoutPodio     = view.findViewById(R.id.layoutPodio)
        imgPodio1       = view.findViewById(R.id.imgPodio1)
        imgPodio2       = view.findViewById(R.id.imgPodio2)
        imgPodio3       = view.findViewById(R.id.imgPodio3)
        tvNomePodio1    = view.findViewById(R.id.tvNomePodio1)
        tvNomePodio2    = view.findViewById(R.id.tvNomePodio2)
        tvNomePodio3    = view.findViewById(R.id.tvNomePodio3)
        tvDoacoesPodio1 = view.findViewById(R.id.tvDoacoesPodio1)
        tvDoacoesPodio2 = view.findViewById(R.id.tvDoacoesPodio2)
        tvDoacoesPodio3 = view.findViewById(R.id.tvDoacoesPodio3)

        carregarRanking()
        return view
    }

    private fun carregarRanking() {
        FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    listaUsuarios.clear()
                    for (item in snapshot.children) {
                        item.getValue(Usuario::class.java)?.let { listaUsuarios.add(it) }
                    }
                    listaUsuarios.sortByDescending { it.total_doacoes }

                    atualizarPodio()

                    val restante: List<Usuario> =
                        if (listaUsuarios.size > 3)
                            ArrayList(listaUsuarios.subList(3, listaUsuarios.size))
                        else emptyList()

                    adapter.atualizarLista(restante)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun atualizarPodio() {
        layoutPodio.visibility =
            if (listaUsuarios.isNotEmpty()) View.VISIBLE else View.GONE

        fun bind(
            index: Int,
            img: ShapeableImageView,
            tvNome: TextView,
            tvDoacoes: TextView
        ) {
            if (index < listaUsuarios.size) {
                val u = listaUsuarios[index]
                tvNome.text    = u.nome_usuario ?: "—"
                tvDoacoes.text = "${u.total_doacoes.toInt()} doa."
                carregarFotoPodio(u.imagemBase64, img)
            } else {
                tvNome.text    = "—"
                tvDoacoes.text = ""
            }
        }

        bind(0, imgPodio1, tvNomePodio1, tvDoacoesPodio1)
        bind(1, imgPodio2, tvNomePodio2, tvDoacoesPodio2)
        bind(2, imgPodio3, tvNomePodio3, tvDoacoesPodio3)
    }

    private fun carregarFotoPodio(base64: String?, imgView: ShapeableImageView) {
        if (!base64.isNullOrEmpty()) {
            try {
                val bytes  = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgView.setImageBitmap(bitmap)
                return
            } catch (_: Exception) {}
        }
        imgView.setImageResource(R.drawable.ic_usuario)
    }
}