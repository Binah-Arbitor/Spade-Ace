package com.binah.spadeace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.binah.spadeace.data.ThemePreferences
import com.binah.spadeace.databinding.FragmentSettingsBinding
import com.binah.spadeace.ui.MainViewModel
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Theme selection
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.radioSystemTheme.id -> viewModel.setThemeMode(ThemePreferences.THEME_MODE_SYSTEM)
                binding.radioLightTheme.id -> viewModel.setThemeMode(ThemePreferences.THEME_MODE_LIGHT)
                binding.radioDarkTheme.id -> viewModel.setThemeMode(ThemePreferences.THEME_MODE_DARK)
            }
        }
        
        // Thread count
        binding.editThreadCount.setText(Runtime.getRuntime().availableProcessors().toString())
        
        // Optimization slider
        binding.sliderOptimization.addOnChangeListener { _, value, _ ->
            // Update optimization level based on slider value
            val level = when (value.toInt()) {
                0 -> com.binah.spadeace.data.OptimizationLevel.LOW
                1 -> com.binah.spadeace.data.OptimizationLevel.MEDIUM  
                2 -> com.binah.spadeace.data.OptimizationLevel.HIGH
                3 -> com.binah.spadeace.data.OptimizationLevel.EXTREME
                else -> com.binah.spadeace.data.OptimizationLevel.MEDIUM
            }
            viewModel.updateOptimizationLevel(level)
        }
        
        // System info
        updateSystemInfo()
    }
    
    private fun updateSystemInfo() {
        val systemInfo = buildString {
            appendLine("Target SDK: Android 16+")
            appendLine("Available Processors: ${Runtime.getRuntime().availableProcessors()}")
            appendLine("Max Memory: ${formatBytes(Runtime.getRuntime().maxMemory())}")
            appendLine("Free Memory: ${formatBytes(Runtime.getRuntime().freeMemory())}")
            appendLine("Encryption Provider: BouncyCastle")
        }
        binding.textSystemInfo.text = systemInfo
    }
    
    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024
        val mb = kb / 1024
        val gb = mb / 1024
        
        return when {
            gb > 0 -> "${gb}.${(mb % 1024) / 100} GB"
            mb > 0 -> "${mb}.${(kb % 1024) / 100} MB"
            else -> "${kb} KB"
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.themeMode.collect { themeMode ->
                when (themeMode) {
                    ThemePreferences.THEME_MODE_LIGHT -> binding.radioLightTheme.isChecked = true
                    ThemePreferences.THEME_MODE_DARK -> binding.radioDarkTheme.isChecked = true
                    ThemePreferences.THEME_MODE_SYSTEM -> binding.radioSystemTheme.isChecked = true
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.attackConfig.collect { config ->
                // Update thread count display
                binding.editThreadCount.setText(config.threadCount.toString())
                
                // Update optimization level slider
                val sliderValue = when (config.optimizationLevel) {
                    com.binah.spadeace.data.OptimizationLevel.LOW -> 0f
                    com.binah.spadeace.data.OptimizationLevel.MEDIUM -> 1f
                    com.binah.spadeace.data.OptimizationLevel.HIGH -> 2f
                    com.binah.spadeace.data.OptimizationLevel.EXTREME -> 3f
                }
                binding.sliderOptimization.value = sliderValue
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = SettingsFragment()
    }
}