package com.binah.spadeace.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    private val _isDarkMode = MutableStateFlow(
        when (prefs.getInt(KEY_THEME_MODE, THEME_MODE_SYSTEM)) {
            THEME_MODE_LIGHT -> false
            THEME_MODE_DARK -> true
            else -> false // Will be updated by system theme
        }
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _themeMode = MutableStateFlow(prefs.getInt(KEY_THEME_MODE, THEME_MODE_SYSTEM))
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()
    
    fun setThemeMode(mode: Int) {
        _themeMode.value = mode
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        
        // Update dark mode state based on theme mode
        when (mode) {
            THEME_MODE_LIGHT -> _isDarkMode.value = false
            THEME_MODE_DARK -> _isDarkMode.value = true
            // For THEME_MODE_SYSTEM, this will be updated by the calling composable
        }
    }
    
    fun updateSystemTheme(isSystemDark: Boolean) {
        if (_themeMode.value == THEME_MODE_SYSTEM) {
            _isDarkMode.value = isSystemDark
        }
    }
    
    companion object {
        const val THEME_MODE_SYSTEM = 0
        const val THEME_MODE_LIGHT = 1
        const val THEME_MODE_DARK = 2
        
        private const val KEY_THEME_MODE = "theme_mode"
    }
}