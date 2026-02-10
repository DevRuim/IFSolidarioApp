package com.ifpr.ifsolidarioapp.ui.dashboard

import android.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.ifpr.ifsolidarioapp.databinding.DoacaoTemplateBinding


class DashboardFragment : Fragment() {

    private var _binding: DoacaoTemplateBinding? = null
    private val binding get() = _binding!!

    private val imageList = mutableListOf<Uri>()
    private var currentImageIndex = 0

    companion object {
        private const val PICK_IMAGES_CODE = 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // INFLA O LAYOUT
        _binding = DoacaoTemplateBinding.inflate(inflater, container, false)

        // AQUI você coloca o código do spinner
        val categorias = arrayOf("Alimento", "Brinquedo", "Roupa")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_dropdown_item,
            categorias
        )

        binding.spinnerCategoria.adapter = adapter

        // retorna a tela
        return binding.root
    }

    private fun setupClicks() {

        // Clicar na imagem abre galeria
        binding.imagePreview.setOnClickListener {

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            startActivityForResult(intent, PICK_IMAGES_CODE)
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
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES_CODE &&
            resultCode == Activity.RESULT_OK
        ) {

            imageList.clear()

            // múltiplas imagens
            if (data?.clipData != null) {

                val count = data.clipData!!.itemCount

                for (i in 0 until count) {

                    val uri = data.clipData!!.getItemAt(i).uri
                    imageList.add(uri)
                }
            }
            // uma imagem
            else if (data?.data != null) {

                imageList.add(data.data!!)
            }

            currentImageIndex = 0

            binding.imagePreview.setImageURI(imageList[currentImageIndex])
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
