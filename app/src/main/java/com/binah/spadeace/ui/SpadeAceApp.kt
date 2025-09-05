package com.binah.spadeace.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.platform.LocalContext
import com.binah.spadeace.R
import com.binah.spadeace.ui.screens.DecryptionScreen
import com.binah.spadeace.ui.screens.FileOperationsScreen
import com.binah.spadeace.ui.screens.SettingsScreen
import com.binah.spadeace.ui.screens.TextDecryptionScreen

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SpadeAceApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.h6
                )
            },
            actions = {
                IconButton(onClick = { /* Help action */ }) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = "Help"
                    )
                }
            },
            backgroundColor = MaterialTheme.colors.primarySurface,
            contentColor = MaterialTheme.colors.onSurface
        )
        
        // Navigation Tabs using BottomNavigation for Material2
        BottomNavigation(
            backgroundColor = MaterialTheme.colors.primarySurface
        ) {
            BottomNavigationItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.Security, contentDescription = null) },
                label = { Text(stringResource(R.string.decryption_attack)) }
            )
            BottomNavigationItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                label = { Text("Text Decryption") }
            )
            BottomNavigationItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                label = { Text(stringResource(R.string.file_operations)) }
            )
            BottomNavigationItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text(stringResource(R.string.settings)) }
            )
        }
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> DecryptionScreen(viewModel = viewModel)
                1 -> TextDecryptionScreen(viewModel = viewModel)
                2 -> FileOperationsScreen(viewModel = viewModel)
                3 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}