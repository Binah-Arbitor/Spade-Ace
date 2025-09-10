import 'dart:typed_data';
import 'dart:math';

// Mock implementation of native bindings for testing
// This allows the Flutter UI to work without the C++ library

// Enum mappings (same as before)
enum NativeAlgorithm {
  aes(0),
  des(1),
  tripleDes(2),
  blowfish(3),
  twofish(4),
  rc4(5),
  chacha20(6),
  unknown(7);
  
  const NativeAlgorithm(this.value);
  final int value;
}

enum NativeMode {
  ecb(0),
  cbc(1),
  cfb(2),
  ofb(3),
  ctr(4),
  gcm(5),
  unknown(6);
  
  const NativeMode(this.value);
  final int value;
}

enum NativeAttackMethod {
  bruteForce(0),
  dictionary(1),
  rainbowTable(2),
  knownPlaintext(3),
  chosenPlaintext(4),
  sideChannel(5);
  
  const NativeAttackMethod(this.value);
  final int value;
}

enum NativePerformanceMode {
  efficiency(1),
  normal(3),
  performance(0);
  
  const NativePerformanceMode(this.value);
  final int value;
}

// Result classes (same as before)
class FileAnalysisResult {
  final NativeAlgorithm detectedAlgorithm;
  final NativeMode detectedMode;
  final int detectedKeySize;
  final double confidence;
  final String fileType;
  final String analysisDetails;
  
  const FileAnalysisResult({
    required this.detectedAlgorithm,
    required this.detectedMode,
    required this.detectedKeySize,
    required this.confidence,
    required this.fileType,
    required this.analysisDetails,
  });
}

class FileDecryptionResult {
  final bool success;
  final Uint8List? data;
  final String? errorMessage;
  final String? keyFound;
  final double timeTaken;
  final int attemptsMade;
  
  const FileDecryptionResult({
    required this.success,
    this.data,
    this.errorMessage,
    this.keyFound,
    required this.timeTaken,
    required this.attemptsMade,
  });
}

// Mock native bindings class
class NativeBindings {
  static final Random _random = Random();
  
  static void init() {
    // Mock initialization
    print('Mock: Native library initialized');
  }
  
  static void cleanup() {
    // Mock cleanup
    print('Mock: Native library cleaned up');
  }
  
  static FileAnalysisResult analyzeFile(Uint8List data) {
    // Mock file analysis
    final entropy = _calculateEntropy(data);
    
    NativeAlgorithm algorithm = NativeAlgorithm.aes;
    NativeMode mode = NativeMode.cbc;
    int keySize = 256;
    double confidence = 0.75;
    String fileType = 'Generic encrypted data';
    
    // Basic heuristic detection
    if (data.length >= 8) {
      final header = String.fromCharCodes(data.take(8));
      if (header == 'Salted__') {
        algorithm = NativeAlgorithm.aes;
        mode = NativeMode.cbc;
        confidence = 0.9;
        fileType = 'OpenSSL encrypted file';
      }
    }
    
    // Check for block alignment
    if (data.length % 16 == 0) {
      algorithm = NativeAlgorithm.aes;
      confidence += 0.1;
    } else if (data.length % 8 == 0) {
      algorithm = NativeAlgorithm.des;
      confidence += 0.1;
    }
    
    final analysisDetails = '''
Entropy: ${entropy.toStringAsFixed(2)}
File size: ${data.length} bytes
Block alignment: ${data.length % 16 == 0 ? '16-byte (AES)' : data.length % 8 == 0 ? '8-byte (DES)' : 'None'}
High entropy detected: ${entropy > 7.5 ? 'Yes' : 'No'}
''';
    
    return FileAnalysisResult(
      detectedAlgorithm: algorithm,
      detectedMode: mode,
      detectedKeySize: keySize,
      confidence: confidence.clamp(0.0, 1.0),
      fileType: fileType,
      analysisDetails: analysisDetails,
    );
  }
  
