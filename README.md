# Spade Ace - File Decryption Tool

A powerful file-based decryption tool built with Flutter and C++ using the Crypto++ library. Spade Ace provides advanced cryptographic attack capabilities with multi-threading support and automatic encryption parameter detection.

## Features

### Core Functionality
- **File-based decryption**: Works with encrypted files, not just text
- **Auto-detection**: Automatically detects encryption algorithm, key size, and mode from file analysis
- **Manual override**: Dropdown menus to manually specify encryption parameters
- **Multiple attack methods**: Support for various cryptographic attack techniques

### Encryption Support
- **Algorithms**: AES, DES, 3DES, Blowfish, Twofish, RC4, ChaCha20
- **Modes**: ECB, CBC, CFB, OFB, CTR, GCM
- **Key sizes**: 128, 192, 256, 512, 1024, 2048 bits

### Attack Methods
- **Brute Force**: Systematic key space exploration
- **Dictionary Attack**: Password-based attacks using common keys
- **Rainbow Tables**: Pre-computed hash lookup
- **Known Plaintext**: Attacks when plaintext is partially known
- **Chosen Plaintext**: Attacks with controlled plaintext input
- **Side Channel**: Timing and power analysis attacks

### Performance Modes
- **Efficiency Mode**: Single-core processing for minimal system impact
- **Normal Mode**: 3-core processing for balanced performance
- **Performance Mode**: All-core processing for maximum speed

### File Format Detection
- OpenSSL encrypted files
- GPG/PGP encrypted files
- PKCS encrypted files
- ZIP archives with encryption
- PDF files with encryption
- Generic encrypted data

## Requirements

### Dependencies
- Flutter SDK (3.0.0+)
- CMake (3.16+)
- Crypto++ library
- C++17 compatible compiler

### Platform Support
- Linux (primary)
- Windows
- macOS
- Android (with NDK)

## Building

### Prerequisites
```bash
# Install Crypto++ on Ubuntu/Debian
sudo apt-get install libcrypto++-dev libcrypto++-doc libcrypto++-utils

# Install Flutter dependencies
flutter pub get
```

### Build Native Library
```bash
# Linux
cd native/cpp
mkdir build && cd build
cmake ..
make -j$(nproc)
```

### Build Flutter App
```bash
# Desktop
flutter build linux

# Android
flutter build apk

# Run in debug mode
flutter run
```

## Usage

1. **Select File**: Choose an encrypted file using the file picker
2. **Analyze**: Click "Analyze" to auto-detect encryption parameters
3. **Configure**: 
   - Review auto-detection results
   - Override parameters manually if needed
   - Choose attack method and performance mode
4. **Decrypt**: Start the decryption process
5. **Monitor**: Track progress and view results

## Architecture

### Flutter Frontend
- Material Design UI with responsive layout
- File picker integration
- Progress tracking and status updates
- Real-time parameter display

### C++ Backend
- Crypto++ integration for cryptographic operations
- Multi-threaded attack implementations
- File format analysis and detection
- FFI interface for Flutter communication

### Key Components
- **CryptoEngine**: Core decryption and attack logic
- **FileAnalyzer**: Encryption parameter detection
- **NativeBindings**: FFI interface between Flutter and C++
- **PerformanceManager**: Multi-threading optimization

## Security Notes

⚠️ **Educational Purpose**: This tool is designed for educational and authorized security testing purposes only.

- Only use on files you own or have explicit permission to decrypt
- Respect all applicable laws and regulations
- Use responsibly and ethically

## Contributing

Contributions are welcome! Please read our contributing guidelines and ensure all tests pass before submitting PRs.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

This software is provided for educational purposes only. The authors are not responsible for any misuse of this tool.