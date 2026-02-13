package com.ifpr.ifsolidarioapp.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.ifpr.ifsolidarioapp.databinding.DoacaoTemplateBinding

class DashboardFragment : Fragment() {

    private var _binding: DoacaoTemplateBinding? = null
    private val binding get() = _binding!!

    // Lista de imagens adicionadas
    private val imageList = mutableListOf<Uri>()

    // índice da imagem atual
    private var currentImageIndex = 0

    companion object {
        private const val PICK_IMAGE_CODE = 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = DoacaoTemplateBinding.inflate(inflater, container, false)

        setupSpinner()

        setupClicks()

        return binding.root
    }

    private fun setupSpinner() {

        val categorias = arrayOf("Alimento", "Brinquedo", "Roupa")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categorias
        )

        binding.spinnerCategoria.adapter = adapter
    }

    private fun setupClicks() {

        // BOTÃO + adiciona UMA imagem por vez
        binding.addImageButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            startActivityForResult(intent, PICK_IMAGE_CODE)
        }

        // Próxima imagem
        binding.buttonNext.setOnClickListener {

            if (imageList.isNotEmpty()) {

                currentImageIndex++

                if (currentImageIndex >= imageList.size)
                    currentImageIndex = 0

                binding.imagePreview.setImageURI(imageList[currentImageIndex])
            }
        }

        // Imagem anterior
        binding.buttonPrev.setOnClickListener {

            if (imageList.isNotEmpty()) {

                currentImageIndex--

                if (currentImageIndex < 0)
                    currentImageIndex = imageList.size - 1

                binding.imagePreview.setImageURI(imageList[currentImageIndex])
            }
        }

        // Botão editar imagem (opcional - abre galeria para substituir imagem atual)
        binding.editImageButton.setOnClickListener {

            if (imageList.isNotEmpty()) {

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"

                startActivityForResult(intent, PICK_IMAGE_CODE)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CODE &&
            resultCode == Activity.RESULT_OK
        ) {

            data?.data?.let { uri ->

                // adiciona imagem na lista
                imageList.add(uri)

                // define como imagem atual
                currentImageIndex = imageList.size - 1

                // mostra no preview
                binding.imagePreview.setImageURI(uri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
