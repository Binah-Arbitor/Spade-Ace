package com.binah.spadeace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.binah.spadeace.data.ThemePreferences
import com.binah.spadeace.ui.MainViewModel
import com.binah.spadeace.ui.SpadeAceApp
import com.binah.spadeace.ui.theme.SpadeAceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            )
            
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            
            // Update system theme when it changes
            LaunchedEffect(isSystemDark, themeMode) {
                if (themeMode == ThemePreferences.THEME_MODE_SYSTEM) {
                    viewModel.updateSystemTheme(isSystemDark)
                }
            }
            
            SpadeAceTheme(darkTheme = isDarkMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SpadeAceApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SpadeAceTheme {
        // Preview removed as it needs ViewModel context
    }
}