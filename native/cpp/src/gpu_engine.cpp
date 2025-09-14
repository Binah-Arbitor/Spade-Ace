#include "../include/gpu_engine.h"
#include <chrono>
#include <iostream>
#include <thread>
#include <atomic>

// CUDA headers (conditional compilation)
#ifdef CUDA_AVAILABLE
#include <cuda_runtime.h>
#include <cuda.h>
#endif

// OpenCL headers (conditional compilation)
#ifdef OPENCL_AVAILABLE
#ifdef __APPLE__
#include <OpenCL/opencl.h>
#else
#include <CL/cl.h>
#endif
#endif

class GPUEngine::GPUEngineImpl {
public:
    GPUPlatform current_platform = GPUPlatform::NONE;
    std::vector<GPUDevice> available_devices;
    std::atomic<bool> should_stop{false};
    
    // CUDA-specific members
#ifdef CUDA_AVAILABLE
    int cuda_device_id = -1;
    cudaDeviceProp cuda_props;
#endif
    
    // OpenCL-specific members
#ifdef OPENCL_AVAILABLE
    cl_context opencl_context = nullptr;
    cl_command_queue opencl_queue = nullptr;
    cl_device_id opencl_device = nullptr;
    cl_program opencl_program = nullptr;
#endif
};

GPUEngine::GPUEngine() : impl_(std::make_unique<GPUEngineImpl>()) {
}

GPUEngine::~GPUEngine() {
    cleanup();
}

bool GPUEngine::initialize_gpu(GPUPlatform preferred_platform) {
    cleanup(); // Clean up any previous initialization
    
    if (preferred_platform == GPUPlatform::AUTO_DETECT) {
        // Try CUDA first, then OpenCL
        if (init_cuda()) {
            impl_->current_platform = GPUPlatform::CUDA;
            log_gpu_info("CUDA platform initialized successfully");
            return true;
        } else if (init_opencl()) {
            impl_->current_platform = GPUPlatform::OPENCL;
            log_gpu_info("OpenCL platform initialized successfully");
            return true;
        }
        return false;
    } else if (preferred_platform == GPUPlatform::CUDA) {
        if (init_cuda()) {
            impl_->current_platform = GPUPlatform::CUDA;
            log_gpu_info("CUDA platform initialized successfully");
            return true;
        }
    } else if (preferred_platform == GPUPlatform::OPENCL) {
        if (init_opencl()) {
            impl_->current_platform = GPUPlatform::OPENCL;
            log_gpu_info("OpenCL platform initialized successfully");
            return true;
        }
    }
    
    impl_->current_platform = GPUPlatform::NONE;
    return false;
}

std::vector<GPUDevice> GPUEngine::detect_gpu_devices() {
    std::vector<GPUDevice> devices;
    
#ifdef CUDA_AVAILABLE
    // Detect CUDA devices
    int cuda_device_count = 0;
    cudaGetDeviceCount(&cuda_device_count);
    
    for (int i = 0; i < cuda_device_count; ++i) {
        cudaDeviceProp props;
        if (cudaGetDeviceProperties(&props, i) == cudaSuccess) {
            GPUDevice device;
            device.name = props.name;
            device.vendor = "NVIDIA";
            device.memory_size = props.totalGlobalMem;
            device.compute_units = props.multiProcessorCount;
            device.platform = GPUPlatform::CUDA;
            device.device_id = i;
            device.available = true;
            devices.push_back(device);
        }
    }
#endif

#ifdef OPENCL_AVAILABLE
    // Detect OpenCL devices
    cl_uint platform_count = 0;
    clGetPlatformIDs(0, nullptr, &platform_count);
    
    if (platform_count > 0) {
        std::vector<cl_platform_id> platforms(platform_count);
        clGetPlatformIDs(platform_count, platforms.data(), nullptr);
        
        for (const auto& platform : platforms) {
            cl_uint device_count = 0;
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 0, nullptr, &device_count);
            
            if (device_count > 0) {
                std::vector<cl_device_id> opencl_devices(device_count);
                clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, device_count, opencl_devices.data(), nullptr);
                
                for (size_t i = 0; i < opencl_devices.size(); ++i) {
                    GPUDevice device;
                    
                    char name[256];
                    clGetDeviceInfo(opencl_devices[i], CL_DEVICE_NAME, sizeof(name), name, nullptr);
                    device.name = name;
                    
                    char vendor[256];
                    clGetDeviceInfo(opencl_devices[i], CL_DEVICE_VENDOR, sizeof(vendor), vendor, nullptr);
                    device.vendor = vendor;
                    
                    cl_ulong memory;
                    clGetDeviceInfo(opencl_devices[i], CL_DEVICE_GLOBAL_MEM_SIZE, sizeof(memory), &memory, nullptr);
                    device.memory_size = memory;
                    
                    cl_uint compute_units;
                    clGetDeviceInfo(opencl_devices[i], CL_DEVICE_MAX_COMPUTE_UNITS, sizeof(compute_units), &compute_units, nullptr);
                    device.compute_units = compute_units;
                    
                    device.platform = GPUPlatform::OPENCL;
                    device.device_id = static_cast<int>(i);
                    device.available = true;
                    devices.push_back(device);
                }
            }
        }
    }
