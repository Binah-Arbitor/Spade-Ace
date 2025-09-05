package com.binah.spadeace.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.binah.spadeace.R
import com.binah.spadeace.ui.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOperationsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedDirectory by remember { mutableStateOf<File?>(null) }
    var fileList by remember { mutableStateOf<List<File>>(emptyList()) }
    var showHidden by remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedDirectory) {
        selectedDirectory?.let { dir ->
            fileList = dir.listFiles()?.filter { file ->
                showHidden || !file.name.startsWith(".")
            }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.file_operations),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Browse and manage encrypted files",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Directory Navigation
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Directory:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row {
                        IconButton(
                            onClick = { 
                                selectedDirectory = File("/storage/emulated/0")
                            }
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        }
                        
                        IconButton(
                            onClick = {
                                selectedDirectory?.parentFile?.let { parent ->
                                    selectedDirectory = parent
                                }
                            },
                            enabled = selectedDirectory?.parentFile != null
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
                
                OutlinedTextField(
                    value = selectedDirectory?.absolutePath ?: "/storage/emulated/0",
                    onValueChange = { path ->
                        val dir = File(path)
                        if (dir.exists() && dir.isDirectory) {
                            selectedDirectory = dir
                        }
                    },
                    label = { Text("Directory Path") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showHidden,
                        onCheckedChange = { showHidden = it }
                    )
                    Text(
                        text = "Show hidden files",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        
        // File List
        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Files (${fileList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(fileList) { file ->
                        FileItem(
                            file = file,
                            onClick = {
                                if (file.isDirectory) {
                                    selectedDirectory = file
                                } else {
                                    viewModel.updateTargetFile(file)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Initialize with default directory
    LaunchedEffect(Unit) {
        selectedDirectory = File("/storage/emulated/0")
    }
}

@Composable
private fun FileItem(
    file: File,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (file.isDirectory) Icons.Default.Folder else getFileIcon(file),
                    contentDescription = null,
                    tint = if (file.isDirectory) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (file.isFile) {
                        Text(
                            text = formatFileSize(file.length()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (file.isFile) {
                IconButton(
                    onClick = onClick
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getFileIcon(file: File): androidx.compose.ui.graphics.vector.ImageVector {
    return when (file.extension.lowercase()) {
        "txt", "log", "md" -> Icons.Default.Description
        "zip", "rar", "7z", "tar", "gz" -> Icons.Default.Archive
        "jpg", "jpeg", "png", "gif", "bmp" -> Icons.Default.Image
        "mp4", "avi", "mov", "mkv" -> Icons.Default.VideoFile
        "mp3", "wav", "flac", "ogg" -> Icons.Default.AudioFile
        "pdf" -> Icons.Default.PictureAsPdf
        "apk" -> Icons.Default.Android
        else -> Icons.Default.InsertDriveFile
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}