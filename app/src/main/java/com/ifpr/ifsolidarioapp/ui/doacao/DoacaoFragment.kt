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
    import android.widget.Toast
    import androidx.appcompat.app.AlertDialog
    import androidx.activity.result.contract.ActivityResultContracts
    import com.ifpr.ifsolidarioapp.ui.conquista.ConquistaActivity
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

        companion object {
            private const val PICK_IMAGE_CODE = 1000
        }

        private val conquistaLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {

                findNavController().navigate(
                    R.id.navigation_home
                )
            }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {

            _binding = FragmentDoacaoBinding.inflate(
                inflater,
                container,
                false
            )

            auth = FirebaseAuth.getInstance()

            database = FirebaseDatabase
                .getInstance()
                .reference

            campanha_id =
                arguments?.getString("campanha_id") ?: ""

            campanha_nome =
                arguments?.getString("campanha_nome") ?: ""

            criadorId =
                arguments?.getString("criadorId") ?: ""

            setupSpinner()

            setupCategoria()

            setupClicks()

            return binding.root
        }

        private fun setupCategoria() {

            val categoriaRecebida =
                arguments?.getString("categoria_campanha")

            categoriaRecebida?.let { categoria ->

                val adapter =
                    binding.spinnerCategoria.adapter
                            as ArrayAdapter<String>

                val index =
                    adapter.getPosition(categoria)

                if (index >= 0) {

                    binding.spinnerCategoria
                        .setSelection(index)
                }

                binding.spinnerCategoria.isEnabled = false
                binding.spinnerCategoria.isClickable = false
            }
        }

        private fun setupSpinner() {

            val categorias = arrayOf(
                "Alimento",
                "Brinquedo",
                "Roupa"
            )

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categorias
            )

            binding.spinnerCategoria.adapter = adapter
        }

        private fun setupClicks() {

            binding.addImageButton.setOnClickListener {

                selecionarImagem()
            }

            binding.editImageButton.setOnClickListener {

                selecionarImagem()
            }

            binding.buttonNext.setOnClickListener {

                if (imageList.isNotEmpty()) {

                    currentImageIndex =
                        (currentImageIndex + 1) % imageList.size

                    binding.imagePreview.setImageURI(
                        imageList[currentImageIndex]
                    )
                }
            }

            binding.buttonPrev.setOnClickListener {

                if (imageList.isNotEmpty()) {

                    currentImageIndex =
                        if (currentImageIndex - 1 < 0)
                            imageList.size - 1
                        else
                            currentImageIndex - 1

                    binding.imagePreview.setImageURI(
                        imageList[currentImageIndex]
                    )
                }
            }

            binding.buttonFinalizar.setOnClickListener {

                finalizarDoacao()
            }
        }

        private fun selecionarImagem() {

            val intent = Intent(Intent.ACTION_GET_CONTENT)

            intent.type = "image/*"

            startActivityForResult(
                intent,
                PICK_IMAGE_CODE
            )
        }

        private fun finalizarDoacao() {

            val quantidadeTexto =
                binding.editQuantidade.text.toString()

            if (quantidadeTexto.isEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "Digite uma quantidade",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            val quantidade =
                quantidadeTexto.toDouble()

            val categoria =
                binding.spinnerCategoria
                    .selectedItem
                    .toString()

            val imagensBase64 =
                imageList.map {
                    uriToBase64(it)
                }

            val doacao = DoacaoData(
                imagens = imagensBase64,
                quantidade = quantidade,
                categoria = categoria,
                campanha_id = campanha_id,
                campanha_nome = campanha_nome
            )

            salvarNoBanco(doacao)
        }

        private fun salvarNoBanco(
            item: DoacaoData
        ) {
            val uid =
                auth.currentUser?.uid ?: return

            database
                .child("doacoes")
                .child(uid)
                .push()
                .setValue(item)
                .addOnSuccessListener {

                    atualizarEstatisticasUsuario(
                        uid,
                        item.categoria,
                        item.quantidade
                    ) { novaConquista, novoTotalCategoria ->

                        atualizarQuantidadeCampanha(
                            item.quantidade
                        ) {
                            val usuarioRef =
                                database.child("usuarios").child(uid)

                            usuarioRef.get()
                            .addOnSuccessListener { snapshot ->

                                val alimentos =
                                    snapshot.child("alimentos")
                                        .getValue(Int::class.java) ?: 0

                                val roupas =
                                    snapshot.child("roupas")
                                        .getValue(Int::class.java) ?: 0

                                val brinquedos =
                                    snapshot.child("brinquedos")
                                        .getValue(Int::class.java) ?: 0

                                val total =
                                    snapshot.child("total_doacoes")
                                        .getValue(Double::class.java)
                                        ?.toInt() ?: 0

                                ConquistasManager.verificarConquistas(
                                    requireContext(),
                                    uid,
                                    alimentos,
                                    roupas,
                                    brinquedos,
                                    total
                                )
                            }
                        }
                    }
                }
        }

        private fun atualizarEstatisticasUsuario(
            uid: String,
            categoria: String,
            quantidade: Double,
            onFinish: (Int?, Int) -> Unit
        ) {

            val usuarioRef =
                database
                    .child("usuarios")
                    .child(uid)

            usuarioRef
                .child("total_doacoes")
                .get()
                .addOnSuccessListener { totalSnapshot ->

                    val totalAtual =
                        totalSnapshot.getValue(Double::class.java)
                            ?: 0.0

                    val novoTotal =
                        totalAtual + quantidade

                    usuarioRef
                        .child("total_doacoes")
                        .setValue(novoTotal)

                    atualizarTotalCategoria(
                        usuarioRef,
                        categoria,
                        quantidade
                    ) { novaConquista, novoTotalCategoria ->

                        onFinish(
                            novaConquista,
                            novoTotalCategoria
                        )
                    }
                }
        }

        private fun atualizarTotalCategoria(
            usuarioRef: DatabaseReference,
            categoria: String,
            quantidade: Double,
            onFinish: (Int?, Int) -> Unit
        ) {
            val campoCategoria = when (categoria) {
                "Alimento" -> "alimentos"
                "Brinquedo" -> "brinquedos"
                "Roupa" -> "roupas"
                else -> {
                    onFinish(null, 0)
                    return
                }
            }

            usuarioRef
                .child(campoCategoria)
                .get()
                .addOnSuccessListener { snapshot ->

                    val valorAtual =
                        snapshot.getValue(Int::class.java) ?: 0

                    val novoValor =
                        valorAtual + quantidade.toInt()

                    usuarioRef
                        .child(campoCategoria)
                        .setValue(novoValor)
                        .addOnSuccessListener {

                            onFinish(
                                null,
                                novoValor
                            )
                        }
                }
        }


        private fun atualizarQuantidadeCampanha(
            quantidadeDoada: Double,
            onFinish: () -> Unit
        ) {
            if (
                campanha_id.isEmpty() ||
                criadorId.isEmpty()
            ) {
                onFinish()
                return
            }

            val campanhaRef =
                database
                    .child("campanhas")
                    .child(criadorId)
                    .child(campanha_id)

            campanhaRef
                .child("quantidade_atual")
                .get()
                .addOnSuccessListener { snapshot ->

                    val atual =
                        snapshot.getValue(Double::class.java)
                            ?: 0.0

                    val novaQuantidade =
                        atual + quantidadeDoada

                    campanhaRef
                        .child("quantidade_atual")
                        .setValue(novaQuantidade)
                        .addOnSuccessListener {
                            onFinish()
                        }
                }
        }

        @Deprecated("Deprecated in Java")
        override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
        ) {

            super.onActivityResult(
                requestCode,
                resultCode,
                data
            )

            if (
                requestCode == PICK_IMAGE_CODE &&
                resultCode == Activity.RESULT_OK
            ) {

                data?.data?.let { uri ->

                    imageList.add(uri)

                    currentImageIndex =
                        imageList.size - 1

                    binding.imagePreview
                        .setImageURI(uri)
                }
            }
        }

        private fun uriToBase64(
            uri: Uri
        ): String {

            val inputStream =
                requireContext()
                    .contentResolver
                    .openInputStream(uri)

            val bitmap =
                BitmapFactory.decodeStream(inputStream)

            val outputStream =
                ByteArrayOutputStream()

            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                50,
                outputStream
            )

            val bytes =
                outputStream.toByteArray()

            return Base64.encodeToString(
                bytes,
                Base64.DEFAULT
            )
        }

        override fun onDestroyView() {

            super.onDestroyView()

            _binding = null
        }
    }