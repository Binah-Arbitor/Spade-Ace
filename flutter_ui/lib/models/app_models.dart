import 'dart:io';

/// Encryption algorithms supported by Crypto++
enum EncryptionAlgorithm {
  aes('AES', [128, 192, 256]),
  serpent('Serpent', [128, 192, 256]),
  twofish('Twofish', [128, 192, 256]),
  rc6('RC6', [128, 192, 256]),
  blowfish('Blowfish', [128, 256, 448]),
  cast('CAST', [128, 256]),
  camellia('Camellia', [128, 192, 256]),
  idea('IDEA', [128]);

  const EncryptionAlgorithm(this.displayName, this.supportedKeySizes);
  
  final String displayName;
  final List<int> supportedKeySizes;
}

/// Operation modes supported by Crypto++
enum OperationMode {
  cbc('CBC'),
  gcm('GCM'),
  ecb('ECB'),
  cfb('CFB'),
  ofb('OFB'),
  ctr('CTR');

  const OperationMode(this.displayName);
  
  final String displayName;
}

/// Log levels for console output
enum LogLevel {
  info('INFO'),
  warning('WARNING'),
  error('ERROR'),
  debug('DEBUG');

  const LogLevel(this.displayName);
  
  final String displayName;
}

/// Log entry model
class LogEntry {
  final DateTime timestamp;
  final LogLevel level;
  final String message;
  
  LogEntry({
    required this.timestamp,
    required this.level,
    required this.message,
  });
  
  @override
  String toString() {
    final timeStr = '${timestamp.hour.toString().padLeft(2, '0')}:'
                   '${timestamp.minute.toString().padLeft(2, '0')}:'
                   '${timestamp.second.toString().padLeft(2, '0')}';
    return '[$timeStr] [${level.displayName}] $message';
  }
}

/// Encryption configuration model
class EncryptionConfig {
  final EncryptionAlgorithm algorithm;
  final int keySize;
  final OperationMode mode;
  final String password;
  final int threadCount;
  final bool useHardwareAcceleration;
  
  EncryptionConfig({
    required this.algorithm,
    required this.keySize,
    required this.mode,
    required this.password,
    required this.threadCount,
    this.useHardwareAcceleration = false,
  });
  
  EncryptionConfig copyWith({
    EncryptionAlgorithm? algorithm,
    int? keySize,
    OperationMode? mode,
    String? password,
    int? threadCount,
    bool? useHardwareAcceleration,
  }) {
    return EncryptionConfig(
      algorithm: algorithm ?? this.algorithm,
      keySize: keySize ?? this.keySize,
      mode: mode ?? this.mode,
      password: password ?? this.password,
      threadCount: threadCount ?? this.threadCount,
      useHardwareAcceleration: useHardwareAcceleration ?? this.useHardwareAcceleration,
    );
  }
}

/// File processing status
enum ProcessingStatus {
  ready,
  processing,
  completed,
  error,
  cancelled,
}

/// Application state model
class AppState {
  final File? selectedFile;
  final EncryptionConfig config;
  final List<LogEntry> logEntries;
  final ProcessingStatus status;
  final double progress;
  final String statusMessage;
  final int maxThreads;
  
  AppState({
    this.selectedFile,
    required this.config,
    this.logEntries = const [],
    this.status = ProcessingStatus.ready,
    this.progress = 0.0,
    this.statusMessage = 'Ready',
    required this.maxThreads,
  });
  
  AppState copyWith({
    File? selectedFile,
    EncryptionConfig? config,
    List<LogEntry>? logEntries,
    ProcessingStatus? status,
    double? progress,
    String? statusMessage,
    int? maxThreads,
  }) {
    return AppState(
      selectedFile: selectedFile ?? this.selectedFile,
      config: config ?? this.config,
      logEntries: logEntries ?? this.logEntries,
      status: status ?? this.status,
      progress: progress ?? this.progress,
      statusMessage: statusMessage ?? this.statusMessage,
      maxThreads: maxThreads ?? this.maxThreads,
    );
  }
}

/// Helper class for algorithm-dependent options
class AlgorithmHelper {
  static List<OperationMode> getSupportedModes(EncryptionAlgorithm algorithm) {
    switch (algorithm) {
      case EncryptionAlgorithm.aes:
        return [OperationMode.cbc, OperationMode.gcm, OperationMode.ecb, 
                OperationMode.cfb, OperationMode.ofb, OperationMode.ctr];
      case EncryptionAlgorithm.serpent:
      case EncryptionAlgorithm.twofish:
      case EncryptionAlgorithm.rc6:
        return [OperationMode.cbc, OperationMode.ecb, OperationMode.cfb, OperationMode.ofb];
      case EncryptionAlgorithm.blowfish:
      case EncryptionAlgorithm.cast:
      case EncryptionAlgorithm.camellia:
      case EncryptionAlgorithm.idea:
        return [OperationMode.cbc, OperationMode.ecb];
    }
  }
  
  static int getDefaultKeySize(EncryptionAlgorithm algorithm) {
    return algorithm.supportedKeySizes.first;
  }
  
  static OperationMode getDefaultMode(EncryptionAlgorithm algorithm) {
    return getSupportedModes(algorithm).first;
  }
}