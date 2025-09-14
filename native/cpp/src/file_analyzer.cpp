#include "../include/file_analyzer.h"
#include <algorithm>
#include <cmath>
#include <map>
#include <set>
#include <numeric>
#include <array>

FileAnalyzer::FileAnalyzer() {
}

FileAnalyzer::~FileAnalyzer() {
}

AnalysisResult FileAnalyzer::analyze_file(const std::vector<uint8_t>& file_data) {
    AnalysisResult result;
    result.detected_algorithm = Algorithm::UNKNOWN;
    result.detected_mode = Mode::UNKNOWN;
    result.detected_key_size = 0;
    result.confidence = 0.0;
    
    if (file_data.empty()) {
        result.analysis_details = "Empty file";
        return result;
    }
    
    // Detect file type first
    result.file_type = detect_file_type(file_data);
    
    // Analyze entropy
    double entropy = analyze_entropy(file_data);
    result.analysis_details += "Entropy: " + std::to_string(entropy) + "\n";
    
    // High entropy suggests encryption
    if (entropy > 7.5) {
        result.confidence += 0.3;
        result.analysis_details += "High entropy detected (likely encrypted)\n";
    }
    
    // Try to detect algorithm from file header/format
    result.detected_algorithm = detect_algorithm_from_header(file_data);
    
    if (result.detected_algorithm != Algorithm::UNKNOWN) {
        result.confidence += 0.4;
        
        // Detect mode based on patterns
        result.detected_mode = detect_mode_from_patterns(file_data, result.detected_algorithm);
        if (result.detected_mode != Mode::UNKNOWN) {
            result.confidence += 0.2;
        }
        
        // Detect key size
        result.detected_key_size = detect_key_size(file_data, result.detected_algorithm);
        if (result.detected_key_size > 0) {
            result.confidence += 0.1;
        }
    } else {
        // Try heuristic detection
        if (has_block_alignment(file_data, 16)) {
            result.possible_algorithms.push_back("AES");
            result.analysis_details += "16-byte block alignment detected (possibly AES)\n";
            result.confidence += 0.2;
        }
        if (has_block_alignment(file_data, 8)) {
            result.possible_algorithms.push_back("DES/3DES");
            result.analysis_details += "8-byte block alignment detected (possibly DES/3DES)\n";
            result.confidence += 0.1;
        }
        
        // Default guess based on common usage
        if (result.possible_algorithms.empty() && entropy > 7.0) {
            result.detected_algorithm = Algorithm::AES;
            result.detected_mode = Mode::CBC;
            result.detected_key_size = 256;
            result.possible_algorithms.push_back("AES (default guess)");
            result.confidence = 0.3;
        }
    }
    
    // Statistical analysis
    double chi_squared = calculate_chi_squared(file_data);
    double ioc = calculate_index_of_coincidence(file_data);
    
    result.analysis_details += "Chi-squared: " + std::to_string(chi_squared) + "\n";
    result.analysis_details += "Index of Coincidence: " + std::to_string(ioc) + "\n";
    
    // Adjust confidence based on statistical tests
    if (chi_squared > 200 && chi_squared < 400) {
        result.confidence += 0.1; // Good randomness indicator
    }
    if (ioc < 0.05) {
        result.confidence += 0.1; // Low correlation suggests good encryption
    }
    
    result.confidence = std::min(result.confidence, 1.0);
    
    return result;
}

