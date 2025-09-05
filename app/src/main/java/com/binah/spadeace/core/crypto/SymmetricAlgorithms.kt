package com.binah.spadeace.core.crypto

import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec

/**
 * AES encryption algorithm implementation
 */
class AESAlgorithm : BaseSymmetricAlgorithm() {
    override val name: String = "AES"
    override val supportedModes: List<String> = listOf("ECB", "CBC", "CFB", "OFB", "CTR", "GCM", "CCM")
    override val supportedPaddings: List<String> = listOf("PKCS5Padding", "NoPadding", "ISO10126Padding")
    override val blockSize: Int = 16
    override val keyLengths: List<Int> = listOf(16, 24, 32) // 128, 192, 256 bits
    
    override fun generateKey(password: String, keyLength: Int): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        val keyBytes = hash.copyOf(keyLength)
        return SecretKeySpec(keyBytes, name)
    }
}

/**
 * DES encryption algorithm implementation
 */
class DESAlgorithm : BaseSymmetricAlgorithm() {
    override val name: String = "DES"
    override val supportedModes: List<String> = listOf("ECB", "CBC", "CFB", "OFB")
    override val supportedPaddings: List<String> = listOf("PKCS5Padding", "NoPadding")
    override val blockSize: Int = 8
    override val keyLengths: List<Int> = listOf(8) // 64 bits (56 effective)
}

/**
 * 3DES/TripleDES encryption algorithm implementation
 */
class TripleDESAlgorithm : BaseSymmetricAlgorithm() {
    override val name: String = "DESede"
    override val supportedModes: List<String> = listOf("ECB", "CBC", "CFB", "OFB")
    override val supportedPaddings: List<String> = listOf("PKCS5Padding", "NoPadding")
    override val blockSize: Int = 8
    override val keyLengths: List<Int> = listOf(24) // 192 bits
}

/**
 * Blowfish encryption algorithm implementation
 */
class BlowfishAlgorithm : BaseSymmetricAlgorithm() {
    override val name: String = "Blowfish"
    override val supportedModes: List<String> = listOf("ECB", "CBC", "CFB", "OFB")
    override val supportedPaddings: List<String> = listOf("PKCS5Padding", "NoPadding")
    override val blockSize: Int = 8
    override val keyLengths: List<Int> = listOf(16, 24, 32, 40, 48, 56) // Variable key length
}

/**
 * RC4 stream cipher algorithm implementation
 */
class RC4Algorithm : BaseSymmetricAlgorithm() {
    override val name: String = "RC4"
    override val supportedModes: List<String> = listOf("NONE") // Stream cipher
    override val supportedPaddings: List<String> = listOf("NoPadding")
    override val blockSize: Int = 1 // Stream cipher
    override val keyLengths: List<Int> = listOf(5, 16, 32) // Variable key length (40-2048 bits)
    
    override fun requiresIV(mode: String): Boolean = false
}

/**
 * Twofish encryption algorithm implementation (via BouncyCastle)
 */
class TwofishAlgorithm : BaseSymmetricAlgorithm() {
    override val name: String = "Twofish"
    override val supportedModes: List<String> = listOf("ECB", "CBC", "CFB", "OFB")
    override val supportedPaddings: List<String> = listOf("PKCS7Padding", "NoPadding")
    override val blockSize: Int = 16
    override val keyLengths: List<Int> = listOf(16, 24, 32) // 128, 192, 256 bits
}

/**
 * CAST5 encryption algorithm implementation
 */
class CAST5Algorithm : BaseSymmetricAlgorithm() {
    override val name: String = "CAST5"
    override val supportedModes: List<String> = listOf("ECB", "CBC", "CFB", "OFB")
    override val supportedPaddings: List<String> = listOf("PKCS5Padding", "NoPadding")
    override val blockSize: Int = 8
    override val keyLengths: List<Int> = listOf(5, 16) // 40-128 bits
}

/**
 * CAST6 encryption algorithm implementation
 */
class CAST6Algorithm : BaseSymmetricAlgorithm() {
    override val name: String = "CAST6"
    override val supportedModes: List<String> = listOf("ECB", "CBC", "CFB", "OFB")
    override val supportedPaddings: List<String> = listOf("PKCS5Padding", "NoPadding")
    override val blockSize: Int = 16
    override val keyLengths: List<Int> = listOf(16, 20, 24, 28, 32) // 128-256 bits
}

/**
 * IDEA encryption algorithm implementation
 */
class IDEAAlgorithm : BaseSymmetricAlgorithm() {
    override val name: String = "IDEA"
    override val supportedModes: List<String> = listOf("ECB", "CBC", "CFB", "OFB")
    override val supportedPaddings: List<String> = listOf("PKCS5Padding", "NoPadding")
    override val blockSize: Int = 8
    override val keyLengths: List<Int> = listOf(16) // 128 bits
}

/**
 * Camellia encryption algorithm implementation
 */
class CamelliaAlgorithm : BaseSymmetricAlgorithm() {
    override val name: String = "Camellia"
    override val supportedModes: List<String> = listOf("ECB", "CBC", "CFB", "OFB", "GCM")
    override val supportedPaddings: List<String> = listOf("PKCS5Padding", "NoPadding")
    override val blockSize: Int = 16
    override val keyLengths: List<Int> = listOf(16, 24, 32) // 128, 192, 256 bits
}