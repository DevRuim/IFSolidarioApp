package com.ifpr.ifsolidarioapp.ui.doacao

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.R
import com.ifpr.ifsolidarioapp.baseclasses.DoacaoData
import com.ifpr.ifsolidarioapp.databinding.FragmentDoacaoBinding
import com.ifpr.ifsolidarioapp.ui.conquista.ConquistasManager
import java.io.ByteArrayOutputStream
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.graphics.Color
import android.view.Gravity

class DoacaoFragment : Fragment() {

    private var _binding: FragmentDoacaoBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var campanha_id: String = ""
    private var campanha_nome: String = ""
    private var criadorId: String = ""

    private val imageList = mutableListOf<Uri>()
    private var currentImageIndex = 0
    private val MAX_FOTOS = 5

    companion object {
        private const val PICK_IMAGE_CODE = 1000
    }

    private val conquistaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            findNavController().navigate(R.id.navigation_home)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoacaoBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        campanha_id   = arguments?.getString("campanha_id")   ?: ""
        campanha_nome = arguments?.getString("campanha_nome") ?: ""
        criadorId     = arguments?.getString("criadorId")     ?: ""

        setupSpinner()
        setupCategoria()
        setupInfoPasso1()
        setupClicks()

        return binding.root
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Passo 1 — textos dinâmicos por categoria
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupInfoPasso1() {
        val categoria = arguments?.getString("categoria_campanha") ?: "Roupa"
        when (categoria) {
            "Alimento" -> {
                binding.tvPerguntaCategoria.text = "Quantos alimentos você está doando?"
                binding.tvInfoTitulo.text = "Itens aceitos (Não perecíveis e dentro da validade):"
                binding.tvItem1.text = "• Arroz"
                binding.tvItem2.text = "• Feijão"
                binding.tvItem3.text = "• Macarrão"
                binding.tvItem4.text = "• Óleo"
                binding.tvDicaContagem.text =
                    "Você pode doar vários itens de diferentes tipos\n(Ex. 2 kg de arroz + 1 macarrão = 3 itens)"
            }
            "Brinquedo" -> {
                binding.tvPerguntaCategoria.text = "Quantos brinquedos você está doando?"
                binding.tvInfoTitulo.text = "Itens aceitos (Em bom estado e sem peças faltando):"
                binding.tvItem1.text = "• Bonecas"
                binding.tvItem2.text = "• Carrinhos"
                binding.tvItem3.text = "• Jogos"
                binding.tvItem4.text = "• Pelúcias"
                binding.tvDicaContagem.text =
                    "Você pode doar vários itens de diferentes tipos\n(Ex. 2 carrinhos + 1 boneca = 3 itens)"
            }
            else -> { // Roupa
                binding.tvPerguntaCategoria.text = "Quantas roupas você está doando?"
                binding.tvInfoTitulo.text = "Itens aceitos (Qualquer tamanho e em bom estado):"
                binding.tvItem1.text = "• Camisetas"
                binding.tvItem2.text = "• Calças"
                binding.tvItem3.text = "• Cobertores"
                binding.tvItem4.text = "• Blusas"
                binding.tvDicaContagem.text =
                    "Você pode doar vários itens de diferentes tipos\n(Ex. 2 blusas + 1 coberta = 3 itens)"
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Spinner (mantido para compatibilidade)
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupCategoria() {
        val cat = arguments?.getString("categoria_campanha") ?: return
        val adapter = binding.spinnerCategoria.adapter as? ArrayAdapter<String> ?: return
        val idx = adapter.getPosition(cat)
        if (idx >= 0) binding.spinnerCategoria.setSelection(idx)
        binding.spinnerCategoria.isEnabled   = false
        binding.spinnerCategoria.isClickable = false
    }

    private fun setupSpinner() {
        binding.spinnerCategoria.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Alimento", "Brinquedo", "Roupa")
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Clicks
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupClicks() {
        // Passo 1
        binding.btnProximo.setOnClickListener { avancarParaPasso2() }
        binding.tvSair.setOnClickListener { findNavController().popBackStack() }

        // Passo 2 — navegação carrossel
        binding.buttonPrev.setOnClickListener { navegarCarrossel(-1) }
        binding.buttonNext.setOnClickListener { navegarCarrossel(+1) }

        // Passo 2 — adicionar foto
        binding.addImageButton.setOnClickListener { selecionarImagem() }
        binding.editImageButton.setOnClickListener { selecionarImagem() }

        // Remover foto atual
        binding.btnRemoverFoto.setOnClickListener { removerFotoAtual() }

        // Finalizar
        binding.buttonFinalizar.setOnClickListener { finalizarDoacao() }

        // Voltar
        binding.tvVoltar.setOnClickListener { voltarParaPasso1() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navegação entre passos
    // ─────────────────────────────────────────────────────────────────────────

    fun avancarParaPasso2() {
        if (binding.editQuantidade.text.toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Digite uma quantidade", Toast.LENGTH_SHORT).show()
            return
        }
        binding.layoutPasso1.visibility = View.GONE
        binding.layoutPasso2.visibility = View.VISIBLE
        atualizarCarrossel()
    }

    private fun voltarParaPasso1() {
        binding.layoutPasso2.visibility = View.GONE
        binding.layoutPasso1.visibility = View.VISIBLE
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Carrossel
    // ─────────────────────────────────────────────────────────────────────────

    /** Avança ou recua no carrossel. delta = +1 ou -1 */
    private fun navegarCarrossel(delta: Int) {
        if (imageList.isEmpty()) return
        currentImageIndex = (currentImageIndex + delta + imageList.size) % imageList.size
        atualizarCarrossel()
    }

    /** Atualiza toda a UI do carrossel: imagem, setas, dots, thumbnails, contadores */
    private fun atualizarCarrossel() {
        val temFotos = imageList.isNotEmpty()

        // Estado vazio vs com foto
        binding.layoutSemFotos.visibility  = if (temFotos) View.GONE  else View.VISIBLE
        binding.tvPosicaoFoto.visibility   = if (temFotos) View.VISIBLE else View.GONE
        binding.btnRemoverFoto.visibility  = if (temFotos) View.VISIBLE else View.GONE

        if (temFotos) {
            // Limita índice
            if (currentImageIndex >= imageList.size) currentImageIndex = imageList.size - 1

            binding.imagePreview.setImageURI(imageList[currentImageIndex])
            binding.tvPosicaoFoto.text = "${currentImageIndex + 1}/${imageList.size}"
        } else {
            binding.imagePreview.setImageDrawable(null)
        }

        // Setas: visíveis só com 2+ fotos
        val mostrarSetas = imageList.size > 1
        binding.buttonPrev.visibility = if (mostrarSetas) View.VISIBLE else View.INVISIBLE
        binding.buttonNext.visibility = if (mostrarSetas) View.VISIBLE else View.INVISIBLE

        // Contador badge
        binding.tvContadorFotos.text = "${imageList.size} / $MAX_FOTOS"

        // Dots
        atualizarDots()

        // Thumbnails
        atualizarThumbnails()

        // Mostrar scroll de miniaturas só com 2+ fotos
        binding.scrollThumbnails.visibility = if (imageList.size > 1) View.VISIBLE else View.GONE
    }

    /** Reconstrói os dots indicadores */
    private fun atualizarDots() {
        binding.layoutDots.removeAllViews()
        if (imageList.size <= 1) return

        val dp6 = dpToPx(6)
        val dp4 = dpToPx(4)
        val dp8 = dpToPx(8)

        imageList.forEachIndexed { i, _ ->
            val dot = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    if (i == currentImageIndex) dp8 else dp6,
                    if (i == currentImageIndex) dp8 else dp6
                ).also { it.setMargins(dp4, 0, dp4, 0) }
                background = ContextCompat.getDrawable(
                    requireContext(),
                    if (i == currentImageIndex) R.drawable.bg_dot_active
                    else R.drawable.bg_dot_inactive
                )
            }
            binding.layoutDots.addView(dot)
        }
    }

    /** Reconstrói as miniaturas clicáveis */
    private fun atualizarThumbnails() {
        binding.layoutThumbnails.removeAllViews()
        val dp56  = dpToPx(56)
        val dp4   = dpToPx(4)
        val dp2   = dpToPx(2)

        imageList.forEachIndexed { i, uri ->
            val thumb = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dp56, dp56).also {
                    it.setMargins(dp2, 0, dp2, 0)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
                background = if (i == currentImageIndex)
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_thumb_selected)
                else
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_thumb_normal)
                setOnClickListener {
                    currentImageIndex = i
                    atualizarCarrossel()
                }
            }
            binding.layoutThumbnails.addView(thumb)
        }
    }

    /** Remove a foto exibida no momento */
    private fun removerFotoAtual() {
        if (imageList.isEmpty()) return
        imageList.removeAt(currentImageIndex)
        if (currentImageIndex >= imageList.size && currentImageIndex > 0) currentImageIndex--
        atualizarCarrossel()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Seleção de imagem
    // ─────────────────────────────────────────────────────────────────────────

    private fun selecionarImagem() {
        if (imageList.size >= MAX_FOTOS) {
            Toast.makeText(
                requireContext(),
                "Limite de $MAX_FOTOS fotos atingido",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    @Deprecated("Usa ActivityResultLauncher onde possível")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageList.add(uri)
                currentImageIndex = imageList.size - 1
                atualizarCarrossel()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Finalizar doação
    // ─────────────────────────────────────────────────────────────────────────

    private fun finalizarDoacao() {
        val qtdTexto = binding.editQuantidade.text.toString()
        if (qtdTexto.isEmpty()) {
            Toast.makeText(requireContext(), "Digite uma quantidade", Toast.LENGTH_SHORT).show()
            return
        }
        val quantidade = qtdTexto.toDouble()
        val categoria  = binding.spinnerCategoria.selectedItem?.toString()
            ?: arguments?.getString("categoria_campanha") ?: "Roupa"
        val imagensBase64 = imageList.map { uriToBase64(it) }

        salvarNoBanco(
            DoacaoData(
                imagens       = imagensBase64,
                quantidade    = quantidade,
                categoria     = categoria,
                campanha_id   = campanha_id,
                campanha_nome = campanha_nome
            )
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Firebase
    // ─────────────────────────────────────────────────────────────────────────

    private fun salvarNoBanco(item: DoacaoData) {
        val uid = auth.currentUser?.uid ?: return
        database.child("doacoes").child(uid).push().setValue(item)
            .addOnSuccessListener {
                atualizarEstatisticasUsuario(uid, item.categoria, item.quantidade) { _, _ ->
                    atualizarQuantidadeCampanha(item.quantidade) {
                        database.child("usuarios").child(uid).get()
                            .addOnSuccessListener { snap ->
                                ConquistasManager.verificarConquistas(
                                    requireContext(),
                                    uid,
                                    snap.child("alimentos").getValue(Int::class.java) ?: 0,
                                    snap.child("roupas").getValue(Int::class.java) ?: 0,
                                    snap.child("brinquedos").getValue(Int::class.java) ?: 0,
                                    snap.child("total_doacoes").getValue(Double::class.java)?.toInt() ?: 0
                                )

                                binding.root.postDelayed({

                                    if (!ConquistasManager.desbloqueouConquista()) {

                                        findNavController().navigate(
                                            R.id.navigation_ranking
                                        )
                                    }

                                }, 1500)
                            }
                    }
                }
            }
    }

    private fun atualizarEstatisticasUsuario(
        uid: String, categoria: String, quantidade: Double,
        onFinish: (Int?, Int) -> Unit
    ) {
        val ref = database.child("usuarios").child(uid)
        ref.child("total_doacoes").get().addOnSuccessListener { snap ->
            ref.child("total_doacoes").setValue((snap.getValue(Double::class.java) ?: 0.0) + quantidade)
            atualizarTotalCategoria(ref, categoria, quantidade, onFinish)
        }
    }

    private fun atualizarTotalCategoria(
        ref: DatabaseReference, categoria: String, quantidade: Double,
        onFinish: (Int?, Int) -> Unit
    ) {
        val campo = when (categoria) {
            "Alimento"  -> "alimentos"
            "Brinquedo" -> "brinquedos"
            "Roupa"     -> "roupas"
            else        -> { onFinish(null, 0); return }
        }
        ref.child(campo).get().addOnSuccessListener { snap ->
            val novo = (snap.getValue(Int::class.java) ?: 0) + quantidade.toInt()
            ref.child(campo).setValue(novo).addOnSuccessListener { onFinish(null, novo) }
        }
    }

    private fun atualizarQuantidadeCampanha(quantidade: Double, onFinish: () -> Unit) {
        if (campanha_id.isEmpty() || criadorId.isEmpty()) { onFinish(); return }
        val ref = database.child("campanhas").child(criadorId).child(campanha_id)
        ref.child("quantidade_atual").get().addOnSuccessListener { snap ->
            val nova = (snap.getValue(Double::class.java) ?: 0.0) + quantidade
            ref.child("quantidade_atual").setValue(nova).addOnSuccessListener { onFinish() }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitários
    // ─────────────────────────────────────────────────────────────────────────

    private fun uriToBase64(uri: Uri): String {
        val bitmap = BitmapFactory.decodeStream(
            requireContext().contentResolver.openInputStream(uri)
        )
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}