#endif

    impl_->available_devices = devices;
    return devices;
}

bool GPUEngine::switch_platform(GPUPlatform platform) {
    if (platform == impl_->current_platform) {
        return true; // Already using this platform
    }
    
    return initialize_gpu(platform);
}

GPUPlatform GPUEngine::get_current_platform() const {
    return impl_->current_platform;
}

std::string GPUEngine::get_platform_info() const {
    switch (impl_->current_platform) {
        case GPUPlatform::CUDA:
#ifdef CUDA_AVAILABLE
            {
                int version;
                cudaRuntimeGetVersion(&version);
                return "CUDA Runtime Version: " + std::to_string(version / 1000) + "." + std::to_string((version % 100) / 10);
            }
#else
            return "CUDA (Not Available)";
#endif
        case GPUPlatform::OPENCL:
            return "OpenCL Platform";
        case GPUPlatform::NONE:
            return "No GPU Platform";
        default:
            return "Unknown Platform";
    }
}

GPUAttackResult GPUEngine::gpu_brute_force_attack(
    const std::vector<uint8_t>& encrypted_data,
    const std::string& algorithm,
    const std::string& mode,
    int key_size,
    uint64_t start_key,
    uint64_t end_key,
    GPUProgressCallback progress_callback
) {
    GPUAttackResult result;
    result.success = false;
    result.attempts_made = 0;
    result.keys_per_second = 0;
    result.platform_used = get_platform_info();
    
    if (impl_->current_platform == GPUPlatform::NONE) {
        result.error_message = "No GPU platform available";
        return result;
    }
    
    impl_->should_stop = false;
    auto start_time = std::chrono::high_resolution_clock::now();
    
    // Calculate key range if not specified
    if (end_key == 0) {
        end_key = 1ULL << std::min(key_size, 40); // Limit for safety
    }
    
    try {
        // GPU-accelerated brute force implementation
        if (impl_->current_platform == GPUPlatform::CUDA) {
            result = gpu_brute_force_cuda(encrypted_data, algorithm, mode, key_size, start_key, end_key, progress_callback);
        } else if (impl_->current_platform == GPUPlatform::OPENCL) {
            result = gpu_brute_force_opencl(encrypted_data, algorithm, mode, key_size, start_key, end_key, progress_callback);
        }
        
        auto end_time = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time);
        result.time_taken = duration.count() / 1000.0;
        
        if (result.time_taken > 0) {
            result.keys_per_second = static_cast<size_t>(result.attempts_made / result.time_taken);
        }
        
    } catch (const std::exception& e) {
        result.error_message = "GPU attack error: " + std::string(e.what());
    }
    
    return result;
}

GPUAttackResult GPUEngine::gpu_dictionary_attack(
    const std::vector<uint8_t>& encrypted_data,
    const std::string& algorithm,
    const std::string& mode,
    const std::vector<std::string>& dictionary,
    GPUProgressCallback progress_callback
) {
    GPUAttackResult result;
    result.success = false;
    result.attempts_made = 0;
    result.keys_per_second = 0;
    result.platform_used = get_platform_info();
    
    if (impl_->current_platform == GPUPlatform::NONE) {
        result.error_message = "No GPU platform available";
        return result;
    }
    
    impl_->should_stop = false;
    auto start_time = std::chrono::high_resolution_clock::now();
    
    // Simplified dictionary attack - in practice would use GPU kernels
    for (size_t i = 0; i < dictionary.size() && !impl_->should_stop; ++i) {
        if (progress_callback && i % 1000 == 0) {
            double progress = static_cast<double>(i) / dictionary.size();
            size_t kps = i / std::max(1.0, (std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::high_resolution_clock::now() - start_time).count() / 1000.0));
            progress_callback(progress, "Trying password: " + dictionary[i], kps);
        }
        
        result.attempts_made = i + 1;
        
        // Simulate GPU dictionary attack
        // In real implementation, this would be GPU kernel code
        std::this_thread::sleep_for(std::chrono::microseconds(1)); // Simulate work
    }
    
    auto end_time = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time);
    result.time_taken = duration.count() / 1000.0;
    
    if (result.time_taken > 0) {
        result.keys_per_second = static_cast<size_t>(result.attempts_made / result.time_taken);
    }
    
    if (!result.success) {
        result.error_message = "GPU dictionary attack failed to find valid key";
    }
    
    return result;
}

size_t GPUEngine::get_optimal_work_group_size() const {
    switch (impl_->current_platform) {
        case GPUPlatform::CUDA:
#ifdef CUDA_AVAILABLE
            return impl_->cuda_props.maxThreadsPerBlock;
#else
            return 256;
#endif
        case GPUPlatform::OPENCL:
            return 256; // Common work group size
        default:
            return 1;
    }
}

