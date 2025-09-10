#include "../include/crypto_engine.h"
#include <cryptopp/aes.h>
#include <cryptopp/des.h>
#include <cryptopp/blowfish.h>
#include <cryptopp/twofish.h>
#include <cryptopp/rc4.h>
#include <cryptopp/chacha.h>
#include <cryptopp/modes.h>
#include <cryptopp/filters.h>
#include <cryptopp/osrng.h>
#include <cryptopp/secblock.h>
#include <random>
#include <chrono>
#include <algorithm>
#include <future>

using namespace CryptoPP;

CryptoEngine::CryptoEngine() : should_stop_(false) {
}

CryptoEngine::~CryptoEngine() {
    stop_decryption();
    cleanup_threads();
}

DecryptionResult CryptoEngine::decrypt_file(
    const std::vector<uint8_t>& encrypted_data,
    Algorithm algorithm,
    Mode mode,
    int key_size,
    AttackMethod attack_method,
    PerformanceMode performance_mode,
    ProgressCallback progress_callback
) {
    should_stop_ = false;
    
    auto start_time = std::chrono::high_resolution_clock::now();
    
    DecryptionResult result;
    result.success = false;
    result.time_taken = 0.0;
    result.attempts_made = 0;
    
    try {
        // Route to appropriate algorithm
        switch (algorithm) {
            case Algorithm::AES:
                result = decrypt_aes(encrypted_data, mode, key_size, attack_method, performance_mode, progress_callback);
                break;
            case Algorithm::DES:
                result = decrypt_des(encrypted_data, mode, attack_method, performance_mode, progress_callback);
                break;
            case Algorithm::BLOWFISH:
                result = decrypt_blowfish(encrypted_data, mode, attack_method, performance_mode, progress_callback);
                break;
            default:
                result.error_message = "Unsupported algorithm";
                break;
        }
        
        auto end_time = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time);
        result.time_taken = duration.count() / 1000.0;
        
    } catch (const Exception& e) {
        result.error_message = "CryptoPP error: " + std::string(e.what());
    } catch (const std::exception& e) {
        result.error_message = "Standard error: " + std::string(e.what());
    }
    
    return result;
}

void CryptoEngine::stop_decryption() {
    should_stop_ = true;
}

DecryptionResult CryptoEngine::decrypt_aes(
    const std::vector<uint8_t>& data,
    Mode mode,
    int key_size,
    AttackMethod attack,
    PerformanceMode perf,
    ProgressCallback cb
) {
    switch (attack) {
        case AttackMethod::BRUTE_FORCE:
            return brute_force_attack(data, Algorithm::AES, mode, key_size, perf, cb);
        case AttackMethod::DICTIONARY:
            return dictionary_attack(data, Algorithm::AES, mode, key_size, perf, cb);
        default:
            return brute_force_attack(data, Algorithm::AES, mode, key_size, perf, cb);
    }
}

DecryptionResult CryptoEngine::decrypt_des(
    const std::vector<uint8_t>& data,
    Mode mode,
    AttackMethod attack,
    PerformanceMode perf,
    ProgressCallback cb
) {
    return brute_force_attack(data, Algorithm::DES, mode, 64, perf, cb);
}

DecryptionResult CryptoEngine::decrypt_blowfish(
    const std::vector<uint8_t>& data,
    Mode mode,
    AttackMethod attack,
    PerformanceMode perf,
    ProgressCallback cb
) {
    return brute_force_attack(data, Algorithm::BLOWFISH, mode, 448, perf, cb);
}

