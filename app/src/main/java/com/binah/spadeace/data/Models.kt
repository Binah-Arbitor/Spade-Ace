package com.binah.spadeace.data

import java.io.File

data class AttackConfiguration(
    val attackType: AttackType = AttackType.BRUTE_FORCE,
    val targetFile: File? = null,
    val maxPasswordLength: Int = 8,
    val characterSet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
    val dictionaryFile: File? = null,
    val threadCount: Int = Runtime.getRuntime().availableProcessors(),
    val chunkSize: Int = 1024 * 1024, // 1MB chunks
    val optimizationLevel: OptimizationLevel = OptimizationLevel.HIGH,
    val hardwareAcceleration: HardwareAcceleration = HardwareAcceleration.CPU_ONLY,
    val enableGpuAcceleration: Boolean = false
)

enum class AttackType {
    BRUTE_FORCE,
    DICTIONARY_ATTACK
}

enum class OptimizationLevel {
    LOW,
    MEDIUM,
    HIGH,
    EXTREME
}

enum class HardwareAcceleration {
    CPU_ONLY,
    GPU_ASSISTED,
    HYBRID_MODE
}

data class GpuInfo(
    val renderer: String = "Unknown",
    val vendor: String = "Unknown", 
    val version: String = "Unknown",
    val isVulkanSupported: Boolean = false,
    val isOpenClSupported: Boolean = false,
    val chipset: String = "Unknown",
    val supportedComputeUnits: Int = 0
)

data class AttackResult(
    val success: Boolean,
    val foundPassword: String? = null,
    val timeElapsed: Long = 0,
    val attemptsCount: Long = 0,
    val errorMessage: String? = null
)

data class AttackProgress(
    val currentAttempt: String = "",
    val attemptsCount: Long = 0,
    val progress: Float = 0f,
    val estimatedTimeRemaining: Long = 0,
    val isRunning: Boolean = false
)