package com.binah.spadeace.core.crypto

/**
 * Registry for managing encryption algorithms
 */
class AlgorithmRegistry {
    private val algorithms = mutableMapOf<String, EncryptionAlgorithm>()
    
    init {
        // Register all built-in algorithms
        registerBuiltInAlgorithms()
    }
    
    private fun registerBuiltInAlgorithms() {
        // Symmetric algorithms
        register(AESAlgorithm())
        register(DESAlgorithm())
        register(TripleDESAlgorithm())
        register(BlowfishAlgorithm())
        register(RC4Algorithm())
        register(TwofishAlgorithm())
        register(CAST5Algorithm())
        register(CAST6Algorithm())
        register(IDEAAlgorithm())
        register(CamelliaAlgorithm())
        
        // Asymmetric algorithms
        register(RSAAlgorithm())
    }
    
    /**
     * Register a new encryption algorithm
     */
    fun register(algorithm: EncryptionAlgorithm) {
        algorithms[algorithm.name.uppercase()] = algorithm
    }
    
    /**
     * Unregister an encryption algorithm
     */
    fun unregister(algorithmName: String) {
        algorithms.remove(algorithmName.uppercase())
    }
    
    /**
     * Get an algorithm by name
     */
    fun getAlgorithm(name: String): EncryptionAlgorithm? {
        return algorithms[name.uppercase()]
    }
    
    /**
     * Get all registered algorithm names
     */
    fun getAllAlgorithmNames(): List<String> {
        return algorithms.keys.toList().sorted()
    }
    
    /**
     * Get all registered algorithms
     */
    fun getAllAlgorithms(): List<EncryptionAlgorithm> {
        return algorithms.values.toList()
    }
    
    /**
     * Check if an algorithm is registered
     */
    fun isRegistered(name: String): Boolean {
        return algorithms.containsKey(name.uppercase())
    }
    
    /**
     * Get algorithms that support a specific block size
     */
    fun getAlgorithmsByBlockSize(blockSize: Int): List<EncryptionAlgorithm> {
        return algorithms.values.filter { it.blockSize == blockSize }
    }
    
    /**
     * Get algorithms that support specific modes and paddings
     */
    fun getCompatibleAlgorithms(mode: String, padding: String): List<EncryptionAlgorithm> {
        return algorithms.values.filter { algorithm ->
            mode.uppercase() in algorithm.supportedModes.map { it.uppercase() } &&
            padding in algorithm.supportedPaddings &&
            algorithm.isValidCombination(mode, padding)
        }
    }
    
    /**
     * Get algorithm suggestions based on file analysis
     */
    fun suggestAlgorithms(
        fileSize: Long,
        blockAlignment: Int,
        possibleModes: List<String> = emptyList(),
        possiblePaddings: List<String> = emptyList()
    ): List<EncryptionAlgorithm> {
        val suggestions = mutableListOf<EncryptionAlgorithm>()
        
        // First filter by block size alignment
        val blockAlignedAlgorithms = if (blockAlignment > 0) {
            getAlgorithmsByBlockSize(blockAlignment)
        } else {
            getAllAlgorithms()
        }
        
        // Then filter by modes and paddings if provided
        for (algorithm in blockAlignedAlgorithms) {
            val isModeSuggested = possibleModes.isEmpty() || 
                possibleModes.any { mode -> 
                    algorithm.supportedModes.any { supportedMode -> 
                        supportedMode.equals(mode, ignoreCase = true) 
                    }
                }
            
            val isPaddingSuggested = possiblePaddings.isEmpty() ||
                possiblePaddings.any { padding -> 
                    algorithm.supportedPaddings.contains(padding)
                }
            
            if (isModeSuggested && isPaddingSuggested) {
                suggestions.add(algorithm)
            }
        }
        
        // Sort by popularity/strength (AES first, then others)
        return suggestions.sortedWith { a, b ->
            when {
                a.name == "AES" && b.name != "AES" -> -1
                a.name != "AES" && b.name == "AES" -> 1
                a.name == "Blowfish" && b.name !in listOf("AES", "Blowfish") -> -1
                a.name !in listOf("AES", "Blowfish") && b.name == "Blowfish" -> 1
                else -> a.name.compareTo(b.name)
            }
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: AlgorithmRegistry? = null
        
        fun getInstance(): AlgorithmRegistry {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AlgorithmRegistry().also { INSTANCE = it }
            }
        }
    }
}