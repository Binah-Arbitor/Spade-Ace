#ifndef FILE_ANALYZER_H
#define FILE_ANALYZER_H

#include "crypto_engine.h"
#include <vector>
#include <string>

// File analysis result
struct AnalysisResult {
    Algorithm detected_algorithm;
    Mode detected_mode;
    int detected_key_size;
    double confidence;
    std::string file_type;
    std::vector<std::string> possible_algorithms;
    std::string analysis_details;
};

class FileAnalyzer {
public:
    FileAnalyzer();
    ~FileAnalyzer();
    
    // Analyze encrypted file and detect parameters
    AnalysisResult analyze_file(const std::vector<uint8_t>& file_data);
    
private:
    // Analysis methods
    Algorithm detect_algorithm_from_header(const std::vector<uint8_t>& data);
    Mode detect_mode_from_patterns(const std::vector<uint8_t>& data, Algorithm algo);
    int detect_key_size(const std::vector<uint8_t>& data, Algorithm algo);
    
    // Pattern analysis
    double analyze_entropy(const std::vector<uint8_t>& data);
    bool has_block_alignment(const std::vector<uint8_t>& data, int block_size);
    std::vector<int> find_repeating_patterns(const std::vector<uint8_t>& data);
    
    // File format detection
    std::string detect_file_type(const std::vector<uint8_t>& data);
    bool is_openssl_format(const std::vector<uint8_t>& data);
    bool is_gpg_format(const std::vector<uint8_t>& data);
    bool is_pkcs_format(const std::vector<uint8_t>& data);
    
    // Statistical analysis
    std::vector<double> calculate_byte_frequencies(const std::vector<uint8_t>& data);
    double calculate_chi_squared(const std::vector<uint8_t>& data);
    double calculate_index_of_coincidence(const std::vector<uint8_t>& data);
};

#endif // FILE_ANALYZER_H