Algorithm FileAnalyzer::detect_algorithm_from_header(const std::vector<uint8_t>& data) {
    if (data.size() < 16) return Algorithm::UNKNOWN;
    
    // Check for OpenSSL format
    if (is_openssl_format(data)) {
        // OpenSSL typically uses AES by default in newer versions
        return Algorithm::AES;
    }
    
    // Check for GPG format
    if (is_gpg_format(data)) {
        // GPG can use various algorithms, but AES is common
        return Algorithm::AES;
    }
    
    // Check for PKCS format
    if (is_pkcs_format(data)) {
        return Algorithm::AES;
    }
    
    // Check specific signatures
    std::vector<uint8_t> header(data.begin(), data.begin() + 16);
    
    // Salted__ prefix (common in OpenSSL)
    if (header.size() >= 8) {
        std::string header_str(header.begin(), header.begin() + 8);
        if (header_str == "Salted__") {
            return Algorithm::AES;
        }
    }
    
    // Check for specific magic bytes
    if (header[0] == 0x01 && header[1] == 0x02 && header[2] == 0x03) {
        return Algorithm::DES;
    }
    
    // PGP/GPG packet format
    if ((header[0] & 0x80) && (header[0] & 0x40)) {
        return Algorithm::AES; // Modern PGP typically uses AES
    }
    
    return Algorithm::UNKNOWN;
}

Mode FileAnalyzer::detect_mode_from_patterns(const std::vector<uint8_t>& data, Algorithm algo) {
    if (data.size() < 32) return Mode::UNKNOWN;
    
    int block_size = 16; // Default for AES
    if (algo == Algorithm::DES || algo == Algorithm::TRIPLE_DES) {
        block_size = 8;
    }
    
    // Check for ECB mode (repeating blocks)
    std::map<std::vector<uint8_t>, int> block_counts;
    for (size_t i = 0; i + block_size <= data.size(); i += block_size) {
        std::vector<uint8_t> block(data.begin() + i, data.begin() + i + block_size);
        block_counts[block]++;
    }
    
    // If we have repeating blocks, it might be ECB
    int max_count = 0;
    for (const auto& pair : block_counts) {
        max_count = std::max(max_count, pair.second);
    }
    
    if (max_count > 1 && block_counts.size() < data.size() / block_size * 0.9) {
        return Mode::ECB;
    }
    
    // Check for IV at the beginning (suggests CBC, CFB, OFB, CTR)
    if (data.size() > block_size * 2) {
        // If the first block looks random and unique, it might be an IV
        std::vector<uint8_t> first_block(data.begin(), data.begin() + block_size);
        double first_block_entropy = analyze_entropy(first_block);
        
        if (first_block_entropy > 7.0) {
            // High entropy first block suggests IV
            return Mode::CBC; // Most common mode with IV
        }
    }
    
    // Default to CBC for block ciphers
    if (algo == Algorithm::AES || algo == Algorithm::DES || algo == Algorithm::TRIPLE_DES) {
        return Mode::CBC;
    }
    
    return Mode::UNKNOWN;
}

int FileAnalyzer::detect_key_size(const std::vector<uint8_t>& data, Algorithm algo) {
    switch (algo) {
        case Algorithm::AES:
            // AES can be 128, 192, or 256 bits
            // Default to 256 for security
            return 256;
        case Algorithm::DES:
            return 64;
        case Algorithm::TRIPLE_DES:
            return 192; // 3 * 64 bits
        case Algorithm::BLOWFISH:
            return 448; // Variable, but 448 is max
        case Algorithm::TWOFISH:
            return 256;
        case Algorithm::RC4:
            return 128; // Variable, but common
        case Algorithm::CHACHA20:
            return 256;
        default:
            return 0;
    }
}

double FileAnalyzer::analyze_entropy(const std::vector<uint8_t>& data) {
    if (data.empty()) return 0.0;
    
    std::array<int, 256> byte_counts = {0};
    
    for (uint8_t byte : data) {
        byte_counts[byte]++;
    }
    
    double entropy = 0.0;
    double total = static_cast<double>(data.size());
    
    for (int count : byte_counts) {
        if (count > 0) {
            double probability = count / total;
            entropy -= probability * log2(probability);
        }
    }
    
    return entropy;
}

bool FileAnalyzer::has_block_alignment(const std::vector<uint8_t>& data, int block_size) {
    return data.size() % block_size == 0;
}

