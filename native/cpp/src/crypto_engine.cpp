#include "../include/crypto_engine.h"
#include "../include/gpu_engine.h"
#include <cryptopp/aes.h>
#include <cryptopp/des.h>
#include <cryptopp/blowfish.h>
#include <cryptopp/twofish.h>
#include <cryptopp/arc4.h>
#include <cryptopp/chacha.h>
#include <cryptopp/modes.h>
#include <cryptopp/filters.h>
#include <cryptopp/osrng.h>
#include <cryptopp/secblock.h>
#include <cryptopp/sha.h>
#include <random>
#include <chrono>
#include <algorithm>
#include <future>
#include <set>

using namespace CryptoPP;

CryptoEngine::CryptoEngine() : should_stop_(false), gpu_engine_(std::make_unique<GPUEngine>()) {
}

CryptoEngine::~CryptoEngine() {
    stop_decryption();
    cleanup_threads();
}

DecryptionResult CryptoEngine::decrypt_file(
    const std::vector<uint8_t>& encrypted_data,
    ::Algorithm algorithm,
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
            case ::Algorithm::AES:
                result = decrypt_aes(encrypted_data, mode, key_size, attack_method, performance_mode, progress_callback);
                break;
            case ::Algorithm::DES:
                result = decrypt_des(encrypted_data, mode, attack_method, performance_mode, progress_callback);
                break;
            case ::Algorithm::TRIPLE_DES:
                result = decrypt_des(encrypted_data, mode, attack_method, performance_mode, progress_callback);
                break;
            case ::Algorithm::BLOWFISH:
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
            return brute_force_attack(data, ::Algorithm::AES, mode, key_size, perf, cb);
        case AttackMethod::DICTIONARY:
            return dictionary_attack(data, ::Algorithm::AES, mode, key_size, perf, cb);
        default:
            return brute_force_attack(data, ::Algorithm::AES, mode, key_size, perf, cb);
    }
}

DecryptionResult CryptoEngine::decrypt_des(
    const std::vector<uint8_t>& data,
    Mode mode,
    AttackMethod attack,
    PerformanceMode perf,
    ProgressCallback cb
) {
    return brute_force_attack(data, ::Algorithm::DES, mode, 64, perf, cb);
}

DecryptionResult CryptoEngine::decrypt_blowfish(
    const std::vector<uint8_t>& data,
    Mode mode,
    AttackMethod attack,
    PerformanceMode perf,
    ProgressCallback cb
) {
    return brute_force_attack(data, ::Algorithm::BLOWFISH, mode, 448, perf, cb);
}

