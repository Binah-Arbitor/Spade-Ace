import 'dart:ffi';
import 'dart:typed_data';
import 'dart:io';
import 'package:ffi/ffi.dart';

// Native library loading
final DynamicLibrary _nativeLib = () {
  if (Platform.isMacOS || Platform.isIOS) {
    return DynamicLibrary.open('spade_ace_native.framework/spade_ace_native');
  } else if (Platform.isAndroid || Platform.isLinux) {
    return DynamicLibrary.open('libspade_ace_native.so');
  } else if (Platform.isWindows) {
    return DynamicLibrary.open('spade_ace_native.dll');
  } else {
    throw UnsupportedError('Unsupported platform');
  }
}();

// Enum mappings
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

// Structs
final class CAnalysisResult extends Struct {
  @Int32()
  external int detectedAlgorithm;
  
  @Int32()
  external int detectedMode;
  
  @Int32()
  external int detectedKeySize;
  
  @Double()
  external double confidence;
  
  external Pointer<Utf8> fileType;
  external Pointer<Utf8> analysisDetails;
}

final class CDecryptionResult extends Struct {
  @Bool()
  external bool success;
  
  external Pointer<Uint8> data;
  
  @Size()
  external int dataSize;
  
  external Pointer<Utf8> errorMessage;
  external Pointer<Utf8> keyFound;
  
  @Double()
  external double timeTaken;
  
  @Size()
  external int attemptsMade;
}

// Function signatures
typedef InitSpadeAceC = Void Function();
typedef InitSpadeAce = void Function();

typedef CleanupSpadeAceC = Void Function();
typedef CleanupSpadeAce = void Function();

typedef AnalyzeFileC = Pointer<CAnalysisResult> Function(Pointer<Uint8> data, Size size);
typedef AnalyzeFile = Pointer<CAnalysisResult> Function(Pointer<Uint8> data, int size);

typedef FreeAnalysisResultC = Void Function(Pointer<CAnalysisResult> result);
typedef FreeAnalysisResult = void Function(Pointer<CAnalysisResult> result);

typedef ProgressCallbackC = Void Function(Double progress, Pointer<Utf8> status);
typedef ProgressCallback = void Function(double progress, Pointer<Utf8> status);

typedef DecryptFileC = Pointer<CDecryptionResult> Function(
  Pointer<Uint8> data, 
  Size size,
  Int32 algorithm,
  Int32 mode,
  Int32 keySize,
  Int32 attackMethod,
  Int32 performanceMode,
  Pointer<NativeFunction<ProgressCallbackC>> callback
);

typedef DecryptFile = Pointer<CDecryptionResult> Function(
  Pointer<Uint8> data, 
  int size,
  int algorithm,
  int mode,
  int keySize,
  int attackMethod,
  int performanceMode,
  Pointer<NativeFunction<ProgressCallbackC>> callback
);

typedef FreeDecryptionResultC = Void Function(Pointer<CDecryptionResult> result);
typedef FreeDecryptionResult = void Function(Pointer<CDecryptionResult> result);

typedef StopDecryptionC = Void Function();
typedef StopDecryption = void Function();

typedef AlgorithmToStringC = Pointer<Utf8> Function(Int32 algorithm);
typedef AlgorithmToString = Pointer<Utf8> Function(int algorithm);

typedef ModeToStringC = Pointer<Utf8> Function(Int32 mode);
typedef ModeToString = Pointer<Utf8> Function(int mode);

// Native bindings
class NativeBindings {
  static final InitSpadeAce _initSpadeAce =
      _nativeLib.lookup<NativeFunction<InitSpadeAceC>>('init_spade_ace').asFunction();
  
  static final CleanupSpadeAce _cleanupSpadeAce =
      _nativeLib.lookup<NativeFunction<CleanupSpadeAceC>>('cleanup_spade_ace').asFunction();
  
  static final AnalyzeFile _analyzeFile =
      _nativeLib.lookup<NativeFunction<AnalyzeFileC>>('analyze_file').asFunction();
  
  static final FreeAnalysisResult _freeAnalysisResult =
      _nativeLib.lookup<NativeFunction<FreeAnalysisResultC>>('free_analysis_result').asFunction();
  
  static final DecryptFile _decryptFile =
      _nativeLib.lookup<NativeFunction<DecryptFileC>>('decrypt_file').asFunction();
  
