package com.binah.spadeace.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.binah.spadeace.R
import com.binah.spadeace.data.AttackType
import com.binah.spadeace.ui.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextDecryptionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var outputText by remember { mutableStateOf("") }
    var selectedPassword by remember { mutableStateOf("") }
    var isDecrypting by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    
    val attackResult by viewModel.attackResult.collectAsState()
    val attackProgress by viewModel.attackProgress.collectAsState()
    val isAttackRunning by viewModel.isAttackRunning.collectAsState()
    
    // Update output when attack completes successfully
    LaunchedEffect(attackResult) {
        attackResult?.let { result ->
            if (result.success && result.foundPassword != null) {
                selectedPassword = result.foundPassword
                // Here you would typically decrypt the text using the found password
                outputText = "Decryption successful with password: ${result.foundPassword}\n" +
                        "Decrypted text: ${inputText.text}" // Placeholder - implement actual decryption
                isDecrypting = false
            } else if (!result.success) {
                outputText = "Decryption failed: ${result.errorMessage ?: "Unknown error"}"
                isDecrypting = false
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary
                    )
                    Text(
                        text = "Text Decryption",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "Decrypt text directly or attempt password recovery",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
        
        // Input Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Input Text",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Encrypted Text") },
                    placeholder = { Text("Paste your encrypted text here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 6,
                    supportingText = { Text("Enter the text you want to decrypt") }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Paste from clipboard
                            val clipData = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipData.primaryClip?.getItemAt(0)?.text?.let { text ->
                                inputText = TextFieldValue(text.toString())
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste")
                    }
                    
                    OutlinedButton(
                        onClick = { inputText = TextFieldValue("") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }
        }
        
        // Password Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Password Options",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = selectedPassword,
                    onValueChange = { selectedPassword = it },
                    label = { Text("Password") },
                    placeholder = { Text("Enter password or use attack to find it") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (selectedPassword.isNotEmpty()) {
                            IconButton(
                                onClick = { selectedPassword = "" }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear password")
                            }
                        }
                    }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            if (inputText.text.isNotEmpty()) {
                                showPasswordDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = inputText.text.isNotEmpty() && !isAttackRunning
                    ) {
                        Icon(Icons.Default.FindInPage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Find Password")
                    }
                    
                    Button(
                        onClick = {
                            if (inputText.text.isNotEmpty() && selectedPassword.isNotEmpty()) {
                                // Simulate decryption with known password
                                outputText = "Decrypting with password: $selectedPassword\n" +
                                        "Decrypted text: ${inputText.text}" // Placeholder
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = inputText.text.isNotEmpty() && selectedPassword.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decrypt")
                    }
                }
            }
        }
        
        // Progress Section (shown when attack is running)
        if (isAttackRunning) {
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
                        text = "Password Recovery in Progress",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    LinearProgressIndicator(
                        progress = attackProgress.progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Current attempt: ${attackProgress.currentAttempt}")
                    Text("Attempts: ${attackProgress.attemptsCount}")
                    
                    if (attackProgress.estimatedTimeRemaining > 0) {
                        Text("Estimated time remaining: ${formatTime(attackProgress.estimatedTimeRemaining)}")
                    }
                    
                    Button(
                        onClick = { viewModel.stopAttack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop Attack")
                    }
                }
            }
        }
        
        // Output Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Output",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (outputText.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(outputText))
                                // Show a snackbar or toast to indicate copy success
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy to clipboard")
                        }
                    }
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = CardDefaults.cardColors(
                        backgroundColor = MaterialTheme.colors.surface
                    )
                ) {
                    SelectionContainer {
                        Text(
                            text = outputText.ifEmpty { "Decrypted text will appear here..." },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            style = MaterialTheme.typography.body1.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (outputText.isEmpty()) 
                                MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            else 
                                MaterialTheme.colors.onSurface
                        )
                    }
                }
                
                if (outputText.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { outputText = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Output")
                    }
                }
            }
        }
    }
    
    // Password Attack Dialog
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Password Recovery Attack") },
            text = { 
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Select attack method to recover the password:")
                    Text(
                        "• Brute Force: Try all possible combinations",
                        style = MaterialTheme.typography.body2
                    )
                    Text(
                        "• Dictionary: Use common passwords list", 
                        style = MaterialTheme.typography.body2
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showPasswordDialog = false
                            viewModel.updateAttackType(AttackType.BRUTE_FORCE)
                            // Create a temporary text file for attack
                            viewModel.startAttack()
                            isDecrypting = true
                        }
                    ) {
                        Text("Brute Force")
                    }
                    TextButton(
                        onClick = {
                            showPasswordDialog = false
                            viewModel.updateAttackType(AttackType.DICTIONARY_ATTACK)
                            viewModel.startAttack()
                            isDecrypting = true
                        }
                    ) {
                        Text("Dictionary")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s" 
        else -> "${seconds}s"
    }
}