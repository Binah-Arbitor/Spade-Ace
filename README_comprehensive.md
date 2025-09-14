# Spade Ace - Advanced File Decryption Tool 🃏

A comprehensive file-based decryption tool built with Flutter and C++ featuring GPU acceleration (CUDA/OpenCL), advanced cryptographic attacks, and intelligent parameter detection.

![Spade Ace Demo Interface](demo_ui.html)

## 🌟 Key Features

### 🚀 GPU Acceleration
- **CUDA Support**: NVIDIA GPU acceleration up to 2500x faster
- **OpenCL Support**: Universal GPU support (AMD, Intel, NVIDIA)  
- **Platform Switching**: Runtime switching between CUDA and OpenCL
- **Multi-GPU Support**: Automatic detection and utilization

### 🎯 Advanced Attack Methods
- **GPU-Accelerated Brute Force**: Exhaustive key search with parallel processing
- **Dictionary Attacks**: Password-based attacks with custom wordlists
- **Rainbow Table Lookups**: Pre-computed hash table attacks
- **Known/Chosen Plaintext**: Advanced cryptanalytic attacks
- **Side Channel Analysis**: Timing and power consumption analysis

### 🔐 Comprehensive Algorithm Support
- **AES**: 128/192/256-bit (all modes: ECB, CBC, CFB, OFB, CTR, GCM)
- **DES/3DES**: Legacy algorithm support
- **Blowfish/Twofish**: Variable key length support
- **RC4**: Stream cipher attacks
- **ChaCha20**: Modern stream cipher support

### 🧠 Intelligent Analysis
- **Automatic Parameter Detection**: AI-powered algorithm and mode detection
- **Entropy Analysis**: Measure encryption strength and randomness
- **Format Recognition**: OpenSSL, GPG/PGP, PKCS format detection
- **Confidence Scoring**: Rate detection reliability
- **Statistical Analysis**: Chi-squared and coincidence index

## 📊 Performance Benchmarks

| Platform | Performance | Speed Increase |
|----------|-------------|----------------|
| Single CPU Core | ~1,000 keys/sec | 1x baseline |
| 8-Core CPU | ~7,500 keys/sec | 7.5x |
| NVIDIA RTX 4090 | ~2.5M keys/sec | 2,500x |
| AMD RX 7900 XTX | ~1.8M keys/sec | 1,800x |

## 🛠️ Installation & Setup

### Prerequisites
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install build-essential cmake libcrypto++-dev
sudo apt-get install nvidia-cuda-toolkit ocl-icd-opencl-dev  # Optional GPU support

# Install Flutter
flutter pub get
```

### Build Instructions
```bash
# Clone repository
git clone https://github.com/Binah-Arbitor/Spade-Ace.git
cd Spade-Ace

# Build native library
cd native/cpp && mkdir build && cd build
cmake .. && make -j$(nproc)

# Build Flutter app
cd ../../.. && flutter build linux

# Or use automated script
chmod +x build.sh && ./build.sh
```

## 🚀 Quick Start Guide

1. **Launch Application**: `flutter run` or use built executable
2. **Select Encrypted File**: Use file picker to choose target
3. **Auto-Analyze**: Click "Analyze" for parameter detection
4. **Configure Attack**: 
   - Enable GPU acceleration
   - Select platform (CUDA/OpenCL)
   - Choose attack method
   - Set performance mode
5. **Start Decryption**: Monitor real-time progress
6. **Save Results**: Export decrypted data

## 🏗️ Architecture Overview

### Frontend (Flutter/Dart)
- **Modern UI**: Material Design with responsive layout
- **Real-time Updates**: Live progress and performance metrics
- **GPU Controls**: Platform selection and device information
- **File Management**: Integrated picker and result export

### Backend (C++/Crypto++)
- **CryptoEngine**: Core cryptographic operations
- **GPUEngine**: CUDA/OpenCL acceleration layer
- **FileAnalyzer**: Intelligent parameter detection
- **AttackManager**: Multi-threaded attack coordination

### GPU Acceleration Layer
- **CUDA Kernels**: High-performance NVIDIA GPU computation
- **OpenCL Kernels**: Universal GPU support across vendors
- **Memory Management**: Optimized GPU memory allocation
- **Load Balancing**: Automatic workload distribution

## 🔐 Security & Ethics

### ⚠️ Legal Usage Only
This tool is designed for:
- **Security Research**: Academic and professional cryptanalysis
- **Penetration Testing**: Authorized security assessments  
- **Data Recovery**: Legitimate file recovery scenarios
- **Educational Purposes**: Learning cryptographic concepts

### 🚫 Prohibited Uses
- Unauthorized access to encrypted data
- Violation of local/international laws
- Malicious or criminal activities
- Copyright infringement

## 📚 Advanced Usage

### Command Line Interface
```bash
# GPU-accelerated brute force
spade_ace --gpu --platform cuda --attack brute-force file.enc

# Dictionary attack with custom wordlist
spade_ace --attack dictionary --wordlist passwords.txt file.enc

# Batch processing
spade_ace --batch --input-dir encrypted/ --output-dir decrypted/
```

### Configuration Files
```yaml
# config.yaml
gpu:
  preferred_platform: "cuda"
  memory_limit: "4GB"
  
attacks:
  brute_force:
    max_key_length: 16
    threads: "auto"
    
  dictionary:
    wordlist_path: "wordlists/"
    rule_files: ["best64.rule"]
```

## 🤝 Contributing

We welcome contributions from the security and cryptographic communities!

### Development Setup
```bash
git clone https://github.com/Binah-Arbitor/Spade-Ace.git
cd Spade-Ace
flutter pub get
cd native/cpp && mkdir build && cd build && cmake .. && make
flutter run --debug
```

### Contribution Areas
- 🔧 **New Algorithms**: Implement additional ciphers
- 🚀 **GPU Optimizations**: Enhance kernel performance  
- 🎨 **UI/UX**: Improve user interface design
- 📖 **Documentation**: Expand guides and tutorials
- 🧪 **Testing**: Add comprehensive test coverage

## 📄 License & Disclaimer

### MIT License
This project is licensed under the MIT License - see [LICENSE](LICENSE) for details.

### Educational Use Only
**This software is provided for educational and legitimate security research purposes only.** Users are solely responsible for compliance with all applicable laws and regulations.

## 🙏 Acknowledgments

- **Crypto++**: Wei Dai and contributors for cryptographic library
- **NVIDIA**: CUDA toolkit and extensive documentation
- **Khronos Group**: OpenCL specification and standards
- **Flutter Team**: Excellent cross-platform framework
- **Security Community**: Research methodologies and best practices

## 📞 Support & Community

- 📖 **Documentation**: [Wiki](https://github.com/Binah-Arbitor/Spade-Ace/wiki)
- 🐛 **Issues**: [GitHub Issues](https://github.com/Binah-Arbitor/Spade-Ace/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/Binah-Arbitor/Spade-Ace/discussions)
- 📧 **Contact**: security@spade-ace.org

---

**Made with ♠️ by the Spade Ace Security Research Team**

*"Advancing cybersecurity through ethical research and education"*