  static final FreeDecryptionResult _freeDecryptionResult =
      _nativeLib.lookup<NativeFunction<FreeDecryptionResultC>>('free_decryption_result').asFunction();
  
  static final StopDecryption _stopDecryption =
      _nativeLib.lookup<NativeFunction<StopDecryptionC>>('stop_decryption').asFunction();
  
  static final AlgorithmToString _algorithmToString =
      _nativeLib.lookup<NativeFunction<AlgorithmToStringC>>('algorithm_to_string').asFunction();
  
  static final ModeToString _modeToString =
      _nativeLib.lookup<NativeFunction<ModeToStringC>>('mode_to_string').asFunction();
  
  static void init() => _initSpadeAce();
  static void cleanup() => _cleanupSpadeAce();
  
  static FileAnalysisResult analyzeFile(Uint8List data) {
    final Pointer<Uint8> dataPtr = malloc<Uint8>(data.length);
    final Uint8List nativeData = dataPtr.asTypedList(data.length);
    nativeData.setAll(0, data);
    
    final result = _analyzeFile(dataPtr, data.length);
    
    if (result == nullptr) {
      malloc.free(dataPtr);
      throw Exception('Failed to analyze file');
    }
    
    final analysisResult = FileAnalysisResult(
      detectedAlgorithm: _algorithmFromInt(result.ref.detectedAlgorithm),
      detectedMode: _modeFromInt(result.ref.detectedMode),
      detectedKeySize: result.ref.detectedKeySize,
      confidence: result.ref.confidence,
      fileType: result.ref.fileType.toDartString(),
      analysisDetails: result.ref.analysisDetails.toDartString(),
    );
    
    _freeAnalysisResult(result);
    malloc.free(dataPtr);
    
    return analysisResult;
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
    final Pointer<Uint8> dataPtr = malloc<Uint8>(data.length);
    final Uint8List nativeData = dataPtr.asTypedList(data.length);
    nativeData.setAll(0, data);
    
    Pointer<NativeFunction<ProgressCallbackC>>? callbackPtr;
    
    if (onProgress != null) {
      // Create native callback
      callbackPtr = Pointer.fromFunction<ProgressCallbackC>(_progressCallback);
      _progressCallbacks[callbackPtr.address] = onProgress;
    }
    
    try {
      final result = _decryptFile(
        dataPtr,
        data.length,
        algorithm.value,
        mode.value,
        keySize,
        attackMethod.value,
        performanceMode.value,
        callbackPtr ?? nullptr,
      );
      
      if (result == nullptr) {
        throw Exception('Failed to start decryption');
      }
      
      Uint8List? decryptedData;
      if (result.ref.data != nullptr && result.ref.dataSize > 0) {
        decryptedData = result.ref.data.asTypedList(result.ref.dataSize);
      }
      
      final decryptionResult = FileDecryptionResult(
        success: result.ref.success,
        data: decryptedData,
        errorMessage: result.ref.errorMessage != nullptr ? result.ref.errorMessage.toDartString() : null,
        keyFound: result.ref.keyFound != nullptr ? result.ref.keyFound.toDartString() : null,
        timeTaken: result.ref.timeTaken,
        attemptsMade: result.ref.attemptsMade,
      );
      
      _freeDecryptionResult(result);
      return decryptionResult;
      
    } finally {
      if (callbackPtr != null) {
        _progressCallbacks.remove(callbackPtr.address);
      }
      malloc.free(dataPtr);
    }
  }
  
  static void stopDecryption() => _stopDecryption();
  
  static String algorithmToString(NativeAlgorithm algorithm) {
    final ptr = _algorithmToString(algorithm.value);
    return ptr.toDartString();
  }
  
  static String modeToString(NativeMode mode) {
    final ptr = _modeToString(mode.value);
    return ptr.toDartString();
  }
  
  // Helper functions
  static NativeAlgorithm _algorithmFromInt(int value) {
    return NativeAlgorithm.values.firstWhere((e) => e.value == value, orElse: () => NativeAlgorithm.unknown);
  }
  
  static NativeMode _modeFromInt(int value) {
    return NativeMode.values.firstWhere((e) => e.value == value, orElse: () => NativeMode.unknown);
  }
  
  // Progress callback management
  static final Map<int, Function(double, String)> _progressCallbacks = {};
  
  static void _progressCallback(double progress, Pointer<Utf8> status) {
    // This would need proper callback handling
    // For now, it's a placeholder
  }
}

// Result classes
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