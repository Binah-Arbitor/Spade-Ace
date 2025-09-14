#ifndef CRYPTO_ENGINE_H
#define CRYPTO_ENGINE_H

#include <string>
#include <vector>
#include <memory>
#include <atomic>
#include <thread>
#include <functional>
#include <future>

// Encryption algorithm types
enum class Algorithm {
    AES,
    DES,
    TRIPLE_DES,
    BLOWFISH,
    TWOFISH,
    RC4,
    CHACHA20,
    UNKNOWN
};

// Encryption modes
enum class Mode {
    ECB,
    CBC, 
    CFB,
    OFB,
    CTR,
    GCM,
    UNKNOWN
};

// Performance modes
enum class PerformanceMode {
    EFFICIENCY = 1,    // Single core
    NORMAL = 3,        // 3 cores
    PERFORMANCE = 0    // All cores (0 = auto-detect)
};

// Attack methods
enum class AttackMethod {
    BRUTE_FORCE,
    DICTIONARY,
    RAINBOW_TABLE,
    KNOWN_PLAINTEXT,
    CHOSEN_PLAINTEXT,
    SIDE_CHANNEL
};

// Decryption result
struct DecryptionResult {
    bool success;
    std::vector<uint8_t> data;
    std::string error_message;
    std::string key_found;
    double time_taken;
    size_t attempts_made;
};

// Progress callback function type
using ProgressCallback = std::function<void(double progress, const std::string& status)>;

class CryptoEngine {
public:
    CryptoEngine();
    ~CryptoEngine();
    
    // Main decryption function
    DecryptionResult decrypt_file(
        const std::vector<uint8_t>& encrypted_data,
        Algorithm algorithm,
        Mode mode,
        int key_size,
        AttackMethod attack_method,
        PerformanceMode performance_mode,
        ProgressCallback progress_callback = nullptr
    );
    
    // Stop ongoing decryption
    void stop_decryption();
    
private:
    std::atomic<bool> should_stop_;
    std::vector<std::unique_ptr<std::thread>> worker_threads_;
    
    // Algorithm-specific decryption methods
    DecryptionResult decrypt_aes(const std::vector<uint8_t>& data, Mode mode, int key_size, 
                                AttackMethod attack, PerformanceMode perf, ProgressCallback cb);
    DecryptionResult decrypt_des(const std::vector<uint8_t>& data, Mode mode, 
                                AttackMethod attack, PerformanceMode perf, ProgressCallback cb);
    DecryptionResult decrypt_blowfish(const std::vector<uint8_t>& data, Mode mode,
                                     AttackMethod attack, PerformanceMode perf, ProgressCallback cb);
    
    // Attack method implementations
    DecryptionResult brute_force_attack(const std::vector<uint8_t>& data, Algorithm algo,
                                       Mode mode, int key_size, PerformanceMode perf, ProgressCallback cb);
    DecryptionResult dictionary_attack(const std::vector<uint8_t>& data, Algorithm algo,
                                      Mode mode, int key_size, PerformanceMode perf, ProgressCallback cb);
    
    // Utility functions
    int get_thread_count(PerformanceMode mode) const;
    bool is_valid_plaintext(const std::vector<uint8_t>& data) const;
    void cleanup_threads();
};

#endif // CRYPTO_ENGINE_H