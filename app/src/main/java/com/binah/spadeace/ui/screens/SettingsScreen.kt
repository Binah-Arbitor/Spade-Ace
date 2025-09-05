package com.binah.spadeace.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.binah.spadeace.R
import com.binah.spadeace.data.OptimizationLevel
import com.binah.spadeace.data.HardwareAcceleration
import com.binah.spadeace.data.ThemePreferences
import com.binah.spadeace.ui.MainViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val attackConfig by viewModel.attackConfig.collectAsState()
    val gpuInfo by viewModel.gpuInfo.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isHardwareSupported = viewModel.isHardwareAccelerationSupported()
    val supportedChipsets = viewModel.getSupportedChipsets()
    
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
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure performance and optimization settings",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
        
        // Theme Settings
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary
                    )
                    Text(
                        text = stringResource(R.string.appearance),
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Text(
                    text = stringResource(R.string.theme_mode),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )
                
                Column {
                    val themeOptions = listOf(
                        ThemePreferences.THEME_MODE_SYSTEM to stringResource(R.string.system_default),
                        ThemePreferences.THEME_MODE_LIGHT to stringResource(R.string.light_theme),
                        ThemePreferences.THEME_MODE_DARK to stringResource(R.string.dark_theme)
                    )
                    
                    themeOptions.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) }
                            )
                            Text(
                                text = label,
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                }
            }
        }
        
        // Performance Settings
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.performance_mode),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                // Optimization Level
                Text(
                    text = stringResource(R.string.optimization_level),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )
                
                Column {
                    OptimizationLevel.values().forEach { level ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = attackConfig.optimizationLevel == level,
                                    onClick = { viewModel.updateOptimizationLevel(level) }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = attackConfig.optimizationLevel == level,
                                onClick = { viewModel.updateOptimizationLevel(level) }
                            )
                            Column(
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = when (level) {
                                        OptimizationLevel.LOW -> stringResource(R.string.low)
                                        OptimizationLevel.MEDIUM -> stringResource(R.string.medium)
                                        OptimizationLevel.HIGH -> stringResource(R.string.high)
                                        OptimizationLevel.EXTREME -> stringResource(R.string.extreme)
                                    },
                                    style = MaterialTheme.typography.body1,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = when (level) {
                                        OptimizationLevel.LOW -> "Balanced performance and battery usage"
                                        OptimizationLevel.MEDIUM -> "Good performance with moderate power consumption"
                                        OptimizationLevel.HIGH -> "Maximum performance, higher power usage"
                                        OptimizationLevel.EXTREME -> "Extreme performance, maximum power consumption"
                                    },
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Hardware Acceleration Settings
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.hardware_acceleration),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                // GPU Acceleration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.enable_gpu_acceleration),
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isHardwareSupported) stringResource(R.string.gpu_acceleration_available) 
                                   else stringResource(R.string.gpu_acceleration_not_supported),
                            style = MaterialTheme.typography.body2,
                            color = if (isHardwareSupported) MaterialTheme.colors.primary 
                                   else MaterialTheme.colors.error
                        )
                    }
                    Switch(
                        checked = attackConfig.enableGpuAcceleration,
                        onCheckedChange = viewModel::updateGpuAcceleration,
                        enabled = isHardwareSupported
                    )
                }
                
                // Hardware Acceleration Mode
                if (attackConfig.enableGpuAcceleration && isHardwareSupported) {
                    Text(
                        text = stringResource(R.string.acceleration_mode),
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Column {
                        HardwareAcceleration.values().forEach { mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = attackConfig.hardwareAcceleration == mode,
                                        onClick = { viewModel.updateHardwareAcceleration(mode) }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = attackConfig.hardwareAcceleration == mode,
                                    onClick = { viewModel.updateHardwareAcceleration(mode) }
                                )
                                Column(
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = when (mode) {
                                            HardwareAcceleration.CPU_ONLY -> stringResource(R.string.cpu_only)
                                            HardwareAcceleration.GPU_ASSISTED -> stringResource(R.string.gpu_assisted)
                                            HardwareAcceleration.HYBRID_MODE -> stringResource(R.string.hybrid_mode)
                                        },
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = when (mode) {
                                            HardwareAcceleration.CPU_ONLY -> "Use CPU cores only for processing"
                                            HardwareAcceleration.GPU_ASSISTED -> "Leverage GPU for parallel computation"
                                            HardwareAcceleration.HYBRID_MODE -> "Balance CPU and GPU workload"
                                        },
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Current GPU Information
                if (gpuInfo != null) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = stringResource(R.string.current_gpu_info),
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )
                    InfoRow("GPU Renderer", gpuInfo.renderer)
                    InfoRow("GPU Vendor", gpuInfo.vendor)
                    InfoRow("Chipset", gpuInfo.chipset)
                    InfoRow("Vulkan Support", if (gpuInfo.isVulkanSupported) "Yes" else "No")
                    InfoRow("Compute Units", "${gpuInfo.supportedComputeUnits}")
                }
            }
        }
        
        // Threading Settings
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Threading Configuration",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                // Thread Count
                Column {
                    Text(
                        text = "${stringResource(R.string.thread_count)}: ${attackConfig.threadCount}",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Available CPU cores: ${Runtime.getRuntime().availableProcessors()}",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = attackConfig.threadCount.toFloat(),
                        onValueChange = { viewModel.updateThreadCount(it.toInt()) },
                        valueRange = 1f..Runtime.getRuntime().availableProcessors().toFloat() * 2,
                        steps = (Runtime.getRuntime().availableProcessors() * 2) - 2
                    )
                }
                
                // Chunk Size
                Column {
                    Text(
                        text = "${stringResource(R.string.chunk_size)}: ${formatBytes(attackConfig.chunkSize)}",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Memory chunk size for file processing",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // Note: Chunk size slider would need additional logic
                    // For now, displaying current value
                }
            }
        }
        
        // Supported Chipsets Information
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.supported_chipsets),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.chipset_support_description),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(supportedChipsets) { chipset ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colors.primary
                            )
                            Text(
                                text = chipset,
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Security Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                backgroundColor = MaterialTheme.colors.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = "Security Notice",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "This tool is designed for educational and authorized penetration testing purposes only. " +
                            "Ensure you have proper authorization before attempting to decrypt any files.",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
        
        // System Information
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "System Information",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                InfoRow("Target SDK", "Android 16+")
                InfoRow("Available Processors", "${Runtime.getRuntime().availableProcessors()}")
                InfoRow("Max Memory", formatBytes(Runtime.getRuntime().maxMemory()))
                InfoRow("Free Memory", formatBytes(Runtime.getRuntime().freeMemory()))
                InfoRow("Encryption Provider", "BouncyCastle")
            }
        }
        
        // Action Buttons
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Reset to defaults
                            viewModel.updateOptimizationLevel(OptimizationLevel.HIGH)
                            viewModel.updateThreadCount(Runtime.getRuntime().availableProcessors())
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset")
                    }
                    
                    Button(
                        onClick = {
                            // Force garbage collection for better performance
                            System.gc()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimize")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}