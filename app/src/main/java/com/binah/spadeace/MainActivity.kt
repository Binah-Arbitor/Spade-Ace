package com.binah.spadeace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.binah.spadeace.data.ThemePreferences
import com.binah.spadeace.databinding.ActivityMainBinding
import com.binah.spadeace.ui.MainViewModel
import com.binah.spadeace.ui.fragments.DecryptionFragment
import com.binah.spadeace.ui.fragments.FileOperationsFragment
import com.binah.spadeace.ui.fragments.SettingsFragment
import com.binah.spadeace.ui.fragments.TextDecryptionFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]
        
        setupToolbar()
        setupViewPager()
        observeTheme()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }
    
    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.decryption_attack)
                1 -> "Text Decryption"
                2 -> getString(R.string.file_operations)  
                3 -> getString(R.string.settings)
                else -> ""
            }
            tab.setIcon(when (position) {
                0 -> R.drawable.ic_security_24
                1 -> R.drawable.ic_text_fields_24
                2 -> R.drawable.ic_folder_24
                3 -> R.drawable.ic_settings_24
                else -> 0
            })
        }.attach()
    }
    
    private fun observeTheme() {
        lifecycleScope.launch {
            viewModel.themeMode.collect { themeMode ->
                // Handle theme changes if needed
                when (themeMode) {
                    ThemePreferences.THEME_MODE_LIGHT -> {
                        delegate.localNightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                    }
                    ThemePreferences.THEME_MODE_DARK -> {
                        delegate.localNightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                    }
                    else -> {
                        delegate.localNightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                }
            }
        }
    }
    
    private inner class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        
        override fun getItemCount(): Int = 4
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DecryptionFragment.newInstance()
                1 -> TextDecryptionFragment.newInstance()
                2 -> FileOperationsFragment.newInstance()
                3 -> SettingsFragment.newInstance()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}