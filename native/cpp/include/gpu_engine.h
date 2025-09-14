#ifndef GPU_ENGINE_H
#define GPU_ENGINE_H

#include <vector>
#include <string>
#include <memory>
#include <functional>

// GPU Platform types
enum class GPUPlatform {
    NONE,
    CUDA,
    OPENCL,
    AUTO_DETECT
};

// GPU Device information
struct GPUDevice {
    std::string name;
    std::string vendor;
    size_t memory_size;
    int compute_units;
    GPUPlatform platform;
    int device_id;
    bool available;
};

// GPU-accelerated attack result
struct GPUAttackResult {
    bool success;
    std::vector<uint8_t> data;
    std::string key_found;
    double time_taken;
    size_t attempts_made;
    std::string error_message;
    size_t keys_per_second;
    std::string platform_used;
};

// Progress callback for GPU attacks
using GPUProgressCallback = std::function<void(double progress, const std::string& status, size_t keys_per_second)>;

class GPUEngine {
public:
    GPUEngine();
    ~GPUEngine();
    
    // GPU platform management
    bool initialize_gpu(GPUPlatform preferred_platform = GPUPlatform::AUTO_DETECT);
    std::vector<GPUDevice> detect_gpu_devices();
    bool switch_platform(GPUPlatform platform);
    GPUPlatform get_current_platform() const;
    std::string get_platform_info() const;
    void cleanup();
    
    // GPU-accelerated attacks
    GPUAttackResult gpu_brute_force_attack(
        const std::vector<uint8_t>& encrypted_data,
        const std::string& algorithm,
        const std::string& mode,
        int key_size,
        uint64_t start_key = 0,
        uint64_t end_key = 0,
        GPUProgressCallback progress_callback = nullptr
    );
    
    GPUAttackResult gpu_dictionary_attack(
        const std::vector<uint8_t>& encrypted_data,
        const std::string& algorithm,
        const std::string& mode,
        const std::vector<std::string>& dictionary,
        GPUProgressCallback progress_callback = nullptr
    );
    
    // Performance monitoring
    size_t get_optimal_work_group_size() const;
    size_t get_max_compute_units() const;
    size_t estimate_keys_per_second() const;
    double get_gpu_memory_usage() const;
    
    // Stop ongoing operations
    void stop_gpu_attack();
    
private:
    class GPUEngineImpl;
    std::unique_ptr<GPUEngineImpl> impl_;
    
    // Platform-specific initialization
    bool init_cuda();
    bool init_opencl();
    
    // Platform-specific cleanup
    void cleanup_cuda();
    void cleanup_opencl();
    
    // Platform-specific attack implementations (forward declarations)
    GPUAttackResult gpu_brute_force_cuda(
        const std::vector<uint8_t>& encrypted_data,
        const std::string& algorithm,
        const std::string& mode,
        int key_size,
        uint64_t start_key,
        uint64_t end_key,
        GPUProgressCallback progress_callback
    );
    
    GPUAttackResult gpu_brute_force_opencl(
        const std::vector<uint8_t>& encrypted_data,
        const std::string& algorithm,
        const std::string& mode,
        int key_size,
        uint64_t start_key,
        uint64_t end_key,
        GPUProgressCallback progress_callback
    );
    
    // Utility functions
    bool is_gpu_available() const;
    void log_gpu_info(const std::string& message) const;
};

#endif // GPU_ENGINE_H