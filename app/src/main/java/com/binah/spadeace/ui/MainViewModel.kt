package com.binah.spadeace.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.binah.spadeace.core.DecryptionEngine
import com.binah.spadeace.core.GpuDetector
import com.binah.spadeace.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val decryptionEngine = DecryptionEngine()
    private var currentAttackJob: Job? = null
    private val gpuDetector = GpuDetector(application.applicationContext)
    
    private val _attackConfig = MutableStateFlow(AttackConfiguration())
    val attackConfig: StateFlow<AttackConfiguration> = _attackConfig.asStateFlow()
    
    private val _attackProgress = MutableStateFlow(AttackProgress())
    val attackProgress: StateFlow<AttackProgress> = _attackProgress.asStateFlow()
    
    private val _attackResult = MutableStateFlow<AttackResult?>(null)
    val attackResult: StateFlow<AttackResult?> = _attackResult.asStateFlow()
    
    private val _isAttackRunning = MutableStateFlow(false)
    val isAttackRunning: StateFlow<Boolean> = _isAttackRunning.asStateFlow()
    
    private val _gpuInfo = MutableStateFlow<GpuInfo?>(null)
    val gpuInfo: StateFlow<GpuInfo?> = _gpuInfo.asStateFlow()
    
    init {
        // Detect GPU info on initialization
        viewModelScope.launch {
            _gpuInfo.value = gpuDetector.detectGpuInfo()
        }
    }
    
    fun updateAttackConfig(config: AttackConfiguration) {
        _attackConfig.value = config
    }
    
    fun updateTargetFile(file: File) {
        _attackConfig.value = _attackConfig.value.copy(targetFile = file)
    }
    
    fun updateAttackType(type: AttackType) {
        _attackConfig.value = _attackConfig.value.copy(attackType = type)
    }
    
    fun updateMaxPasswordLength(length: Int) {
        _attackConfig.value = _attackConfig.value.copy(maxPasswordLength = length)
    }
    
    fun updateCharacterSet(charset: String) {
        _attackConfig.value = _attackConfig.value.copy(characterSet = charset)
    }
    
    fun updateDictionaryFile(file: File?) {
        _attackConfig.value = _attackConfig.value.copy(dictionaryFile = file)
    }
    
    fun updateThreadCount(count: Int) {
        _attackConfig.value = _attackConfig.value.copy(threadCount = count)
    }
    
    fun updateOptimizationLevel(level: OptimizationLevel) {
        _attackConfig.value = _attackConfig.value.copy(optimizationLevel = level)
    }
    
    fun updateHardwareAcceleration(acceleration: HardwareAcceleration) {
        _attackConfig.value = _attackConfig.value.copy(hardwareAcceleration = acceleration)
    }
    
    fun updateKeyDerivationMethod(method: KeyDerivationMethod) {
        _attackConfig.value = _attackConfig.value.copy(keyDerivationMethod = method)
    }
    
    fun updateMaskPattern(pattern: String) {
        _attackConfig.value = _attackConfig.value.copy(maskPattern = pattern)
    }
    
    fun updateRainbowTableFile(file: File?) {
        _attackConfig.value = _attackConfig.value.copy(rainbowTableFile = file)
    }
    
    fun updateRuleFile(file: File?) {
        _attackConfig.value = _attackConfig.value.copy(ruleFile = file)

      fun updateGpuAcceleration(enabled: Boolean) {
        _attackConfig.value = _attackConfig.value.copy(enableGpuAcceleration = enabled)
    }
    
    fun getSupportedChipsets(): List<String> {
        return gpuDetector.getSupportedChipsetsList()
    }
    
    fun isHardwareAccelerationSupported(): Boolean {
        return gpuDetector.isHardwareAccelerationSupported()

    }
    
    fun startAttack() {
        if (_isAttackRunning.value) return
        
        _isAttackRunning.value = true
        _attackResult.value = null
        _attackProgress.value = AttackProgress(isRunning = true)
        
        currentAttackJob = viewModelScope.launch {
            try {
                decryptionEngine.startAttack(_attackConfig.value) { progress ->
                    _attackProgress.value = progress
                }.collect { result ->
                    _attackResult.value = result
                    _isAttackRunning.value = false
                    _attackProgress.value = _attackProgress.value.copy(isRunning = false)
                }
            } catch (e: Exception) {
                _attackResult.value = AttackResult(
                    success = false,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
                _isAttackRunning.value = false
                _attackProgress.value = _attackProgress.value.copy(isRunning = false)
            }
        }
    }
    
    fun stopAttack() {
        currentAttackJob?.cancel()
        currentAttackJob = null
        _isAttackRunning.value = false
        _attackProgress.value = _attackProgress.value.copy(isRunning = false)
    }
    
    fun clearResult() {
        _attackResult.value = null
        _attackProgress.value = AttackProgress()
    }
    
    override fun onCleared() {
        super.onCleared()
        stopAttack()
    }
}