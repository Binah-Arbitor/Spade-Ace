package com.binah.spadeace.core.crypto

import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.GCMParameterSpec

/**
 * Interface for encryption algorithm implementations
 */
interface EncryptionAlgorithm {
    val name: String
    val supportedModes: List<String>
    val supportedPaddings: List<String>
    val blockSize: Int
    val keyLengths: List<Int>
    
    fun decrypt(data: ByteArray, password: String, mode: String, padding: String): ByteArray?
    fun encrypt(data: ByteArray, password: String, mode: String, padding: String): ByteArray?
    fun isValidCombination(mode: String, padding: String): Boolean
    fun requiresIV(mode: String): Boolean
    fun generateKey(password: String, keyLength: Int): SecretKeySpec
}

/**
 * Base implementation for symmetric encryption algorithms
 */
abstract class BaseSymmetricAlgorithm : EncryptionAlgorithm {
    
    protected fun getTransformation(mode: String, padding: String): String {
        return "$name/$mode/$padding"
    }
    
    override fun isValidCombination(mode: String, padding: String): Boolean {
        return when (mode.uppercase()) {
            "GCM", "CCM" -> padding == "NoPadding"
            "ECB", "CBC" -> padding in listOf("PKCS5Padding", "NoPadding", "ISO10126Padding")
            "CFB", "OFB", "CTR" -> padding == "NoPadding"
            else -> true
        }
    }
    
    override fun requiresIV(mode: String): Boolean {
        return mode.uppercase() !in listOf("ECB")
    }
    
    protected fun generateIV(mode: String): ByteArray {
        val ivSize = when (mode.uppercase()) {
            "GCM" -> 12
            "CCM" -> 7
            else -> blockSize
        }
        return ByteArray(ivSize) { (it % 256).toByte() } // Simplified IV generation
    }
    
    override fun decrypt(data: ByteArray, password: String, mode: String, padding: String): ByteArray? {
        if (!isValidCombination(mode, padding)) return null
        
        return try {
            val key = generateKey(password, keyLengths.firstOrNull() ?: 16)
            val cipher = Cipher.getInstance(getTransformation(mode, padding))
            
            if (requiresIV(mode)) {
                val iv = generateIV(mode)
                when (mode.uppercase()) {
                    "GCM" -> {
                        val gcmSpec = GCMParameterSpec(128, iv)
                        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
                    }
                    else -> {
                        val ivSpec = IvParameterSpec(iv)
                        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
                    }
                }
            } else {
                cipher.init(Cipher.DECRYPT_MODE, key)
            }
            
            cipher.doFinal(data)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun encrypt(data: ByteArray, password: String, mode: String, padding: String): ByteArray? {
        if (!isValidCombination(mode, padding)) return null
        
        return try {
            val key = generateKey(password, keyLengths.firstOrNull() ?: 16)
            val cipher = Cipher.getInstance(getTransformation(mode, padding))
            
            if (requiresIV(mode)) {
                val iv = generateIV(mode)
                when (mode.uppercase()) {
                    "GCM" -> {
                        val gcmSpec = GCMParameterSpec(128, iv)
                        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
                    }
                    else -> {
                        val ivSpec = IvParameterSpec(iv)
                        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
                    }
                }
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key)
            }
            
            cipher.doFinal(data)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun generateKey(password: String, keyLength: Int): SecretKeySpec {
        val hash = password.toByteArray()
        val keyBytes = ByteArray(keyLength)
        System.arraycopy(hash, 0, keyBytes, 0, minOf(hash.size, keyLength))
        return SecretKeySpec(keyBytes, name)
    }
}