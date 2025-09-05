package com.binah.spadeace.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.binah.spadeace.R
import com.binah.spadeace.data.AttackType
import com.binah.spadeace.ui.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecryptionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val attackConfig by viewModel.attackConfig.collectAsState()
    val attackProgress by viewModel.attackProgress.collectAsState()
    val attackResult by viewModel.attackResult.collectAsState()
    val isAttackRunning by viewModel.isAttackRunning.collectAsState()
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val file = File(uri.path ?: "")
                if (file.exists()) {
                    viewModel.updateTargetFile(file)
                }
            }
        }
    }
    
    val dictionaryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val file = File(uri.path ?: "")
                if (file.exists()) {
                    viewModel.updateDictionaryFile(file)
                }
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
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
                    text = stringResource(R.string.decryption_attack),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "High-performance decryption attack engine",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // File Selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.file_path),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = attackConfig.targetFile?.absolutePath ?: "",
                    onValueChange = { },
                    label = { Text("Target File") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                    type = "*/*"
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                }
                                filePickerLauncher.launch(intent)
                            }
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Select File")
                        }
                    }
                )
            }
        }
        
        // Attack Type Selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.attack_type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AttackType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = attackConfig.attackType == type,
                                    onClick = { viewModel.updateAttackType(type) }
                                )
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = attackConfig.attackType == type,
                                onClick = { viewModel.updateAttackType(type) }
                            )
                            Text(
                                text = when (type) {
                                    AttackType.BRUTE_FORCE -> stringResource(R.string.brute_force)
                                    AttackType.DICTIONARY_ATTACK -> stringResource(R.string.dictionary_attack)
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Attack Configuration
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Attack Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (attackConfig.attackType == AttackType.BRUTE_FORCE) {
                    // Max Password Length
                    Text("${stringResource(R.string.max_length)}: ${attackConfig.maxPasswordLength}")
                    Slider(
                        value = attackConfig.maxPasswordLength.toFloat(),
                        onValueChange = { viewModel.updateMaxPasswordLength(it.toInt()) },
                        valueRange = 1f..12f,
                        steps = 10
                    )
                    
                    // Character Set
                    OutlinedTextField(
                        value = attackConfig.characterSet,
                        onValueChange = { viewModel.updateCharacterSet(it) },
                        label = { Text(stringResource(R.string.charset)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )
                } else {
                    // Dictionary File Selection
                    OutlinedTextField(
                        value = attackConfig.dictionaryFile?.absolutePath ?: "",
                        onValueChange = { },
                        label = { Text(stringResource(R.string.dictionary_file)) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                        type = "text/*"
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                    }
                                    dictionaryPickerLauncher.launch(intent)
                                }
                            ) {
                                Icon(Icons.Default.Description, contentDescription = "Select Dictionary")
                            }
                        }
                    )
                }
            }
        }
        
        // Progress Display
        if (isAttackRunning || attackProgress.attemptsCount > 0) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.progress),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isAttackRunning) {
                        LinearProgressIndicator(
                            progress = attackProgress.progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Text("Current attempt: ${attackProgress.currentAttempt}")
                    Text("Attempts: ${attackProgress.attemptsCount}")
                    
                    if (attackProgress.estimatedTimeRemaining > 0) {
                        Text("Estimated time remaining: ${formatTime(attackProgress.estimatedTimeRemaining)}")
                    }
                }
            }
        }
        
        // Result Display
        attackResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = if (result.success) {
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                } else {
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (result.success) "Success!" else "Failed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (result.success) {
                        Text("Password found: ${result.foundPassword}")
                    }
                    
                    Text("Time elapsed: ${formatTime(result.timeElapsed)}")
                    Text("Total attempts: ${result.attemptsCount}")
                    
                    result.errorMessage?.let { error ->
                        Text("Error: $error")
                    }
                }
            }
        }
        
        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { 
                    if (isAttackRunning) {
                        viewModel.stopAttack()
                    } else {
                        viewModel.startAttack()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = attackConfig.targetFile != null
            ) {
                Icon(
                    imageVector = if (isAttackRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAttackRunning) 
                        stringResource(R.string.stop_attack) 
                    else 
                        stringResource(R.string.start_attack)
                )
            }
            
            OutlinedButton(
                onClick = { viewModel.clearResult() },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear")
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}