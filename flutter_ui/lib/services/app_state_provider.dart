import 'dart:async';
import 'dart:io';
import 'dart:isolate';
import 'package:flutter/foundation.dart';
import 'package:device_info_plus/device_info_plus.dart';
import '../models/app_models.dart';

class AppStateProvider with ChangeNotifier {
  AppState _state;
  
  // Stream for real-time log updates
  final StreamController<LogEntry> _logStreamController = StreamController<LogEntry>.broadcast();
  Stream<LogEntry> get logStream => _logStreamController.stream;
  
  // Timer for simulating backend communication
  Timer? _processingTimer;
  Timer? _logTimer;
  
  AppStateProvider() : _state = AppState(
    config: EncryptionConfig(
      algorithm: EncryptionAlgorithm.aes,
      keySize: 256,
      mode: OperationMode.cbc,
      password: '',
      threadCount: 1,
    ),
    maxThreads: 1,
  ) {
    _initializeMaxThreads();
  }
  
  AppState get state => _state;
  
  /// Initialize maximum thread count based on device capabilities
  Future<void> _initializeMaxThreads() async {
    try {
      int maxThreads = Platform.numberOfProcessors;
      // Allow up to 2x CPU cores for I/O bound operations
      maxThreads = (maxThreads * 2).clamp(1, 16);
      
      _updateState(_state.copyWith(
        maxThreads: maxThreads,
        config: _state.config.copyWith(threadCount: (maxThreads / 2).ceil()),
      ));
      
      addLog(LogLevel.info, 'System initialized with $maxThreads max threads (${Platform.numberOfProcessors} CPU cores detected)');
    } catch (e) {
      addLog(LogLevel.error, 'Failed to detect system capabilities: $e');
    }
  }
  
  /// Update the internal state and notify listeners
  void _updateState(AppState newState) {
    _state = newState;
    notifyListeners();
  }
  
  /// Select a file for processing
  void selectFile(File file) {
    _updateState(_state.copyWith(selectedFile: file));
    addLog(LogLevel.info, 'Selected file: ${file.path} (${_formatFileSize(file.lengthSync())})');
  }
  
  /// Update encryption algorithm and reset dependent options
  void updateAlgorithm(EncryptionAlgorithm algorithm) {
    final newKeySize = AlgorithmHelper.getDefaultKeySize(algorithm);
    final newMode = AlgorithmHelper.getDefaultMode(algorithm);
    
    final newConfig = _state.config.copyWith(
      algorithm: algorithm,
      keySize: newKeySize,
      mode: newMode,
    );
    
    _updateState(_state.copyWith(config: newConfig));
    addLog(LogLevel.info, 'Algorithm changed to ${algorithm.displayName}');
  }
  
  /// Update key size
  void updateKeySize(int keySize) {
    final newConfig = _state.config.copyWith(keySize: keySize);
    _updateState(_state.copyWith(config: newConfig));
    addLog(LogLevel.debug, 'Key size set to $keySize bits');
  }
  
  /// Update operation mode
  void updateMode(OperationMode mode) {
    final newConfig = _state.config.copyWith(mode: mode);
    _updateState(_state.copyWith(config: newConfig));
    addLog(LogLevel.debug, 'Operation mode set to ${mode.displayName}');
  }
  
  /// Update password
  void updatePassword(String password) {
    final newConfig = _state.config.copyWith(password: password);
    _updateState(_state.copyWith(config: newConfig));
    addLog(LogLevel.debug, 'Password updated (${password.length} characters)');
  }
  
  /// Update thread count
  void updateThreadCount(int threadCount) {
    final clampedCount = threadCount.clamp(1, _state.maxThreads);
    final newConfig = _state.config.copyWith(threadCount: clampedCount);
    _updateState(_state.copyWith(config: newConfig));
    addLog(LogLevel.info, 'Thread count set to $clampedCount');
  }
  
  /// Toggle hardware acceleration
  void toggleHardwareAcceleration(bool enabled) {
    final newConfig = _state.config.copyWith(useHardwareAcceleration: enabled);
    _updateState(_state.copyWith(config: newConfig));
    addLog(LogLevel.info, 'Hardware acceleration ${enabled ? 'enabled' : 'disabled'}');
  }
  
  /// Start encryption process
  Future<void> startEncryption() async {
    if (_state.selectedFile == null) {
      addLog(LogLevel.error, 'No file selected for encryption');
      return;
    }
    
    if (_state.config.password.isEmpty) {
      addLog(LogLevel.error, 'Password is required for encryption');
      return;
    }
    
    addLog(LogLevel.info, 'Starting encryption process...');
    addLog(LogLevel.info, 'Algorithm: ${_state.config.algorithm.displayName}');
    addLog(LogLevel.info, 'Key Size: ${_state.config.keySize} bits');
    addLog(LogLevel.info, 'Mode: ${_state.config.mode.displayName}');
    addLog(LogLevel.info, 'Threads: ${_state.config.threadCount}');
    
    _updateState(_state.copyWith(
      status: ProcessingStatus.processing,
      progress: 0.0,
      statusMessage: 'Initializing encryption...',
    ));
    
    // Simulate encryption process
    await _simulateProcessing(true);
  }
  