size_t GPUEngine::get_max_compute_units() const {
    switch (impl_->current_platform) {
        case GPUPlatform::CUDA:
#ifdef CUDA_AVAILABLE
            return impl_->cuda_props.multiProcessorCount;
#else
            return 0;
#endif
        case GPUPlatform::OPENCL:
            if (!impl_->available_devices.empty()) {
                return impl_->available_devices[0].compute_units;
            }
            return 0;
        default:
            return 0;
    }
}

size_t GPUEngine::estimate_keys_per_second() const {
    size_t compute_units = get_max_compute_units();
    size_t work_group_size = get_optimal_work_group_size();
    
    // Rough estimate based on GPU specifications
    // This would need benchmarking for accuracy
    return compute_units * work_group_size * 1000; // Conservative estimate
}

double GPUEngine::get_gpu_memory_usage() const {
    // Implementation would query actual GPU memory usage
    return 0.0; // Placeholder
}

void GPUEngine::stop_gpu_attack() {
    impl_->should_stop = true;
}

bool GPUEngine::init_cuda() {
#ifdef CUDA_AVAILABLE
    int device_count = 0;
    if (cudaGetDeviceCount(&device_count) != cudaSuccess || device_count == 0) {
        return false;
    }
    
    // Use the first available device
    impl_->cuda_device_id = 0;
    if (cudaSetDevice(impl_->cuda_device_id) != cudaSuccess) {
        return false;
    }
    
    if (cudaGetDeviceProperties(&impl_->cuda_props, impl_->cuda_device_id) != cudaSuccess) {
        return false;
    }
    
    return true;
#else
    return false;
#endif
}

bool GPUEngine::init_opencl() {
#ifdef OPENCL_AVAILABLE
    cl_uint platform_count = 0;
    if (clGetPlatformIDs(0, nullptr, &platform_count) != CL_SUCCESS || platform_count == 0) {
        return false;
    }
    
    std::vector<cl_platform_id> platforms(platform_count);
    clGetPlatformIDs(platform_count, platforms.data(), nullptr);
    
    // Find a GPU device
    for (const auto& platform : platforms) {
        cl_uint device_count = 0;
        if (clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 0, nullptr, &device_count) == CL_SUCCESS && device_count > 0) {
            std::vector<cl_device_id> devices(device_count);
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, device_count, devices.data(), nullptr);
            
            impl_->opencl_device = devices[0]; // Use first GPU
            
            // Create context
            impl_->opencl_context = clCreateContext(nullptr, 1, &impl_->opencl_device, nullptr, nullptr, nullptr);
            if (impl_->opencl_context == nullptr) {
                continue;
            }
            
            // Create command queue
            impl_->opencl_queue = clCreateCommandQueue(impl_->opencl_context, impl_->opencl_device, 0, nullptr);
            if (impl_->opencl_queue == nullptr) {
                clReleaseContext(impl_->opencl_context);
                impl_->opencl_context = nullptr;
                continue;
            }
            
            return true;
        }
    }
    
    return false;
#else
    return false;
#endif
}

void GPUEngine::cleanup() {
    impl_->should_stop = true;
    cleanup_cuda();
    cleanup_opencl();
    impl_->current_platform = GPUPlatform::NONE;
}

void GPUEngine::cleanup_cuda() {
#ifdef CUDA_AVAILABLE
    if (impl_->cuda_device_id >= 0) {
        cudaDeviceReset();
        impl_->cuda_device_id = -1;
    }
#endif
}

void GPUEngine::cleanup_opencl() {
#ifdef OPENCL_AVAILABLE
    if (impl_->opencl_program) {
        clReleaseProgram(impl_->opencl_program);
        impl_->opencl_program = nullptr;
    }
    if (impl_->opencl_queue) {
        clReleaseCommandQueue(impl_->opencl_queue);
        impl_->opencl_queue = nullptr;
    }
    if (impl_->opencl_context) {
        clReleaseContext(impl_->opencl_context);
        impl_->opencl_context = nullptr;
    }
    impl_->opencl_device = nullptr;
#endif
}

bool GPUEngine::is_gpu_available() const {
    return impl_->current_platform != GPUPlatform::NONE;
}

void GPUEngine::log_gpu_info(const std::string& message) const {
    std::cout << "[GPU Engine] " << message << std::endl;
}

// Platform-specific attack implementations (stubs)
GPUAttackResult GPUEngine::gpu_brute_force_cuda(
    const std::vector<uint8_t>& encrypted_data,
    const std::string& algorithm,
    const std::string& mode,
    int key_size,
    uint64_t start_key,
    uint64_t end_key,
    GPUProgressCallback progress_callback
) {
    GPUAttackResult result;
    result.success = false;
    result.error_message = "CUDA brute force not yet implemented";
    // Implementation would contain CUDA kernel code
    return result;
}

GPUAttackResult GPUEngine::gpu_brute_force_opencl(
    const std::vector<uint8_t>& encrypted_data,
    const std::string& algorithm,
    const std::string& mode,
    int key_size,
    uint64_t start_key,
    uint64_t end_key,
    GPUProgressCallback progress_callback
) {
    GPUAttackResult result;
    result.success = false;
    result.error_message = "OpenCL brute force not yet implemented";
    // Implementation would contain OpenCL kernel code
    return result;
}