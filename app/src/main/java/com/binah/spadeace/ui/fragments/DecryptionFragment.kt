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
import com.binah.spadeace.ui.MainViewModel
import kotlinx.coroutines.launch
import java.io.File

class DecryptionFragment : Fragment() {
    
    private var _binding: FragmentDecryptionBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val uri = result.data?.data
            uri?.let { 
                val file = File(uri.path ?: "")
                viewModel.updateTargetFile(file)
            }
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
            viewModel.analyzeFile()
        }
        
        binding.buttonStartAttack.setOnClickListener {
            viewModel.startAttack()
        }
        
        binding.buttonStopAttack.setOnClickListener {
            viewModel.stopAttack()
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