DecryptionResult CryptoEngine::brute_force_attack(
    const std::vector<uint8_t>& data,
    ::Algorithm algo,
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
                    if (algo == ::Algorithm::AES) {
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
    ::Algorithm algo,
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
            SHA256 hash;
            StringSource(password, true, new HashFilter(hash, new ArraySink(key, key.size())));
            
            std::vector<uint8_t> plaintext(data.size());
            
            // Try decryption
            if (algo == ::Algorithm::AES && mode == Mode::CBC) {
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

// GPU-related implementations
DecryptionResult CryptoEngine::decrypt_file_gpu(
    const std::vector<uint8_t>& encrypted_data,
    ::Algorithm algorithm,
    Mode mode,
    int key_size,
    AttackMethod attack_method,
    ProgressCallback progress_callback
) {
    DecryptionResult result;
    result.success = false;
    result.time_taken = 0.0;
    result.attempts_made = 0;
    
    if (!gpu_engine_) {
        result.error_message = "GPU engine not available";
        return result;
    }
    
    auto start_time = std::chrono::high_resolution_clock::now();
    
    try {
        // Convert algorithm enum to string for GPU engine
        std::string algo_str;
        switch (algorithm) {
            case ::Algorithm::AES: algo_str = "AES"; break;
            case ::Algorithm::DES: algo_str = "DES"; break;
            case ::Algorithm::TRIPLE_DES: algo_str = "3DES"; break;
            case ::Algorithm::BLOWFISH: algo_str = "Blowfish"; break;
            default: algo_str = "AES"; break;
        }
        
        // Convert mode enum to string
        std::string mode_str;
        switch (mode) {
            case Mode::ECB: mode_str = "ECB"; break;
            case Mode::CBC: mode_str = "CBC"; break;
            case Mode::CFB: mode_str = "CFB"; break;
            case Mode::OFB: mode_str = "OFB"; break;
            case Mode::CTR: mode_str = "CTR"; break;
            case Mode::GCM: mode_str = "GCM"; break;
            default: mode_str = "CBC"; break;
        }
        
        // GPU progress callback adapter
        auto gpu_progress_callback = [progress_callback](double progress, const std::string& status, size_t kps) {
            if (progress_callback) {
                progress_callback(progress, status + " (GPU: " + std::to_string(kps) + " keys/sec)");
            }
        };
        
        // Perform GPU-accelerated attack
        GPUAttackResult gpu_result;
        if (attack_method == AttackMethod::BRUTE_FORCE) {
            gpu_result = gpu_engine_->gpu_brute_force_attack(
                encrypted_data, algo_str, mode_str, key_size, 0, 0, gpu_progress_callback
            );
        } else {
            // For other attack methods, fall back to CPU implementation
            return decrypt_file(encrypted_data, algorithm, mode, key_size, attack_method, PerformanceMode::PERFORMANCE, progress_callback);
        }
        
        // Convert GPU result to regular result
        result.success = gpu_result.success;
        result.data = gpu_result.data;
        result.error_message = gpu_result.error_message;
        result.key_found = gpu_result.key_found;
        result.time_taken = gpu_result.time_taken;
        result.attempts_made = gpu_result.attempts_made;
        
    } catch (const std::exception& e) {
        result.error_message = "GPU decryption error: " + std::string(e.what());
    }
    
    return result;
}

bool CryptoEngine::initialize_gpu(const std::string& platform) {
    if (!gpu_engine_) {
        return false;
    }
    
    GPUPlatform gpu_platform = GPUPlatform::AUTO_DETECT;
    if (platform == "cuda") {
        gpu_platform = GPUPlatform::CUDA;
    } else if (platform == "opencl") {
        gpu_platform = GPUPlatform::OPENCL;
    }
    
    return gpu_engine_->initialize_gpu(gpu_platform);
}

std::vector<std::string> CryptoEngine::get_available_gpu_platforms() {
    std::vector<std::string> platforms;
    
    if (!gpu_engine_) {
        return platforms;
    }
    
    auto devices = gpu_engine_->detect_gpu_devices();
    std::set<std::string> unique_platforms;
    
    for (const auto& device : devices) {
        if (device.platform == GPUPlatform::CUDA) {
            unique_platforms.insert("CUDA");
        } else if (device.platform == GPUPlatform::OPENCL) {
            unique_platforms.insert("OpenCL");
        }
    }
    
    for (const auto& platform : unique_platforms) {
        platforms.push_back(platform);
    }
    
    return platforms;
}

bool CryptoEngine::switch_gpu_platform(const std::string& platform) {
    if (!gpu_engine_) {
        return false;
    }
    
    GPUPlatform gpu_platform = GPUPlatform::AUTO_DETECT;
    if (platform == "CUDA") {
        gpu_platform = GPUPlatform::CUDA;
    } else if (platform == "OpenCL") {
        gpu_platform = GPUPlatform::OPENCL;
    }
    
    return gpu_engine_->switch_platform(gpu_platform);
}

std::string CryptoEngine::get_gpu_info() const {
    if (!gpu_engine_) {
        return "GPU engine not available";
    }
    
    auto devices = gpu_engine_->detect_gpu_devices();
    if (devices.empty()) {
        return "No GPU devices detected";
    }
    
    std::string info = "GPU Devices:\n";
    for (const auto& device : devices) {
        info += "- " + device.name + " (" + device.vendor + ")\n";
        info += "  Memory: " + std::to_string(device.memory_size / (1024*1024)) + " MB\n";
        info += "  Compute Units: " + std::to_string(device.compute_units) + "\n";
        info += "  Platform: " + std::string(device.platform == GPUPlatform::CUDA ? "CUDA" : "OpenCL") + "\n";
    }
    
    info += "\nCurrent Platform: " + gpu_engine_->get_platform_info();
    return info;
}