  static Future<FileDecryptionResult> decryptFile({
    required Uint8List data,
    required NativeAlgorithm algorithm,
    required NativeMode mode,
    required int keySize,
    required NativeAttackMethod attackMethod,
    required NativePerformanceMode performanceMode,
    Function(double progress, String status)? onProgress,
  }) async {
    // Mock decryption process
    final int totalSteps = 100;
    final int delayMs = attackMethod == NativeAttackMethod.dictionary ? 50 : 100;
    
    for (int i = 0; i <= totalSteps; i++) {
      await Future.delayed(Duration(milliseconds: delayMs));
      
      final progress = i / totalSteps;
      final status = _getProgressStatus(i, algorithm, attackMethod, performanceMode);
      
      onProgress?.call(progress, status);
      
      // Simulate early success for dictionary attacks
      if (attackMethod == NativeAttackMethod.dictionary && i >= 15 && _random.nextDouble() < 0.3) {
        return FileDecryptionResult(
          success: true,
          data: _generateMockDecryptedData(data.length),
          keyFound: 'password123',
          timeTaken: (i * delayMs) / 1000.0,
          attemptsMade: i * 100,
        );
      }
      
      // Simulate success for brute force after more attempts
      if (attackMethod == NativeAttackMethod.bruteForce && i >= 80 && _random.nextDouble() < 0.2) {
        return FileDecryptionResult(
          success: true,
          data: _generateMockDecryptedData(data.length),
          keyFound: 'Key found at attempt ${i * 1000}',
          timeTaken: (i * delayMs) / 1000.0,
          attemptsMade: i * 1000,
        );
      }
    }
    
    // Simulate failure
    return FileDecryptionResult(
      success: false,
      errorMessage: 'Failed to decrypt file with ${algorithmToString(algorithm)} ${modeToString(mode)}',
      timeTaken: (totalSteps * delayMs) / 1000.0,
      attemptsMade: totalSteps * 1000,
    );
  }
  
  static void stopDecryption() {
    // Mock stop
    print('Mock: Decryption stopped');
  }
  
  static String algorithmToString(NativeAlgorithm algorithm) {
    switch (algorithm) {
      case NativeAlgorithm.aes: return 'AES';
      case NativeAlgorithm.des: return 'DES';
      case NativeAlgorithm.tripleDes: return '3DES';
      case NativeAlgorithm.blowfish: return 'Blowfish';
      case NativeAlgorithm.twofish: return 'Twofish';
      case NativeAlgorithm.rc4: return 'RC4';
      case NativeAlgorithm.chacha20: return 'ChaCha20';
      case NativeAlgorithm.unknown: return 'Unknown';
    }
  }
  
  static String modeToString(NativeMode mode) {
    switch (mode) {
      case NativeMode.ecb: return 'ECB';
      case NativeMode.cbc: return 'CBC';
      case NativeMode.cfb: return 'CFB';
      case NativeMode.ofb: return 'OFB';
      case NativeMode.ctr: return 'CTR';
      case NativeMode.gcm: return 'GCM';
      case NativeMode.unknown: return 'Unknown';
    }
  }
  
  // Helper methods
  static double _calculateEntropy(Uint8List data) {
    if (data.isEmpty) return 0.0;
    
    final List<int> counts = List.filled(256, 0);
    for (int byte in data) {
      counts[byte]++;
    }
    
    double entropy = 0.0;
    final int length = data.length;
    
    for (int count in counts) {
      if (count > 0) {
        final double probability = count / length;
        entropy -= probability * (probability.logE / log2e);
      }
    }
    
    return entropy;
  }
  
  static String _getProgressStatus(int step, NativeAlgorithm algorithm, 
                                  NativeAttackMethod attack, NativePerformanceMode perf) {
    final algoStr = algorithmToString(algorithm);
    final attackStr = attack.name;
    final coreCount = perf == NativePerformanceMode.efficiency ? 1 : 
                     perf == NativePerformanceMode.normal ? 3 : 8;
    
    if (step == 0) {
      return 'Initializing $attackStr attack on $algoStr (${coreCount} cores)';
    } else if (step < 20) {
      return 'Analyzing encryption parameters...';
    } else if (step < 50) {
      return 'Testing keys... ($step% complete)';
    } else if (step < 80) {
      return 'Deep analysis in progress... ($step% complete)';
    } else {
      return 'Final attempts... ($step% complete)';
    }
  }
  
  static Uint8List _generateMockDecryptedData(int originalLength) {
    // Generate mock plaintext that looks realistic
    final random = Random();
    final data = Uint8List(originalLength);
    
    // Fill with printable ASCII characters mostly
    for (int i = 0; i < data.length; i++) {
      if (random.nextDouble() < 0.8) {
        data[i] = 32 + random.nextInt(95); // Printable ASCII
      } else {
        data[i] = random.nextInt(256); // Some binary data
      }
    }
    
    return data;
  }
}

const double log2e = 1.4426950408889634; // log2(e)