  /// Start decryption process
  Future<void> startDecryption() async {
    if (_state.selectedFile == null) {
      addLog(LogLevel.error, 'No file selected for decryption');
      return;
    }
    
    if (_state.config.password.isEmpty) {
      addLog(LogLevel.error, 'Password is required for decryption');
      return;
    }
    
    addLog(LogLevel.info, 'Starting decryption process...');
    addLog(LogLevel.info, 'Algorithm: ${_state.config.algorithm.displayName}');
    addLog(LogLevel.info, 'Key Size: ${_state.config.keySize} bits');
    addLog(LogLevel.info, 'Mode: ${_state.config.mode.displayName}');
    addLog(LogLevel.info, 'Threads: ${_state.config.threadCount}');
    
    _updateState(_state.copyWith(
      status: ProcessingStatus.processing,
      progress: 0.0,
      statusMessage: 'Initializing decryption...',
    ));
    
    // Simulate decryption process
    await _simulateProcessing(false);
  }
  
  /// Simulate processing for demonstration
  Future<void> _simulateProcessing(bool isEncryption) async {
    final operation = isEncryption ? 'Encryption' : 'Decryption';
    final fileSize = _state.selectedFile?.lengthSync() ?? 1024000;
    final chunkCount = (fileSize / 65536).ceil(); // 64KB chunks
    
    addLog(LogLevel.info, 'Processing file in $chunkCount chunks');
    
    int currentChunk = 0;
    _processingTimer = Timer.periodic(const Duration(milliseconds: 200), (timer) {
      if (currentChunk >= chunkCount) {
        timer.cancel();
        _finishProcessing(operation);
        return;
      }
      
      currentChunk++;
      final progress = currentChunk / chunkCount;
      final statusMessage = '${operation.substring(0, operation.length - 3)}ing chunk $currentChunk of $chunkCount...';
      
      _updateState(_state.copyWith(
        progress: progress,
        statusMessage: statusMessage,
      ));
      
      // Occasional log updates
      if (currentChunk % 5 == 0 || currentChunk == chunkCount) {
        addLog(LogLevel.info, 'Processed chunk $currentChunk/$chunkCount (${(progress * 100).toStringAsFixed(1)}%)');
      }
    });
  }
  
  /// Complete the processing
  void _finishProcessing(String operation) {
    _updateState(_state.copyWith(
      status: ProcessingStatus.completed,
      progress: 1.0,
      statusMessage: '$operation completed successfully',
    ));
    
    addLog(LogLevel.info, '$operation completed successfully');
    
    // Reset status after 3 seconds
    Timer(const Duration(seconds: 3), () {
      if (_state.status == ProcessingStatus.completed) {
        _updateState(_state.copyWith(
          status: ProcessingStatus.ready,
          progress: 0.0,
          statusMessage: 'Ready',
        ));
      }
    });
  }
  
  /// Cancel current operation
  void cancelOperation() {
    _processingTimer?.cancel();
    _updateState(_state.copyWith(
      status: ProcessingStatus.cancelled,
      statusMessage: 'Operation cancelled',
    ));
    
    addLog(LogLevel.warning, 'Operation cancelled by user');
    
    // Reset status after 2 seconds
    Timer(const Duration(seconds: 2), () {
      if (_state.status == ProcessingStatus.cancelled) {
        _updateState(_state.copyWith(
          status: ProcessingStatus.ready,
          progress: 0.0,
          statusMessage: 'Ready',
        ));
      }
    });
  }
  
  /// Add a log entry
  void addLog(LogLevel level, String message) {
    final entry = LogEntry(
      timestamp: DateTime.now(),
      level: level,
      message: message,
    );
    
    final updatedLogs = List<LogEntry>.from(_state.logEntries)..add(entry);
    // Keep only last 1000 entries
    if (updatedLogs.length > 1000) {
      updatedLogs.removeRange(0, updatedLogs.length - 1000);
    }
    
    _updateState(_state.copyWith(logEntries: updatedLogs));
    _logStreamController.add(entry);
  }
  
  /// Clear all logs
  void clearLogs() {
    _updateState(_state.copyWith(logEntries: []));
    addLog(LogLevel.info, 'Log cleared');
  }
  
  /// Format file size for display
  String _formatFileSize(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    if (bytes < 1024 * 1024 * 1024) return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
    return '${(bytes / (1024 * 1024 * 1024)).toStringAsFixed(1)} GB';
  }
  
  @override
  void dispose() {
    _processingTimer?.cancel();
    _logTimer?.cancel();
    _logStreamController.close();
    super.dispose();
  }
}