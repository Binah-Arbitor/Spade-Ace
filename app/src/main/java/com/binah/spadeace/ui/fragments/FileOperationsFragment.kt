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
import com.binah.spadeace.ui.MainViewModel
import java.io.File

class FileOperationsFragment : Fragment() {
    
    private var _binding: FragmentFileOperationsBinding? = null
    private val binding get() = _binding!!
    
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
            navigateToDirectory(Environment.getExternalStorageDirectory())
        }
        
        binding.buttonBack.setOnClickListener {
            currentDirectory?.parentFile?.let { parent ->
                navigateToDirectory(parent)
            }
        }
        
        binding.buttonBrowse.setOnClickListener {
            val pathText = binding.editDirectoryPath.text.toString()
            if (pathText.isNotEmpty()) {
                val directory = File(pathText)
                if (directory.exists() && directory.isDirectory) {
                    navigateToDirectory(directory)
                } else {
                    Toast.makeText(requireContext(), "Directory does not exist", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setInitialDirectory() {
        val initialPath = Environment.getExternalStorageDirectory()
        navigateToDirectory(initialPath)
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
            
            // For now, just show count - in a real implementation, this would update RecyclerView
            Toast.makeText(
                requireContext(), 
                "Found ${files.size} items in directory", 
                Toast.LENGTH_SHORT
            ).show()
            
            // TODO: Implement RecyclerView adapter for file list
            
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Cannot list files", Toast.LENGTH_SHORT).show()
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