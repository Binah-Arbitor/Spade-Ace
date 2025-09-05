package com.binah.spadeace.core

import com.binah.spadeace.data.AttackConfiguration
import com.binah.spadeace.data.AttackProgress
import com.binah.spadeace.data.AttackResult
import com.binah.spadeace.data.AttackType
import com.binah.spadeace.data.OptimizationLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.coroutineContext
import kotlin.math.pow

class DecryptionEngine {
    
    init {
        Security.addProvider(BouncyCastleProvider())
    }
    
    suspend fun startAttack(
        config: AttackConfiguration,
        onProgress: (AttackProgress) -> Unit
    ): Flow<AttackResult> = flow {
        val startTime = System.currentTimeMillis()
        
        when (config.attackType) {
            AttackType.BRUTE_FORCE -> {
                val result = performBruteForceAttack(config, onProgress)
                emit(result.copy(timeElapsed = System.currentTimeMillis() - startTime))
            }
            AttackType.DICTIONARY_ATTACK -> {
                val result = performDictionaryAttack(config, onProgress)
                emit(result.copy(timeElapsed = System.currentTimeMillis() - startTime))
            }
        }
    }.flowOn(Dispatchers.Default)
    
    private suspend fun performBruteForceAttack(
        config: AttackConfiguration,
        onProgress: (AttackProgress) -> Unit
    ): AttackResult {
        val charset = config.characterSet.toCharArray()
        val maxLength = config.maxPasswordLength
        var attemptCount = 0L
        val totalCombinations = calculateTotalCombinations(charset.size, maxLength)
        
        for (length in 1..maxLength) {
            if (!coroutineContext.isActive) {
                return AttackResult(false, errorMessage = "Attack cancelled")
            }
            
            val result = generatePasswords(charset, length) { password ->
                attemptCount++
                
                // Update progress every 1000 attempts or based on optimization level
                val updateInterval = when (config.optimizationLevel) {
                    OptimizationLevel.LOW -> 100
                    OptimizationLevel.MEDIUM -> 500
                    OptimizationLevel.HIGH -> 1000
                    OptimizationLevel.EXTREME -> 5000
                }
                
                if (attemptCount % updateInterval == 0L) {
                    val progress = attemptCount.toFloat() / totalCombinations
                    val estimatedTimeRemaining = estimateTimeRemaining(
                        attemptCount, 
                        totalCombinations, 
                        System.currentTimeMillis()
                    )
                    
                    onProgress(AttackProgress(
                        currentAttempt = password,
                        attemptsCount = attemptCount,
                        progress = progress,
                        estimatedTimeRemaining = estimatedTimeRemaining,
                        isRunning = true
                    ))
                }
                
                tryDecrypt(config.targetFile, password)
            }
            
            if (result != null) {
                return AttackResult(
                    success = true,
                    foundPassword = result,
                    attemptsCount = attemptCount
                )
            }
        }
        
        return AttackResult(
            success = false,
            attemptsCount = attemptCount,
            errorMessage = "Password not found within specified parameters"
        )
    }
    
    private suspend fun performDictionaryAttack(
        config: AttackConfiguration,
        onProgress: (AttackProgress) -> Unit
    ): AttackResult {
        val dictionaryFile = config.dictionaryFile 
            ?: return AttackResult(false, errorMessage = "Dictionary file not specified")
        
        if (!dictionaryFile.exists()) {
            return AttackResult(false, errorMessage = "Dictionary file not found")
        }
        
        return withContext(Dispatchers.IO) {
            var attemptCount = 0L
            val totalLines = countLines(dictionaryFile)
            
            dictionaryFile.useLines { lines ->
                for (password in lines) {
                    if (!coroutineContext.isActive) {
                        return@withContext AttackResult(false, errorMessage = "Attack cancelled")
                    }
                    
                    attemptCount++
                    
                    val progress = attemptCount.toFloat() / totalLines
                    onProgress(AttackProgress(
                        currentAttempt = password,
                        attemptsCount = attemptCount,
                        progress = progress,
                        isRunning = true
                    ))
                    
                    if (tryDecrypt(config.targetFile, password.trim())) {
                        return@withContext AttackResult(
                            success = true,
                            foundPassword = password.trim(),
                            attemptsCount = attemptCount
                        )
                    }
                }
                
                AttackResult(
                    success = false,
                    attemptsCount = attemptCount,
                    errorMessage = "Password not found in dictionary"
                )
            }
        }
    }
    
    private suspend fun generatePasswords(
        charset: CharArray,
        length: Int,
        onPasswordGenerated: suspend (String) -> Boolean
    ): String? {
        val password = CharArray(length)
        
        fun generate(pos: Int): String? {
            if (pos == length) {
                val pwd = String(password)
                return if (kotlinx.coroutines.runBlocking { onPasswordGenerated(pwd) }) pwd else null
            }
            
            for (char in charset) {
                password[pos] = char
                val result = generate(pos + 1)
                if (result != null) return result
            }
            return null
        }
        
        return withContext(Dispatchers.Default) {
            generate(0)
        }
    }
    
    private fun tryDecrypt(file: File?, password: String): Boolean {
        if (file == null || !file.exists()) return false
        
        return try {
            // Try multiple encryption algorithms
            val algorithms = listOf("AES", "DES", "3DES", "Blowfish")
            val modes = listOf("ECB", "CBC")
            val paddings = listOf("PKCS5Padding", "NoPadding")
            
            for (algorithm in algorithms) {
                for (mode in modes) {
                    for (padding in paddings) {
                        try {
                            val transformation = "$algorithm/$mode/$padding"
                            val key = generateKey(password, algorithm)
                            val cipher = Cipher.getInstance(transformation)
                            cipher.init(Cipher.DECRYPT_MODE, key)
                            
                            // Try to decrypt first few bytes
                            val testData = ByteArray(minOf(1024, file.length().toInt()))
                            FileInputStream(file).use { fis ->
                                fis.read(testData)
                            }
                            
                            cipher.doFinal(testData)
                            return true // Successful decryption
                        } catch (e: Exception) {
                            // Continue with next combination
                        }
                    }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun generateKey(password: String, algorithm: String): SecretKeySpec {
        val keyLength = when (algorithm) {
            "AES" -> 16
            "DES" -> 8
            "3DES" -> 24
            "Blowfish" -> 16
            else -> 16
        }
        
        val md = MessageDigest.getInstance("SHA-256")
        val keyBytes = md.digest(password.toByteArray()).copyOf(keyLength)
        return SecretKeySpec(keyBytes, algorithm)
    }
    
    private fun calculateTotalCombinations(charsetSize: Int, maxLength: Int): Long {
        var total = 0L
        for (length in 1..maxLength) {
            total += charsetSize.toDouble().pow(length.toDouble()).toLong()
        }
        return total
    }
    
    private fun countLines(file: File): Long {
        return file.useLines { it.count().toLong() }
    }
    
    private fun estimateTimeRemaining(
        currentAttempts: Long,
        totalCombinations: Long,
        startTime: Long
    ): Long {
        val elapsedTime = System.currentTimeMillis() - startTime
        if (currentAttempts == 0L) return 0L
        
        val avgTimePerAttempt = elapsedTime.toDouble() / currentAttempts
        val remainingAttempts = totalCombinations - currentAttempts
        return (remainingAttempts * avgTimePerAttempt).toLong()
    }
}