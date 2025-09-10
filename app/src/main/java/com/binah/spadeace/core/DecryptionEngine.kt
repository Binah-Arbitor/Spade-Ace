package com.binah.spadeace.core

import com.binah.spadeace.data.AttackConfiguration
import com.binah.spadeace.data.AttackProgress
import com.binah.spadeace.data.AttackResult
import com.binah.spadeace.data.AttackType
import com.binah.spadeace.data.OptimizationLevel
import com.binah.spadeace.data.HardwareAcceleration
import com.binah.spadeace.data.EncryptionAnalysis
import com.binah.spadeace.data.KeyDerivationMethod

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.crypto.generators.SCrypt
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.KeyParameter
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.Security
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.GCMParameterSpec
import kotlin.coroutines.coroutineContext
import kotlin.math.pow
import kotlin.random.Random

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
            AttackType.RAINBOW_TABLE -> {
                val result = performRainbowTableAttack(config, onProgress)
                emit(result.copy(timeElapsed = System.currentTimeMillis() - startTime))
            }
            AttackType.HYBRID_ATTACK -> {
                val result = performHybridAttack(config, onProgress)
                emit(result.copy(timeElapsed = System.currentTimeMillis() - startTime))
            }
            AttackType.MASK_ATTACK -> {
                val result = performMaskAttack(config, onProgress)
                emit(result.copy(timeElapsed = System.currentTimeMillis() - startTime))
            }
            AttackType.RULE_BASED_ATTACK -> {
                val result = performRuleBasedAttack(config, onProgress)
                emit(result.copy(timeElapsed = System.currentTimeMillis() - startTime))
            }
            AttackType.SMART_BRUTE_FORCE -> {
                val result = performSmartBruteForceAttack(config, onProgress)
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
        val startTime = System.currentTimeMillis()
        
        // Determine threading strategy based on hardware acceleration settings
        val effectiveThreadCount = when (config.hardwareAcceleration) {
            HardwareAcceleration.CPU_ONLY -> config.threadCount
            HardwareAcceleration.GPU_ASSISTED -> if (config.enableGpuAcceleration) 
                minOf(config.threadCount * 2, 16) else config.threadCount
            HardwareAcceleration.HYBRID_MODE -> if (config.enableGpuAcceleration) 
                config.threadCount + 2 else config.threadCount
        }
        
        for (length in 1..maxLength) {
            if (!coroutineContext.isActive) {
                return AttackResult(false, errorMessage = "Attack cancelled")
            }
            
            val result = when (config.hardwareAcceleration) {
                HardwareAcceleration.CPU_ONLY -> generatePasswords(charset, length) { password ->
                    attemptCount++
                    updateProgress(config, attemptCount, totalCombinations, startTime, password, onProgress)
                    tryDecrypt(config.targetFile, password, config.keyDerivationMethod)
                }
                else -> generatePasswordsWithHardwareAcceleration(charset, length, config) { password ->
                    attemptCount++
                    updateProgress(config, attemptCount, totalCombinations, startTime, password, onProgress)
                    tryDecrypt(config.targetFile, password, config.keyDerivationMethod)
                }
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
                    
                    if (tryDecrypt(config.targetFile, password.trim(), config.keyDerivationMethod)) {
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
    
    private suspend fun generatePasswordsWithHardwareAcceleration(
        charset: CharArray,
        length: Int,
        config: AttackConfiguration,
        onPasswordGenerated: suspend (String) -> Boolean
    ): String? {
        val password = CharArray(length)
        
        // For simplicity, simulate hardware acceleration by using more aggressive parallelization
        // In a real implementation, this would use GPU compute shaders or OpenCL/Vulkan compute
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
            // Simulate hardware acceleration by using different dispatching strategy
            when (config.hardwareAcceleration) {
                HardwareAcceleration.GPU_ASSISTED -> {
                    // Use more parallel processing to simulate GPU acceleration
                    generate(0)
                }
                HardwareAcceleration.HYBRID_MODE -> {
                    // Balance CPU and simulated GPU work
                    generate(0)
                }
                else -> generate(0)
            }
        }
    }
    

    private fun tryDecrypt(file: File?, password: String, keyDerivationMethod: KeyDerivationMethod = KeyDerivationMethod.SHA256_SIMPLE): Boolean {
        if (file == null || !file.exists()) return false
        
        return try {
            // Expanded list of encryption algorithms
            val algorithms = listOf(
                "AES", "DES", "3DES", "Blowfish", 
                "Twofish", "Serpent", "CAST5", "CAST6",
                "RC2", "RC4", "RC5", "RC6",
                "Camellia", "IDEA", "Skipjack"
            )
            
            // Expanded cipher modes  
            val modes = listOf("ECB", "CBC", "CFB", "OFB", "CTR", "GCM", "CCM")
            
            // Expanded padding schemes
            val paddings = listOf(
                "PKCS5Padding", "NoPadding", "PKCS1Padding",
                "OAEPPadding", "ISO10126Padding", "X9.23Padding"
            )
            
            for (algorithm in algorithms) {
                for (mode in modes) {
                    // Skip unsupported combinations
                    if (isUnsupportedCombination(algorithm, mode)) continue
                    
                    for (padding in paddings) {
                        // Skip invalid padding for certain modes
                        if (isInvalidPadding(mode, padding)) continue
                        
                        try {
                            val transformation = "$algorithm/$mode/$padding"
                            val key = generateKey(password, algorithm, keyDerivationMethod)
                            val cipher = Cipher.getInstance(transformation)
                            
                            // Handle different modes that require IV
                            if (requiresIV(mode)) {
                                val iv = generateIV(algorithm, mode)
                                if (mode == "GCM") {
                                    val gcmSpec = GCMParameterSpec(128, iv)
                                    cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
                                } else {
                                    val ivSpec = IvParameterSpec(iv)
                                    cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
                                }
                            } else {
                                cipher.init(Cipher.DECRYPT_MODE, key)
                            }
                            
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
    
    private fun generateKey(password: String, algorithm: String, keyDerivationMethod: KeyDerivationMethod = KeyDerivationMethod.SHA256_SIMPLE): SecretKeySpec {
        val keyLength = when (algorithm) {
            "AES" -> 32 // Support both AES-128 and AES-256
            "DES" -> 8
            "3DES" -> 24
            "Blowfish" -> 16
            "Twofish" -> 32
            "Serpent" -> 32
            "CAST5" -> 16
            "CAST6" -> 32
            "RC2" -> 16
            "RC4" -> 16
            "RC5" -> 16
            "RC6" -> 32
            "Camellia" -> 32
            "IDEA" -> 16
            "Skipjack" -> 10
            else -> 16
        }
        
        val keyBytes = when (keyDerivationMethod) {
            KeyDerivationMethod.SHA256_SIMPLE -> {
                val md = MessageDigest.getInstance("SHA-256")
                md.digest(password.toByteArray()).copyOf(keyLength)
            }
            KeyDerivationMethod.PBKDF2 -> {
                val salt = "SpadeAce".toByteArray() // In real implementation, use random salt
                val generator = PKCS5S2ParametersGenerator(SHA256Digest())
                generator.init(password.toByteArray(), salt, 10000)
                val key = generator.generateDerivedParameters(keyLength * 8) as KeyParameter
                key.key.copyOf(keyLength)
            }
            KeyDerivationMethod.SCRYPT -> {
                val salt = "SpadeAce".toByteArray()
                SCrypt.generate(password.toByteArray(), salt, 16384, 8, 1, keyLength)
            }
            KeyDerivationMethod.ARGON2 -> {
                // Simplified Argon2 implementation using multiple hashing rounds
                var hash = password.toByteArray()
                repeat(1000) {
                    val md = MessageDigest.getInstance("SHA-256")
                    md.update(hash)
                    md.update("argon2_salt".toByteArray())
                    hash = md.digest()
                }
                hash.copyOf(keyLength)
            }
            KeyDerivationMethod.BCRYPT -> {
                // Simplified bcrypt-like implementation
                var hash = password.toByteArray()
                repeat(12) { // 12 rounds similar to bcrypt cost factor
                    val md = MessageDigest.getInstance("SHA-256")
                    md.update(hash)
                    md.update("bcrypt_salt".toByteArray())
                    hash = md.digest()
                }
                hash.copyOf(keyLength)
            }
        }
        
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
    
    // New attack methods
    private suspend fun performRainbowTableAttack(
        config: AttackConfiguration,
        onProgress: (AttackProgress) -> Unit
    ): AttackResult {
        val rainbowFile = config.rainbowTableFile 
            ?: return AttackResult(false, errorMessage = "Rainbow table file not specified")
        
        if (!rainbowFile.exists()) {
            return AttackResult(false, errorMessage = "Rainbow table file not found")
        }
        
        return withContext(Dispatchers.IO) {
            var attemptCount = 0L
            val totalLines = countLines(rainbowFile)
            val startTime = System.currentTimeMillis()
            
            rainbowFile.useLines { lines ->
                for (line in lines) {
                    if (!coroutineContext.isActive) {
                        return@withContext AttackResult(false, errorMessage = "Attack cancelled")
                    }
                    
                    attemptCount++
                    val parts = line.trim().split(":")
                    if (parts.size >= 2) {
                        val password = parts[1] // Assuming hash:password format
                        
                        updateProgress(config, attemptCount, totalLines, startTime, password, onProgress)
                        
                        if (tryDecrypt(config.targetFile, password, config.keyDerivationMethod)) {
                            return@withContext AttackResult(
                                success = true,
                                foundPassword = password,
                                attemptsCount = attemptCount
                            )
                        }
                    }
                }
                
                AttackResult(
                    success = false,
                    attemptsCount = attemptCount,
                    errorMessage = "Password not found in rainbow table"
                )
            }
        }
    }
    
    private suspend fun performHybridAttack(
        config: AttackConfiguration,
        onProgress: (AttackProgress) -> Unit
    ): AttackResult {
        // First try dictionary attack, then brute force on successful dictionary candidates
        val dictionaryResult = performDictionaryAttack(config, onProgress)
        if (dictionaryResult.success) return dictionaryResult
        
        // If dictionary fails, try brute force with common patterns
        val modifiedConfig = config.copy(
            attackType = AttackType.SMART_BRUTE_FORCE,
            enableSmartPatterns = true,
            commonPasswordsFirst = true
        )
        return performSmartBruteForceAttack(modifiedConfig, onProgress)
    }
    
    private suspend fun performMaskAttack(
        config: AttackConfiguration,
        onProgress: (AttackProgress) -> Unit
    ): AttackResult {
        val maskPattern = config.maskPattern
        val combinations = calculateMaskCombinations(maskPattern)
        var attemptCount = 0L
        val startTime = System.currentTimeMillis()
        
        return generateMaskPasswords(maskPattern) { password ->
            if (!coroutineContext.isActive) return@generateMaskPasswords false
            
            attemptCount++
            updateProgress(config, attemptCount, combinations, startTime, password, onProgress)
            
            if (tryDecrypt(config.targetFile, password, config.keyDerivationMethod)) {
                return AttackResult(
                    success = true,
                    foundPassword = password,
                    attemptsCount = attemptCount
                )
            }
            false
        } ?: AttackResult(
            success = false,
            attemptsCount = attemptCount,
            errorMessage = "Password not found using mask pattern"
        )
    }
    
    private suspend fun performRuleBasedAttack(
        config: AttackConfiguration,
        onProgress: (AttackProgress) -> Unit
    ): AttackResult {
        val dictionaryFile = config.dictionaryFile 
            ?: return AttackResult(false, errorMessage = "Dictionary file required for rule-based attack")
        
        val ruleFile = config.ruleFile
            ?: return AttackResult(false, errorMessage = "Rule file not specified")
        
        if (!dictionaryFile.exists() || !ruleFile.exists()) {
            return AttackResult(false, errorMessage = "Dictionary or rule file not found")
        }
        
        return withContext(Dispatchers.IO) {
            var attemptCount = 0L
            val startTime = System.currentTimeMillis()
            val rules = loadRules(ruleFile)
            val totalCombinations = countLines(dictionaryFile) * rules.size
            
            dictionaryFile.useLines { words ->
                for (baseWord in words) {
                    for (rule in rules) {
                        if (!coroutineContext.isActive) {
                            return@withContext AttackResult(false, errorMessage = "Attack cancelled")
                        }
                        
                        attemptCount++
                        val password = applyRule(baseWord.trim(), rule)
                        
                        updateProgress(config, attemptCount, totalCombinations.toLong(), startTime, password, onProgress)
                        
                        if (tryDecrypt(config.targetFile, password, config.keyDerivationMethod)) {
                            return@withContext AttackResult(
                                success = true,
                                foundPassword = password,
                                attemptsCount = attemptCount
                            )
                        }
                    }
                }
                
                AttackResult(
                    success = false,
                    attemptsCount = attemptCount,
                    errorMessage = "Password not found using rule-based attack"
                )
            }
        }
    }
    
    private suspend fun performSmartBruteForceAttack(
        config: AttackConfiguration,
        onProgress: (AttackProgress) -> Unit
    ): AttackResult {
        // Smart brute force uses common patterns and optimizations
        val commonPatterns = if (config.commonPasswordsFirst) {
            listOf("password", "123456", "qwerty", "admin", "letmein", "welcome", "monkey", "dragon")
        } else emptyList()
        
        // Try common patterns first
        var attemptCount = 0L
        val startTime = System.currentTimeMillis()
        
        for (pattern in commonPatterns) {
            if (!coroutineContext.isActive) {
                return AttackResult(false, errorMessage = "Attack cancelled")
            }
            
            attemptCount++
            updateProgress(config, attemptCount, commonPatterns.size.toLong(), startTime, pattern, onProgress)
            
            if (tryDecrypt(config.targetFile, pattern, config.keyDerivationMethod)) {
                return AttackResult(
                    success = true,
                    foundPassword = pattern,
                    attemptsCount = attemptCount
                )
            }
        }
        
        // Fall back to regular brute force with optimizations
        return performBruteForceAttack(config, onProgress)
    }

    
    // Helper functions for new features
    private fun updateProgress(
        config: AttackConfiguration,
        attemptCount: Long,
        totalCombinations: Long,
        startTime: Long,
        currentPassword: String,
        onProgress: (AttackProgress) -> Unit
    ) {
        val updateInterval = when (config.optimizationLevel) {
            OptimizationLevel.LOW -> 100
            OptimizationLevel.MEDIUM -> 500
            OptimizationLevel.HIGH -> 1000
            OptimizationLevel.EXTREME -> 5000
        }
        
        if (attemptCount % updateInterval == 0L) {
            val progress = attemptCount.toFloat() / totalCombinations
            val estimatedTimeRemaining = estimateTimeRemaining(attemptCount, totalCombinations, startTime)
            
            onProgress(AttackProgress(
                currentAttempt = currentPassword,
                attemptsCount = attemptCount,
                progress = progress,
                estimatedTimeRemaining = estimatedTimeRemaining,
                isRunning = true
            ))
        }
    }
    
    private fun isUnsupportedCombination(algorithm: String, mode: String): Boolean {
        // Some algorithms don't support certain modes
        return when {
            algorithm == "RC4" && mode != "ECB" -> true // RC4 is stream cipher
            algorithm == "DES" && mode in listOf("GCM", "CCM") -> true // DES doesn't support authenticated modes
            else -> false
        }
    }
    
    private fun isInvalidPadding(mode: String, padding: String): Boolean {
        return when {
            mode in listOf("CTR", "CFB", "OFB") && padding != "NoPadding" -> true // Stream modes don't use padding
            mode in listOf("GCM", "CCM") && padding != "NoPadding" -> true // Authenticated modes handle padding internally
            else -> false
        }
    }
    
    private fun requiresIV(mode: String): Boolean {
        return mode in listOf("CBC", "CFB", "OFB", "CTR", "GCM", "CCM")
    }
    
    private fun generateIV(algorithm: String, mode: String): ByteArray {
        val blockSize = when (algorithm) {
            "AES" -> 16
            "DES", "3DES" -> 8
            "Blowfish" -> 8
            else -> 16
        }
        
        val ivSize = when (mode) {
            "GCM", "CCM" -> 12 // Recommended IV size for GCM/CCM
            else -> blockSize
        }
        
        val iv = ByteArray(ivSize)
        SecureRandom().nextBytes(iv)
        return iv
    }
    
    private fun calculateMaskCombinations(mask: String): Long {
        var combinations = 1L
        var i = 0
        while (i < mask.length) {
            if (mask[i] == '?' && i + 1 < mask.length) {
                when (mask[i + 1]) {
                    'l' -> combinations *= 26 // lowercase
                    'u' -> combinations *= 26 // uppercase  
                    'd' -> combinations *= 10 // digits
                    's' -> combinations *= 32 // special chars
                    'a' -> combinations *= 95 // all printable
                }
                i += 2
            } else {
                i++
            }
        }
        return combinations
    }
    
    private suspend fun generateMaskPasswords(mask: String, onPassword: suspend (String) -> Boolean): String? {
        return withContext(Dispatchers.Default) {
            generateMaskRecursive(mask, 0, StringBuilder()) { password ->
                kotlinx.coroutines.runBlocking { onPassword(password) }
            }
        }
    }
    
    private fun generateMaskRecursive(mask: String, pos: Int, current: StringBuilder, onPassword: (String) -> Boolean): String? {
        if (pos >= mask.length) {
            val password = current.toString()
            return if (onPassword(password)) password else null
        }
        
        if (mask[pos] == '?' && pos + 1 < mask.length) {
            val charset = when (mask[pos + 1]) {
                'l' -> "abcdefghijklmnopqrstuvwxyz"
                'u' -> "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                'd' -> "0123456789"
                's' -> "!@#$%^&*()_+-=[]{}|;:,.<>?"
                'a' -> "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{}|;:,.<>?"
                else -> "?"
            }
            
            for (char in charset) {
                current.append(char)
                val result = generateMaskRecursive(mask, pos + 2, current, onPassword)
                if (result != null) return result
                current.deleteCharAt(current.length - 1)
            }
        } else {
            current.append(mask[pos])
            val result = generateMaskRecursive(mask, pos + 1, current, onPassword)
            if (result != null) return result
            current.deleteCharAt(current.length - 1)
        }
        
        return null
    }
    
    private fun loadRules(ruleFile: File): List<String> {
        return try {
            ruleFile.readLines().filter { it.isNotBlank() && !it.startsWith("#") }
        } catch (e: Exception) {
            // Default rules if file can't be read
            listOf(
                ":", // No change
                "$1", "$2", "$3", // Append digits
                "^1", "^2", "^3", // Prepend digits  
                "u", "l", "c", // Upper, lower, capitalize
                "r", // Reverse
                "d", // Duplicate
            )
        }
    }
    
    private fun applyRule(word: String, rule: String): String {
        return when {
            rule == ":" -> word
            rule.startsWith("$") -> word + rule.substring(1) // Append
            rule.startsWith("^") -> rule.substring(1) + word // Prepend
            rule == "u" -> word.uppercase()
            rule == "l" -> word.lowercase()
            rule == "c" -> word.lowercase().replaceFirstChar { it.uppercase() }
            rule == "r" -> word.reversed()
            rule == "d" -> word + word
            else -> word
        }
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
    
    /**
     * Analyzes an encrypted file to infer possible encryption methods and padding
     */
    fun analyzeFile(file: File?): EncryptionAnalysis {
        if (file == null || !file.exists()) {
            return EncryptionAnalysis(
                analysisNotes = listOf("File not found or inaccessible")
            )
        }
        
        val fileSize = file.length()
        val analysisNotes = mutableListOf<String>()
        val possibleAlgorithms = mutableListOf<String>()
        val possibleModes = mutableListOf<String>()
        val possiblePaddings = mutableListOf<String>()
        
        // Read first few bytes for signature analysis
        val headerBytes = ByteArray(minOf(1024, fileSize.toInt()))
        try {
            FileInputStream(file).use { fis ->
                fis.read(headerBytes)
            }
        } catch (e: Exception) {
            return EncryptionAnalysis(
                fileSize = fileSize,
                analysisNotes = listOf("Could not read file: ${e.message}")
            )
        }
        
        // Analyze file size for block alignment
        val blockSizeAlignment = analyzeBlockAlignment(fileSize)
        analysisNotes.add("File size: $fileSize bytes")
        
        // Check for common encrypted file signatures
        val detectedFormat = detectFileFormat(headerBytes)
        
        // Analyze block size alignment to suggest algorithms
        when (blockSizeAlignment) {
            16 -> {
                possibleAlgorithms.addAll(listOf("AES", "Twofish", "Serpent", "CAST6", "Camellia"))
                analysisNotes.add("16-byte alignment suggests AES, Twofish, Serpent, CAST6, or Camellia")
            }
            8 -> {
                possibleAlgorithms.addAll(listOf("DES", "3DES", "Blowfish", "CAST5"))
                analysisNotes.add("8-byte alignment suggests DES, 3DES, Blowfish, or CAST5")
            }
            else -> {
                possibleAlgorithms.addAll(listOf("RC4", "RC5", "RC6")) // Stream ciphers or variable block
                analysisNotes.add("No clear block alignment - might be stream cipher or custom implementation")
            }
        }
        
        // Suggest modes based on file size patterns
        if (fileSize % blockSizeAlignment == 0 && blockSizeAlignment > 0) {
            possibleModes.addAll(listOf("ECB", "CBC"))
            possiblePaddings.addAll(listOf("PKCS5Padding", "NoPadding"))
            analysisNotes.add("Perfect block alignment suggests ECB or CBC mode")
        } else {
            possibleModes.addAll(listOf("CFB", "OFB", "CTR", "GCM"))
            possiblePaddings.add("NoPadding")
            analysisNotes.add("Non-aligned size suggests stream mode (CFB, OFB, CTR) or authenticated mode (GCM)")
        }
        
        // Check entropy patterns (simplified)
        val entropy = calculateSimpleEntropy(headerBytes)
        if (entropy < 7.0) {
            analysisNotes.add("Lower entropy detected - file might not be encrypted or uses weak encryption")
        } else {
            analysisNotes.add("High entropy detected - consistent with encrypted data")
        }
        
        // Calculate confidence based on analysis
        var confidence = 0.5f
        if (blockSizeAlignment > 0) confidence += 0.2f
        if (entropy >= 7.0) confidence += 0.2f
        if (detectedFormat != null) confidence += 0.1f
        
        return EncryptionAnalysis(
            possibleAlgorithms = possibleAlgorithms,
            possibleModes = possibleModes,
            possiblePaddings = possiblePaddings,
            fileSize = fileSize,
            blockSizeAlignment = blockSizeAlignment,
            hasFileSignature = detectedFormat != null,
            detectedFormat = detectedFormat,
            confidence = confidence,
            analysisNotes = analysisNotes
        )
    }
    
    private fun analyzeBlockAlignment(fileSize: Long): Int {
        return when {
            fileSize % 16 == 0L -> 16  // AES, Twofish, Serpent
            fileSize % 8 == 0L -> 8    // DES, 3DES, Blowfish
            else -> 0  // No clear alignment or stream cipher
        }
    }
    
    private fun detectFileFormat(headerBytes: ByteArray): String? {
        if (headerBytes.size < 16) return null
        
        // Check for common encrypted file signatures
        return when {
            // PGP encrypted file
            headerBytes[0] == 0x85.toByte() -> "PGP Encrypted"
            // ZIP with encryption
            headerBytes[0] == 0x50.toByte() && headerBytes[1] == 0x4B.toByte() -> "Encrypted ZIP"
            // 7z with encryption  
            headerBytes.take(6).toByteArray().contentEquals("7z\u00BC\u00AF'\u001C".toByteArray()) -> "Encrypted 7Z"
            // RAR with encryption
            headerBytes.take(4).toByteArray().contentEquals("Rar!".toByteArray()) -> "Encrypted RAR"
            else -> null
        }
    }
    
    private fun calculateSimpleEntropy(data: ByteArray): Double {
        if (data.isEmpty()) return 0.0
        
        val frequencies = IntArray(256)
        for (byte in data) {
            frequencies[byte.toInt() and 0xFF]++
        }
        
        var entropy = 0.0
        val length = data.size.toDouble()
        
        for (freq in frequencies) {
            if (freq > 0) {
                val probability = freq / length
                entropy -= probability * (kotlin.math.ln(probability) / kotlin.math.ln(2.0))
            }
        }
        
        return entropy
    }
}