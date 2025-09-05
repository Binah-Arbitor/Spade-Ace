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
import com.binah.spadeace.data.ThemePreferences
import com.binah.spadeace.databinding.FragmentSettingsBinding
import com.binah.spadeace.ui.Constants
import com.binah.spadeace.ui.MainViewModel
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Fragment binding is null")
    
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
        
        // Thread count with validation
        val availableProcessors = Runtime.getRuntime().availableProcessors()
        val safeThreadCount = availableProcessors.coerceIn(Constants.MIN_THREAD_COUNT, Constants.MAX_THREAD_COUNT)
        binding.editThreadCount.setText(safeThreadCount.toString())
        
        // Add input validation for thread count
        binding.editThreadCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                try {
                    val input = s?.toString()?.trim()
                    if (!input.isNullOrEmpty()) {
                        val threadCount = input.toIntOrNull()
                        when {
                            threadCount == null -> {
                                binding.editThreadCount.error = "Please enter a valid number"
                            }
                            threadCount < Constants.MIN_THREAD_COUNT -> {
                                binding.editThreadCount.error = "Minimum ${Constants.MIN_THREAD_COUNT} thread required"
                            }
                            threadCount > Constants.MAX_THREAD_COUNT -> {
                                binding.editThreadCount.error = "Maximum ${Constants.MAX_THREAD_COUNT} threads allowed"
                            }
                            else -> {
                                binding.editThreadCount.error = null
                            }
                        }
                    }
                } catch (e: Exception) {
                    binding.editThreadCount.error = "Invalid input"
                }
            }
        })
        
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
        try {
            val runtime = Runtime.getRuntime()
            val availableProcessors = runtime.availableProcessors()
            val maxMemory = runtime.maxMemory()
            val freeMemory = runtime.freeMemory()
            
            val systemInfo = buildString {
                appendLine("Target SDK: Android 21+")
                appendLine("Available Processors: $availableProcessors")
                appendLine("Max Memory: ${formatBytes(maxMemory)}")
                appendLine("Free Memory: ${formatBytes(freeMemory)}")
                appendLine("Encryption Provider: BouncyCastle")
                
                // Add memory usage warning if needed
                val memoryUsagePercent = ((maxMemory - freeMemory) * 100 / maxMemory).coerceIn(0, 100)
                if (memoryUsagePercent > Constants.HIGH_MEMORY_USAGE_THRESHOLD) {
                    appendLine("\n⚠️ High memory usage: ${memoryUsagePercent}%")
                }
            }
            binding.textSystemInfo.text = systemInfo
        } catch (e: Exception) {
            binding.textSystemInfo.text = "System information unavailable"
        }
    }
    
    private fun formatBytes(bytes: Long): String {
        return try {
            // Prevent overflow and negative values
            val safeBytes = bytes.coerceAtLeast(0)
            val kb = safeBytes / 1024
            val mb = kb / 1024
            val gb = mb / 1024
            
            when {
                gb > 0 -> String.format("%.1f GB", gb + (mb % 1024) / 1024.0)
                mb > 0 -> String.format("%.1f MB", mb + (kb % 1024) / 1024.0)
                kb > 0 -> "$kb KB"
                else -> "$safeBytes B"
            }
        } catch (e: Exception) {
            "Unknown"
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
                try {
                    // Update thread count display with bounds checking
                    val safeThreadCount = config.threadCount.coerceIn(Constants.MIN_THREAD_COUNT, Constants.MAX_THREAD_COUNT)
                    binding.editThreadCount.setText(safeThreadCount.toString())
                    
                    // Update optimization level slider
                    val sliderValue = when (config.optimizationLevel) {
                        com.binah.spadeace.data.OptimizationLevel.LOW -> 0f
                        com.binah.spadeace.data.OptimizationLevel.MEDIUM -> 1f
                        com.binah.spadeace.data.OptimizationLevel.HIGH -> 2f
                        com.binah.spadeace.data.OptimizationLevel.EXTREME -> 3f
                    }
                    binding.sliderOptimization.value = sliderValue
                } catch (e: Exception) {
                    // Handle any potential errors gracefully
                    Toast.makeText(requireContext(), "Error updating settings display", Toast.LENGTH_SHORT).show()
                }
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