package com.binah.spadeace.ui.fragments

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.binah.spadeace.databinding.FragmentFileOperationsBinding
import com.binah.spadeace.ui.Constants
import com.binah.spadeace.ui.MainViewModel
import java.io.File

class FileOperationsFragment : Fragment() {
    
    private var _binding: FragmentFileOperationsBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Fragment binding is null")
    
    private val viewModel: MainViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }
    
    private var currentDirectory: File? = null
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFileOperationsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setInitialDirectory()
    }
    
    private fun setupUI() {
        binding.buttonHome.setOnClickListener {
            try {
                val homeDirectory = Environment.getExternalStorageDirectory()
                if (homeDirectory?.exists() == true && homeDirectory.isDirectory) {
                    navigateToDirectory(homeDirectory)
                } else {
                    Toast.makeText(requireContext(), "Cannot access home directory", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error accessing home directory", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.buttonBack.setOnClickListener {
            try {
                currentDirectory?.parentFile?.let { parent ->
                    if (parent.exists() && parent.canRead()) {
                        navigateToDirectory(parent)
                    } else {
                        Toast.makeText(requireContext(), "Cannot access parent directory", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error accessing parent directory", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.buttonBrowse.setOnClickListener {
            try {
                val pathText = binding.editDirectoryPath.text?.toString()?.trim()
                if (!pathText.isNullOrEmpty()) {
                    val directory = File(pathText)
                    if (directory.exists() && directory.isDirectory && directory.canRead()) {
                        navigateToDirectory(directory)
                    } else {
                        Toast.makeText(requireContext(), Constants.ERROR_DIRECTORY_NOT_ACCESSIBLE, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter a directory path", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), Constants.ERROR_PERMISSION_DENIED, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error accessing directory: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setInitialDirectory() {
        try {
            val initialPath = Environment.getExternalStorageDirectory()
            if (initialPath?.exists() == true && initialPath.canRead()) {
                navigateToDirectory(initialPath)
            } else {
                // Fallback to a safe directory
                val fallbackPath = requireContext().getExternalFilesDir(null)
                if (fallbackPath != null) {
                    navigateToDirectory(fallbackPath)
                } else {
                    Toast.makeText(requireContext(), "Cannot access storage", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Storage permission required", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error accessing initial directory", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToDirectory(directory: File) {
        try {
            if (directory.exists() && directory.isDirectory && directory.canRead()) {
                currentDirectory = directory
                binding.editDirectoryPath.setText(directory.absolutePath)
                updateFileList(directory)
                
                // Enable/disable back button
                binding.buttonBack.isEnabled = directory.parentFile != null
            } else {
                Toast.makeText(requireContext(), "Cannot access directory", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateFileList(directory: File) {
        try {
            val files = directory.listFiles()?.sortedWith(
                compareBy({ !it.isDirectory }, { it.name.lowercase() })
            ) ?: emptyList()
            
            // Apply file count limit for safety
            val message = if (files.size > Constants.MAX_DIRECTORY_FILES) {
                "Found ${files.size} items (showing first ${Constants.MAX_DIRECTORY_FILES})"
            } else {
                "Found ${files.size} items in directory"
            }
            
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            
            // TODO: Implement RecyclerView adapter for file list with paging
            
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), Constants.ERROR_PERMISSION_DENIED, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error listing directory contents", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = FileOperationsFragment()
    }
}