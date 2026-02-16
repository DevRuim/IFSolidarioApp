package com.ifpr.ifsolidarioapp.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.ifsolidarioapp.baseclasses.Item
import com.ifpr.ifsolidarioapp.databinding.FragmentDoacaoBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDoacaoBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

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

        _binding = FragmentDoacaoBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupSpinner()
        setupClicks()

        return binding.root
    }

    // 🔥 FUNÇÃO QUE O MAIN ACTIVITY VAI CHAMAR
    fun salvarDoacao() {
        val categoria = binding.spinnerCategoria.selectedItem.toString()
        val quantidadeTexto = binding.editQuantidade.text.toString()

        if (quantidadeTexto.isEmpty()) {
            Toast.makeText(requireContext(), "Informe a quantidade", Toast.LENGTH_SHORT).show()
            return
        }

        val quantidade = quantidadeTexto.toDouble()

        val item = Item(
            categoria = categoria,
            quantidade = quantidade
        )

        val database = FirebaseDatabase
            .getInstance()
            .getReference("doacoes")

        val id = database.push().key!!

        database.child(id).setValue(item)
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
                currentImageIndex++
                if (currentImageIndex >= imageList.size)
                    currentImageIndex = 0

                binding.imagePreview.setImageURI(imageList[currentImageIndex])
            }
        }

        binding.buttonPrev.setOnClickListener {
            if (imageList.isNotEmpty()) {
                currentImageIndex--
                if (currentImageIndex < 0)
                    currentImageIndex = imageList.size - 1

                binding.imagePreview.setImageURI(imageList[currentImageIndex])
            }
        }

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