DecryptionResult CryptoEngine::brute_force_attack(
    const std::vector<uint8_t>& data,
    Algorithm algo,
    Mode mode,
    int key_size,
    PerformanceMode perf,
    ProgressCallback cb
) {
    DecryptionResult result;
    result.success = false;
    result.attempts_made = 0;
    
    int thread_count = get_thread_count(perf);
    if (cb) cb(0.0, "Starting brute force attack with " + std::to_string(thread_count) + " threads");
    
    // Calculate key space based on key size
    uint64_t total_keys = 1ULL << std::min(key_size, 32); // Limit for demo purposes
    uint64_t keys_per_thread = total_keys / thread_count;
    
    std::atomic<bool> found(false);
    std::mutex result_mutex;
    
    // Launch worker threads
    std::vector<std::future<void>> futures;
    
    for (int i = 0; i < thread_count && !should_stop_; ++i) {
        uint64_t start_key = i * keys_per_thread;
        uint64_t end_key = (i == thread_count - 1) ? total_keys : (i + 1) * keys_per_thread;
        
        auto future = std::async(std::launch::async, [=, &found, &result, &result_mutex, &cb]() {
            SecByteBlock key(key_size / 8);
            SecByteBlock iv(16); // Assume 16-byte IV for most modes
            std::vector<uint8_t> plaintext;
            
            for (uint64_t k = start_key; k < end_key && !should_stop_ && !found; ++k) {
                // Generate key from counter
                memset(key, 0, key.size());
                for (size_t j = 0; j < std::min(sizeof(k), key.size()); ++j) {
                    key[j] = (k >> (j * 8)) & 0xFF;
                }
                
                try {
                    plaintext.clear();
                    plaintext.resize(data.size());
                    
                    // Try decryption based on algorithm and mode
                    if (algo == Algorithm::AES) {
                        if (mode == Mode::CBC) {
                            CBC_Mode<AES>::Decryption decryption;
                            decryption.SetKeyWithIV(key, key.size(), iv);
                            
                            ArraySource(data.data(), data.size(), true,
                                new StreamTransformationFilter(decryption,
                                    new ArraySink(plaintext.data(), plaintext.size())
                                )
                            );
                        }
                    }
                    
                    // Check if result looks like valid plaintext
                    if (is_valid_plaintext(plaintext)) {
                        std::lock_guard<std::mutex> lock(result_mutex);
                        if (!found.exchange(true)) {
                            result.success = true;
                            result.data = plaintext;
                            result.key_found = "Found at attempt " + std::to_string(k);
                            result.attempts_made = k + 1;
                        }
                        return;
                    }
                    
                } catch (...) {
                    // Ignore decryption errors and continue
                }
                
                // Update progress periodically
                if (k % 1000 == 0 && cb) {
                    double progress = static_cast<double>(k - start_key) / (end_key - start_key);
                    cb(progress * (1.0 / thread_count) + (i * 1.0 / thread_count),
                       "Thread " + std::to_string(i) + " testing key " + std::to_string(k));
                }
            }
        });
        
        futures.push_back(std::move(future));
    }
    
    // Wait for all threads to complete
    for (auto& future : futures) {
        future.wait();
    }
    
    if (!result.success && !should_stop_) {
        result.error_message = "Brute force attack failed to find valid key";
    }
    
    return result;
}

DecryptionResult CryptoEngine::dictionary_attack(
    const std::vector<uint8_t>& data,
    Algorithm algo,
    Mode mode,
    int key_size,
    PerformanceMode perf,
    ProgressCallback cb
) {
    DecryptionResult result;
    result.success = false;
    
    // Common passwords/keys for dictionary attack
    std::vector<std::string> dictionary = {
        "password", "123456", "password123", "admin", "test",
        "secret", "key", "default", "user", "qwerty",
        "letmein", "welcome", "monkey", "dragon", "master"
    };
    
    if (cb) cb(0.0, "Starting dictionary attack");
    
    for (size_t i = 0; i < dictionary.size() && !should_stop_; ++i) {
        const std::string& password = dictionary[i];
        
        try {
            // Derive key from password (simplified)
            SecByteBlock key(key_size / 8);
            SecByteBlock iv(16);
            
            // Simple key derivation (in practice, use PBKDF2/scrypt)
            StringSource(password, true, new HashFilter(SHA256(), new ArraySink(key, key.size())));
            
            std::vector<uint8_t> plaintext(data.size());
            
            // Try decryption
            if (algo == Algorithm::AES && mode == Mode::CBC) {
                CBC_Mode<AES>::Decryption decryption;
                decryption.SetKeyWithIV(key, key.size(), iv);
                
                ArraySource(data.data(), data.size(), true,
                    new StreamTransformationFilter(decryption,
                        new ArraySink(plaintext.data(), plaintext.size())
                    )
                );
            }
            
            if (is_valid_plaintext(plaintext)) {
                result.success = true;
                result.data = plaintext;
                result.key_found = password;
                result.attempts_made = i + 1;
                break;
            }
            
        } catch (...) {
            // Continue with next password
        }
        
        if (cb) {
            double progress = static_cast<double>(i + 1) / dictionary.size();
            cb(progress, "Trying password: " + password);
        }
    }
    
    if (!result.success && !should_stop_) {
        result.error_message = "Dictionary attack failed";
    }
    
    return result;
}

int CryptoEngine::get_thread_count(PerformanceMode mode) const {
    switch (mode) {
        case PerformanceMode::EFFICIENCY:
            return 1;
        case PerformanceMode::NORMAL:
            return 3;
        case PerformanceMode::PERFORMANCE:
            return std::thread::hardware_concurrency();
        default:
            return 1;
    }
}

bool CryptoEngine::is_valid_plaintext(const std::vector<uint8_t>& data) const {
    if (data.empty()) return false;
    
    // Simple heuristics for valid plaintext
    int printable_count = 0;
    int null_count = 0;
    
    for (uint8_t byte : data) {
        if (byte == 0) null_count++;
        else if (byte >= 32 && byte <= 126) printable_count++;
    }
    
    // Consider it valid if more than 70% are printable characters
    double printable_ratio = static_cast<double>(printable_count) / data.size();
    return printable_ratio > 0.7 && null_count < data.size() * 0.1;
}

void CryptoEngine::cleanup_threads() {
    for (auto& thread : worker_threads_) {
        if (thread && thread->joinable()) {
            thread->join();
        }
    }
    worker_threads_.clear();
}