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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.binah.spadeace.R
import com.binah.spadeace.data.AttackType
import com.binah.spadeace.data.HardwareAcceleration
import com.binah.spadeace.data.KeyDerivationMethod
import com.binah.spadeace.ui.MainViewModel
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
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
    val encryptionAnalysis by viewModel.encryptionAnalysis.collectAsState()
    
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
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.decryption_attack),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "High-performance decryption attack engine",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
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
                    style = MaterialTheme.typography.h6,
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
                
                // Analyze File Button
                if (attackConfig.targetFile != null) {
                    Button(
                        onClick = { 
                            viewModel.analyzeFile() 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAttackRunning
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Analyze File Encryption")
                    }
                }
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
                    style = MaterialTheme.typography.h6,
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
                                    AttackType.RAINBOW_TABLE -> "Rainbow Table"
                                    AttackType.HYBRID_ATTACK -> "Hybrid Attack"
                                    AttackType.MASK_ATTACK -> "Mask Attack"
                                    AttackType.RULE_BASED_ATTACK -> "Rule-based Attack"
                                    AttackType.SMART_BRUTE_FORCE -> "Smart Brute Force"
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
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                when (attackConfig.attackType) {
                    AttackType.BRUTE_FORCE, AttackType.SMART_BRUTE_FORCE -> {
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
                    }
                    
                    AttackType.DICTIONARY_ATTACK, AttackType.HYBRID_ATTACK, AttackType.RULE_BASED_ATTACK -> {
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
                        
                        // Rule File Selection (for rule-based attack)
                        if (attackConfig.attackType == AttackType.RULE_BASED_ATTACK) {
                            OutlinedTextField(
                                value = attackConfig.ruleFile?.absolutePath ?: "",
                                onValueChange = { },
                                label = { Text("Rule File") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                                type = "text/*"
                                                addCategory(Intent.CATEGORY_OPENABLE)
                                            }
                                            // Note: This would need a separate launcher for rule files
                                            dictionaryPickerLauncher.launch(intent)
                                        }
                                    ) {
                                        Icon(Icons.Default.Description, contentDescription = "Select Rule File")
                                    }
                                }
                            )
                        }
                    }
                    
                    AttackType.RAINBOW_TABLE -> {
                        // Rainbow Table File Selection
                        OutlinedTextField(
                            value = attackConfig.rainbowTableFile?.absolutePath ?: "",
                            onValueChange = { },
                            label = { Text("Rainbow Table File") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                            type = "*/*"
                                            addCategory(Intent.CATEGORY_OPENABLE)
                                        }
                                        dictionaryPickerLauncher.launch(intent)
                                    }
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = "Select Rainbow Table")
                                }
                            }
                        )
                    }
                    
                    AttackType.MASK_ATTACK -> {
                        // Mask Pattern
                        OutlinedTextField(
                            value = attackConfig.maskPattern,
                            onValueChange = { viewModel.updateMaskPattern(it) },
                            label = { Text("Mask Pattern") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("?l?l?l?l?d?d?d?d") },
                            supportingText = { Text("?l=lowercase, ?u=uppercase, ?d=digit, ?s=special") }
                        )
                    }
                }
                
                // Hardware Acceleration Settings
                Text(
                    text = "Hardware Acceleration",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HardwareAcceleration.values().forEach { accel ->
                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = attackConfig.hardwareAcceleration == accel,
                                    onClick = { viewModel.updateHardwareAcceleration(accel) }
                                )
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = attackConfig.hardwareAcceleration == accel,
                                onClick = { viewModel.updateHardwareAcceleration(accel) }
                            )
                            Text(
                                text = when (accel) {
                                    HardwareAcceleration.CPU_ONLY -> "CPU Only"
                                    HardwareAcceleration.GPU_ASSISTED -> "GPU Assisted"
                                    HardwareAcceleration.HYBRID_MODE -> "Hybrid Mode"
                                },
                                modifier = Modifier.padding(start = 4.dp),
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                }
                
                // Key Derivation Method
                Text(
                    text = "Key Derivation Method",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                var showKDFDropdown by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = showKDFDropdown,
                    onExpandedChange = { showKDFDropdown = !showKDFDropdown }
                ) {
                    OutlinedTextField(
                        value = when (attackConfig.keyDerivationMethod) {
                            KeyDerivationMethod.SHA256_SIMPLE -> "SHA-256 Simple"
                            KeyDerivationMethod.PBKDF2 -> "PBKDF2"
                            KeyDerivationMethod.SCRYPT -> "Scrypt"
                            KeyDerivationMethod.ARGON2 -> "Argon2"
                            KeyDerivationMethod.BCRYPT -> "Bcrypt"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showKDFDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showKDFDropdown,
                        onDismissRequest = { showKDFDropdown = false }
                    ) {
                        KeyDerivationMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = { 
                                    Text(when (method) {
                                        KeyDerivationMethod.SHA256_SIMPLE -> "SHA-256 Simple"
                                        KeyDerivationMethod.PBKDF2 -> "PBKDF2"
                                        KeyDerivationMethod.SCRYPT -> "Scrypt"
                                        KeyDerivationMethod.ARGON2 -> "Argon2"
                                        KeyDerivationMethod.BCRYPT -> "Bcrypt"
                                    })
                                },
                                onClick = {
                                    viewModel.updateKeyDerivationMethod(method)
                                    showKDFDropdown = false
                                }
                            )
                        }
                    }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAttackRunning) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                        Text(
                            text = stringResource(R.string.progress),
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    if (isAttackRunning) {
                        LinearProgressIndicator(
                            progress = attackProgress.progress,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colors.primary,
                            trackColor = MaterialTheme.colors.surface
                        )
                        
                        // Progress percentage
                        Text(
                            text = "${(attackProgress.progress * 100).toInt()}% Complete",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colors.onSurface
                    )
                    
                    // Current attempt with styling
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colors.onSurface
                        )
                        Text(
                            text = "Current: ${attackProgress.currentAttempt}",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.body1.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        )
                    }
                    
                    // Attempts count with formatting
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Numbers,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colors.onSurface
                        )
                        Text(
                            text = "Attempts: ${formatNumber(attackProgress.attemptsCount)}",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.body1
                        )
                    }
                    
                    if (attackProgress.estimatedTimeRemaining > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colors.onSurface
                            )
                            Text(
                                text = "ETA: ${formatTime(attackProgress.estimatedTimeRemaining)}",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.secondary
                            )
                        }
                    }
                }
            }
        }
        
        // Encryption Analysis Results
        encryptionAnalysis?.let { analysis ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    backgroundColor = MaterialTheme.colors.secondary
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "File Analysis Results",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Confidence: ${(analysis.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (analysis.possibleAlgorithms.isNotEmpty()) {
                        Text("Possible Algorithms: ${analysis.possibleAlgorithms.joinToString(", ")}")
                    }
                    
                    if (analysis.possibleModes.isNotEmpty()) {
                        Text("Possible Modes: ${analysis.possibleModes.joinToString(", ")}")
                    }
                    
                    if (analysis.possiblePaddings.isNotEmpty()) {
                        Text("Possible Paddings: ${analysis.possiblePaddings.joinToString(", ")}")
                    }
                    
                    analysis.detectedFormat?.let { format ->
                        Text("Detected Format: $format")
                    }
                    
                    if (analysis.analysisNotes.isNotEmpty()) {
                        Text(
                            text = "Analysis Notes:",
                            fontWeight = FontWeight.Medium
                        )
                        analysis.analysisNotes.forEach { note ->
                            Text("• $note", style = MaterialTheme.typography.body2)
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.clearAnalysis() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear")
                    }
                }
            }
        }
        
        // Result Display
        attackResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = if (result.success) {
                    CardDefaults.cardColors(backgroundColor = MaterialTheme.colors.primary)
                } else {
                    CardDefaults.cardColors(backgroundColor = MaterialTheme.colors.error)
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (result.success) "Success!" else "Failed",
                        style = MaterialTheme.typography.h6,
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

private fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000_000 -> String.format("%.1fB", number / 1_000_000_000.0)
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}