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
import com.ifpr.ifsolidarioapp.baseclasses.DoacaoData
import com.ifpr.ifsolidarioapp.databinding.FragmentItemdoacaoBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentItemdoacaoBinding? = null
    private val binding get() = _binding!!

    private val imageList = mutableListOf<Uri>()
    private var currentImageIndex = 0

    companion object {
        private const val PICK_IMAGE_CODE = 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentItemdoacaoBinding.inflate(inflater, container, false)

        setupSpinner()
        setupClicks()

        return binding.root
    }

    fun obterDados(): DoacaoData? {

        val b = _binding ?: return null

        val quantidadeTexto = b.editQuantidade.text.toString()

        if (quantidadeTexto.isEmpty()) return null

        val quantidade = quantidadeTexto.toDouble()
        val categoria = b.spinnerCategoria.selectedItem.toString()

        val imagensString = imageList.map { it.toString() }

        return DoacaoData(
            categoria = categoria,
            quantidade = quantidade,
            imagens = imagensString
        )
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

        binding.addImageButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_CODE)
        }

        binding.buttonNext.setOnClickListener {

            if (imageList.isNotEmpty()) {

                currentImageIndex = (currentImageIndex + 1) % imageList.size

                binding.imagePreview.setImageURI(imageList[currentImageIndex])
            }
        }

        binding.buttonPrev.setOnClickListener {

            if (imageList.isNotEmpty()) {

                currentImageIndex =
                    if (currentImageIndex - 1 < 0)
                        imageList.size - 1
                    else
                        currentImageIndex - 1

                binding.imagePreview.setImageURI(imageList[currentImageIndex])
            }
        }

        binding.editImageButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_CODE)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK) {

            data?.data?.let { uri ->

                imageList.add(uri)

                currentImageIndex = imageList.size - 1

                binding.imagePreview.setImageURI(uri)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}