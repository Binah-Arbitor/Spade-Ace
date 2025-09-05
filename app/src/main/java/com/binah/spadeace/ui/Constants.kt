package com.binah.spadeace.ui

/**
 * Constants for UI validation and safety limits
 */
object Constants {
    
    // File operation limits
    const val MAX_FILE_SIZE_BYTES = 100 * 1024 * 1024L // 100MB
    const val MAX_DIRECTORY_FILES = 1000
    
    // Text processing limits
    const val MAX_TEXT_INPUT_LENGTH = 10000
    
    // Thread configuration limits
    const val MIN_THREAD_COUNT = 1
    const val MAX_THREAD_COUNT = 32
    
    // Memory and performance limits
    const val MIN_CHUNK_SIZE = 1024 // 1KB
    const val MAX_CHUNK_SIZE = 10 * 1024 * 1024 // 10MB
    const val HIGH_MEMORY_USAGE_THRESHOLD = 80 // percentage
    
    // Password attack limits
    const val MIN_PASSWORD_LENGTH = 1
    const val MAX_PASSWORD_LENGTH = 20
    
    // Default values
    const val DEFAULT_THREAD_COUNT = 4
    const val DEFAULT_CHUNK_SIZE = 1024 * 1024 // 1MB
    
    // Error messages
    const val ERROR_FILE_TOO_LARGE = "File too large (max 100MB)"
    const val ERROR_TEXT_TOO_LONG = "Text too long (max 10,000 characters)"
    const val ERROR_INVALID_THREAD_COUNT = "Thread count must be between 1 and 32"
    const val ERROR_PERMISSION_DENIED = "Permission denied"
    const val ERROR_INVALID_FILE_PATH = "Invalid file path"
    const val ERROR_DIRECTORY_NOT_ACCESSIBLE = "Directory does not exist or is not accessible"
}