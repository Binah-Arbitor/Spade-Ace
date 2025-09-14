#include "../include/file_analyzer.h"
#include "../include/crypto_engine.h"
#include <cstring>

extern "C" {
    // FFI interface for Flutter
    
    // File analysis
    struct CAnalysisResult {
        int detected_algorithm;
        int detected_mode;
        int detected_key_size;
        double confidence;
        char* file_type;
        char* analysis_details;
    };
    
    // Decryption result
    struct CDecryptionResult {
        bool success;
        uint8_t* data;
        size_t data_size;
        char* error_message;
        char* key_found;
        double time_taken;
        size_t attempts_made;
    };
    
    // Progress callback type
    typedef void (*ProgressCallbackC)(double progress, const char* status);
    
    // Global objects
    static FileAnalyzer* analyzer = nullptr;
    static CryptoEngine* engine = nullptr;
    
    // Initialize the native library
    void init_spade_ace() {
        if (!analyzer) analyzer = new FileAnalyzer();
        if (!engine) engine = new CryptoEngine();
    }
    
    // Cleanup
    void cleanup_spade_ace() {
        delete analyzer;
        delete engine;
        analyzer = nullptr;
        engine = nullptr;
    }
    
    // Analyze file
    CAnalysisResult* analyze_file(uint8_t* data, size_t size) {
        if (!analyzer) init_spade_ace();
        
        std::vector<uint8_t> file_data(data, data + size);
        AnalysisResult result = analyzer->analyze_file(file_data);
        
        CAnalysisResult* c_result = new CAnalysisResult();
        c_result->detected_algorithm = static_cast<int>(result.detected_algorithm);
        c_result->detected_mode = static_cast<int>(result.detected_mode);
        c_result->detected_key_size = result.detected_key_size;
        c_result->confidence = result.confidence;
        
        // Allocate and copy strings
        size_t type_len = result.file_type.length() + 1;
        c_result->file_type = new char[type_len];
        strcpy(c_result->file_type, result.file_type.c_str());
        
        size_t details_len = result.analysis_details.length() + 1;
        c_result->analysis_details = new char[details_len];
        strcpy(c_result->analysis_details, result.analysis_details.c_str());
        
        return c_result;
    }
    
    // Free analysis result
    void free_analysis_result(CAnalysisResult* result) {
        if (result) {
            delete[] result->file_type;
            delete[] result->analysis_details;
            delete result;
        }
    }
    
    // Start decryption
    CDecryptionResult* decrypt_file(
        uint8_t* data, 
        size_t size,
        int algorithm,
        int mode,
        int key_size,
        int attack_method,
        int performance_mode,
        ProgressCallbackC callback
    ) {
        if (!engine) init_spade_ace();
        
        std::vector<uint8_t> file_data(data, data + size);
        
        // Convert callback
        ProgressCallback cpp_callback = nullptr;
        if (callback) {
            cpp_callback = [callback](double progress, const std::string& status) {
                callback(progress, status.c_str());
            };
        }
        
        DecryptionResult result = engine->decrypt_file(
            file_data,
            static_cast<Algorithm>(algorithm),
            static_cast<Mode>(mode),
            key_size,
            static_cast<AttackMethod>(attack_method),
            static_cast<PerformanceMode>(performance_mode),
            cpp_callback
        );
        
        CDecryptionResult* c_result = new CDecryptionResult();
        c_result->success = result.success;
        c_result->time_taken = result.time_taken;
        c_result->attempts_made = result.attempts_made;
        
        // Copy data
        if (!result.data.empty()) {
            c_result->data_size = result.data.size();
            c_result->data = new uint8_t[result.data.size()];
            memcpy(c_result->data, result.data.data(), result.data.size());
        } else {
            c_result->data = nullptr;
            c_result->data_size = 0;
        }
        
        // Copy strings
        if (!result.error_message.empty()) {
            size_t len = result.error_message.length() + 1;
            c_result->error_message = new char[len];
            strcpy(c_result->error_message, result.error_message.c_str());
        } else {
            c_result->error_message = nullptr;
        }
        
        if (!result.key_found.empty()) {
            size_t len = result.key_found.length() + 1;
            c_result->key_found = new char[len];
            strcpy(c_result->key_found, result.key_found.c_str());
        } else {
            c_result->key_found = nullptr;
        }
        
        return c_result;
    }
    
    // Free decryption result
    void free_decryption_result(CDecryptionResult* result) {
        if (result) {
            delete[] result->data;
            delete[] result->error_message;
            delete[] result->key_found;
            delete result;
        }
    }
    
    // Stop current decryption
    void stop_decryption() {
        if (engine) {
            engine->stop_decryption();
        }
    }
    
    // Utility functions
    const char* algorithm_to_string(int algorithm) {
        switch (static_cast<Algorithm>(algorithm)) {
            case Algorithm::AES: return "AES";
            case Algorithm::DES: return "DES";
            case Algorithm::TRIPLE_DES: return "3DES";
            case Algorithm::BLOWFISH: return "Blowfish";
            case Algorithm::TWOFISH: return "Twofish";
            case Algorithm::RC4: return "RC4";
            case Algorithm::CHACHA20: return "ChaCha20";
            default: return "Unknown";
        }
    }
    
    const char* mode_to_string(int mode) {
        switch (static_cast<Mode>(mode)) {
            case Mode::ECB: return "ECB";
            case Mode::CBC: return "CBC";
            case Mode::CFB: return "CFB";
            case Mode::OFB: return "OFB";
            case Mode::CTR: return "CTR";
            case Mode::GCM: return "GCM";
            default: return "Unknown";
        }
    }
    
    // GPU-related functions
    bool initialize_gpu(const char* platform) {
        if (!engine) init_spade_ace();
        return engine->initialize_gpu(platform ? platform : "auto");
    }
    
    char** get_available_gpu_platforms(int* count) {
        if (!engine) init_spade_ace();
        
        auto platforms = engine->get_available_gpu_platforms();
        *count = static_cast<int>(platforms.size());
        
        if (platforms.empty()) {
            return nullptr;
        }
        
        char** result = new char*[platforms.size()];
        for (size_t i = 0; i < platforms.size(); ++i) {
            result[i] = new char[platforms[i].length() + 1];
            strcpy(result[i], platforms[i].c_str());
        }
        
        return result;
    }
    
    bool switch_gpu_platform(const char* platform) {
        if (!engine) init_spade_ace();
        return engine->switch_gpu_platform(platform ? platform : "auto");
    }
    
    char* get_gpu_info() {
        if (!engine) init_spade_ace();
        
        std::string info = engine->get_gpu_info();
        char* result = new char[info.length() + 1];
        strcpy(result, info.c_str());
        return result;
    }
    
    // GPU-accelerated decryption
    CDecryptionResult* decrypt_file_gpu(
        uint8_t* data, 
        size_t size,
        int algorithm,
        int mode,
        int key_size,
        int attack_method,
        ProgressCallbackC callback
    ) {
        if (!engine) init_spade_ace();
        
        std::vector<uint8_t> file_data(data, data + size);
        
        // Convert callback
        ProgressCallback cpp_callback = nullptr;
        if (callback) {
            cpp_callback = [callback](double progress, const std::string& status) {
                callback(progress, status.c_str());
            };
        }
        
        DecryptionResult result = engine->decrypt_file_gpu(
            file_data,
            static_cast<Algorithm>(algorithm),
            static_cast<Mode>(mode),
            key_size,
            static_cast<AttackMethod>(attack_method),
            cpp_callback
        );
        
        CDecryptionResult* c_result = new CDecryptionResult();
        c_result->success = result.success;
        c_result->time_taken = result.time_taken;
        c_result->attempts_made = result.attempts_made;
        
        if (result.success && !result.data.empty()) {
            c_result->data = new uint8_t[result.data.size()];
            c_result->data_size = result.data.size();
            memcpy(c_result->data, result.data.data(), result.data.size());
        } else {
            c_result->data = nullptr;
            c_result->data_size = 0;
        }
        
        if (!result.error_message.empty()) {
            c_result->error_message = new char[result.error_message.length() + 1];
            strcpy(c_result->error_message, result.error_message.c_str());
        } else {
            c_result->error_message = nullptr;
        }
        
        if (!result.key_found.empty()) {
            c_result->key_found = new char[result.key_found.length() + 1];
            strcpy(c_result->key_found, result.key_found.c_str());
        } else {
            c_result->key_found = nullptr;
        }
        
        return c_result;
    }
    
    // Utility functions for string array cleanup
    void free_string_array(char** array, int count) {
        if (array) {
            for (int i = 0; i < count; ++i) {
                delete[] array[i];
            }
            delete[] array;
        }
    }
    
    void free_string(char* str) {
        delete[] str;
    }
}