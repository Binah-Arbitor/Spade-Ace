package com.binah.spadeace.data

import java.io.File

data class AttackConfiguration(
    val attackType: AttackType = AttackType.BRUTE_FORCE,
    val targetFile: File? = null,
    val maxPasswordLength: Int = 8,
    val characterSet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
    val dictionaryFile: File? = null,
    val rainbowTableFile: File? = null,
    val maskPattern: String = "?l?l?l?l?d?d?d?d", // ?l=lowercase, ?u=uppercase, ?d=digit, ?s=special
    val ruleFile: File? = null,
    val threadCount: Int = Runtime.getRuntime().availableProcessors(),
    val chunkSize: Int = 1024 * 1024, // 1MB chunks
    val optimizationLevel: OptimizationLevel = OptimizationLevel.HIGH,
    val hardwareAcceleration: HardwareAcceleration = HardwareAcceleration.CPU_ONLY,
    val enableGpuAcceleration: Boolean = false,
    val keyDerivationMethod: KeyDerivationMethod = KeyDerivationMethod.SHA256_SIMPLE,
    val enableSmartPatterns: Boolean = true,
    val commonPasswordsFirst: Boolean = true,
    val skipWeakCombinations: Boolean = false
)

enum class AttackType {
    BRUTE_FORCE,
    DICTIONARY_ATTACK,
    RAINBOW_TABLE,
    HYBRID_ATTACK,
    MASK_ATTACK,
    RULE_BASED_ATTACK,
    SMART_BRUTE_FORCE
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

enum class KeyDerivationMethod {
    SHA256_SIMPLE,
    PBKDF2,
    SCRYPT,
    ARGON2,
    BCRYPT
}

data class GpuInfo(
    val name: String = "Unknown",
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