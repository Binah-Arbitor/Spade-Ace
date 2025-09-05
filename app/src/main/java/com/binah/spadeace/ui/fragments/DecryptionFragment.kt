package com.binah.spadeace.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.binah.spadeace.data.AttackType
import com.binah.spadeace.databinding.FragmentDecryptionBinding
import com.binah.spadeace.ui.Constants
import com.binah.spadeace.ui.MainViewModel
import kotlinx.coroutines.launch
import java.io.File

class DecryptionFragment : Fragment() {
    
    private var _binding: FragmentDecryptionBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Fragment binding is null")
    
    private val viewModel: MainViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri = result.data?.data
                uri?.let { 
                    val path = uri.path
                    if (!path.isNullOrEmpty()) {
                        val file = File(path)
                        if (file.exists() && file.canRead()) {
                            // Additional safety check for file size
                            if (file.length() > Constants.MAX_FILE_SIZE_BYTES) {
                                Toast.makeText(requireContext(), Constants.ERROR_FILE_TOO_LARGE, Toast.LENGTH_SHORT).show()
                                return@registerForActivityResult
                            }
                            viewModel.updateTargetFile(file)
                        } else {
                            Toast.makeText(requireContext(), "Cannot access selected file", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), Constants.ERROR_INVALID_FILE_PATH, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error selecting file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecryptionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // File selection
        binding.editFilePath.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            filePickerLauncher.launch(intent)
        }
        
        // Attack type selection
        binding.radioGroupAttackType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioBruteForce.id -> viewModel.updateAttackType(AttackType.BRUTE_FORCE)
                binding.radioDictionary.id -> viewModel.updateAttackType(AttackType.DICTIONARY_ATTACK)
            }
        }
        
        // Action buttons
        binding.buttonAnalyze.setOnClickListener {
            try {
                viewModel.analyzeFile()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error analyzing file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.buttonStartAttack.setOnClickListener {
            try {
                viewModel.startAttack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error starting attack: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.buttonStopAttack.setOnClickListener {
            try {
                viewModel.stopAttack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error stopping attack: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.attackConfig.collect { config ->
                // Update file path
                binding.editFilePath.setText(config.targetFile?.absolutePath ?: "")
                
                // Update attack type selection
                when (config.attackType) {
                    AttackType.BRUTE_FORCE -> binding.radioBruteForce.isChecked = true
                    AttackType.DICTIONARY_ATTACK -> binding.radioDictionary.isChecked = true
                    else -> {}
                }
                
                // Enable/disable analyze button
                binding.buttonAnalyze.isEnabled = config.targetFile != null
            }
        }
        
        lifecycleScope.launch {
            viewModel.isAttackRunning.collect { isRunning ->
                binding.buttonStartAttack.isEnabled = !isRunning
                binding.buttonStopAttack.isEnabled = isRunning
                binding.buttonAnalyze.isEnabled = !isRunning
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = DecryptionFragment()
    }
}