std::vector<int> FileAnalyzer::find_repeating_patterns(const std::vector<uint8_t>& data) {
    std::vector<int> pattern_lengths;
    
    // Look for patterns of length 2-16 bytes
    for (int len = 2; len <= 16 && len < static_cast<int>(data.size() / 4); ++len) {
        std::map<std::vector<uint8_t>, std::vector<size_t>> patterns;
        
        for (size_t i = 0; i <= data.size() - len; ++i) {
            std::vector<uint8_t> pattern(data.begin() + i, data.begin() + i + len);
            patterns[pattern].push_back(i);
        }
        
        for (const auto& pair : patterns) {
            if (pair.second.size() >= 3) { // Found at least 3 times
                pattern_lengths.push_back(len);
                break;
            }
        }
    }
    
    return pattern_lengths;
}

std::string FileAnalyzer::detect_file_type(const std::vector<uint8_t>& data) {
    if (data.size() < 16) return "Unknown";
    
    // Check common file signatures
    if (data.size() >= 4) {
        // ZIP/JAR files
        if (data[0] == 0x50 && data[1] == 0x4B && data[2] == 0x03 && data[3] == 0x04) {
            return "ZIP archive (possibly encrypted)";
        }
        
        // PDF
        if (data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F') {
            return "PDF (possibly encrypted)";
        }
        
        // RAR
        if (data[0] == 0x52 && data[1] == 0x61 && data[2] == 0x72 && data[3] == 0x21) {
            return "RAR archive";
        }
    }
    
    if (is_openssl_format(data)) {
        return "OpenSSL encrypted file";
    }
    
    if (is_gpg_format(data)) {
        return "GPG/PGP encrypted file";
    }
    
    // Check if it looks like a generic encrypted file
    double entropy = analyze_entropy(data);
    if (entropy > 7.5) {
        return "Generic encrypted data";
    }
    
    return "Unknown";
}

bool FileAnalyzer::is_openssl_format(const std::vector<uint8_t>& data) {
    if (data.size() < 8) return false;
    
    // Check for "Salted__" prefix
    return (data[0] == 'S' && data[1] == 'a' && data[2] == 'l' && data[3] == 't' &&
            data[4] == 'e' && data[5] == 'd' && data[6] == '_' && data[7] == '_');
}

bool FileAnalyzer::is_gpg_format(const std::vector<uint8_t>& data) {
    if (data.size() < 2) return false;
    
    // GPG/PGP binary format starts with packet header
    // First byte has format: 10TTTTLL where T=packet type, L=length type
    return (data[0] & 0x80) != 0 && (data[0] & 0x40) != 0;
}

bool FileAnalyzer::is_pkcs_format(const std::vector<uint8_t>& data) {
    if (data.size() < 2) return false;
    
    // PKCS#7/CMS format starts with ASN.1 DER encoding
    // Should start with SEQUENCE tag (0x30)
    return data[0] == 0x30;
}

std::vector<double> FileAnalyzer::calculate_byte_frequencies(const std::vector<uint8_t>& data) {
    std::vector<double> frequencies(256, 0.0);
    
    if (data.empty()) return frequencies;
    
    for (uint8_t byte : data) {
        frequencies[byte] += 1.0;
    }
    
    double total = static_cast<double>(data.size());
    for (double& freq : frequencies) {
        freq /= total;
    }
    
    return frequencies;
}

double FileAnalyzer::calculate_chi_squared(const std::vector<uint8_t>& data) {
    if (data.empty()) return 0.0;
    
    auto frequencies = calculate_byte_frequencies(data);
    double expected = 1.0 / 256.0; // Expected frequency for uniform distribution
    double chi_squared = 0.0;
    
    for (double observed : frequencies) {
        double diff = observed - expected;
        chi_squared += (diff * diff) / expected;
    }
    
    return chi_squared * data.size();
}

double FileAnalyzer::calculate_index_of_coincidence(const std::vector<uint8_t>& data) {
    if (data.size() < 2) return 0.0;
    
    std::array<int, 256> counts = {0};
    for (uint8_t byte : data) {
        counts[byte]++;
    }
    
    double ioc = 0.0;
    double n = static_cast<double>(data.size());
    
    for (int count : counts) {
        if (count > 1) {
            ioc += count * (count - 1);
        }
    }
    
    return ioc / (n * (n - 1));
}