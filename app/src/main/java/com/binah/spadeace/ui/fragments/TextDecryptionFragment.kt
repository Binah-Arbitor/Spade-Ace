package com.binah.spadeace.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.binah.spadeace.databinding.FragmentTextDecryptionBinding
import com.binah.spadeace.ui.Constants
import com.binah.spadeace.ui.MainViewModel
import kotlinx.coroutines.launch

class TextDecryptionFragment : Fragment() {
    
    private var _binding: FragmentTextDecryptionBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Fragment binding is null")
    
    private val viewModel: MainViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextDecryptionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }
    
    private fun setupUI() {
        binding.buttonDecryptText.setOnClickListener {
            try {
                val encryptedText = binding.editEncryptedText.text?.toString()?.trim()
                
                if (encryptedText.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Please enter encrypted text", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // Validate input length to prevent excessive processing
                if (encryptedText.length > Constants.MAX_TEXT_INPUT_LENGTH) {
                    Toast.makeText(requireContext(), Constants.ERROR_TEXT_TOO_LONG, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // For demonstration, just show a mock decrypted text
                // In a real implementation, this would call the viewModel for actual decryption
                val mockDecryptedText = "Decrypted: ${encryptedText.reversed()}"
                binding.editDecryptedText.setText(mockDecryptedText)
                
                Toast.makeText(requireContext(), "Text decrypted (mock)", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error processing text: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = TextDecryptionFragment()
    }
}