import 'package:flutter/material.dart';
import 'package:file_picker/file_picker.dart';
import 'dart:io';
import 'dart:typed_data';
// Use mock bindings for now - replace with 'native_bindings.dart' when C++ library is built
import 'native_bindings_mock.dart' as bindings;

void main() {
  runApp(const SpadeAceApp());
}

class SpadeAceApp extends StatelessWidget {
  const SpadeAceApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Spade Ace - Decryption Tool',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const DecryptionScreen(),
    );
  }
}

class DecryptionScreen extends StatefulWidget {
  const DecryptionScreen({super.key});

  @override
  State<DecryptionScreen> createState() => _DecryptionScreenState();
}

class _DecryptionScreenState extends State<DecryptionScreen> {
  File? _selectedFile;
  bindings.FileAnalysisResult? _analysisResult;
  
  // Manual override dropdowns
  bool _useGPU = false;
  String _gpuPlatform = 'Auto Detect';
  List<String> _availableGPUPlatforms = ['Auto Detect'];
  String _gpuInfo = 'GPU not initialized';
  String? _manualAlgorithm;
  String? _manualKeySize; 
  String? _manualMode;
  String _performanceMode = 'Normal (3 cores)';
  String _attackMethod = 'Brute Force';
  
  // Progress tracking
  bool _isDecrypting = false;
  double _progress = 0.0;
  String _statusMessage = 'Ready';

  final List<String> _algorithms = ['AES', 'DES', '3DES', 'Blowfish', 'Twofish', 'RC4', 'ChaCha20'];
  final List<String> _keySizes = ['128', '192', '256', '512', '1024', '2048'];
  final List<String> _modes = ['ECB', 'CBC', 'CFB', 'OFB', 'CTR', 'GCM'];
  final List<String> _performanceModes = ['Efficiency (1 core)', 'Normal (3 cores)', 'Performance (All cores)'];
  final List<String> _attackMethods = ['Brute Force', 'Dictionary Attack', 'Rainbow Tables', 'Known Plaintext', 'Chosen Plaintext', 'Side Channel'];

  @override
  void initState() {
    super.initState();
    bindings.NativeBindings.init();
    _initializeGPU();
  }

  Future<void> _initializeGPU() async {
    try {
      // In the real implementation, this would call native GPU functions
      // For now, simulate GPU detection
      setState(() {
        _availableGPUPlatforms = ['Auto Detect', 'CUDA', 'OpenCL'];
        _gpuInfo = 'Detecting GPU devices...';
      });
      
      await Future.delayed(const Duration(milliseconds: 500));
      
      setState(() {
        _gpuInfo = 'GPU devices found:\n- NVIDIA GeForce RTX (CUDA)\n- Intel UHD Graphics (OpenCL)';
      });
    } catch (e) {
      setState(() {
        _gpuInfo = 'GPU initialization failed: $e';
      });
    }
  }

  @override
  void dispose() {
    bindings.NativeBindings.cleanup();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text('Spade Ace - File Decryption Tool'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // File Selection Section
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'File Selection',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 12),
                    ElevatedButton.icon(
                      onPressed: _pickFile,
                      icon: const Icon(Icons.folder_open),
                      label: const Text('Select Encrypted File'),
                    ),
                    if (_selectedFile != null) ...[
                      const SizedBox(height: 8),
                      Text('Selected: ${_selectedFile!.path.split('/').last}'),
                      Text('Size: ${_formatFileSize(_selectedFile!.lengthSync())}'),
                    ],
                  ],
                ),
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Detection Results Section
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Auto-Detection Results',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('Algorithm: ${_analysisResult?.detectedAlgorithm != null ? bindings.NativeBindings.algorithmToString(_analysisResult!.detectedAlgorithm) : "Unknown"}'),
                              Text('Key Size: ${_analysisResult?.detectedKeySize ?? "Unknown"}'),
                              Text('Mode: ${_analysisResult?.detectedMode != null ? bindings.NativeBindings.modeToString(_analysisResult!.detectedMode) : "Unknown"}'),
                              Text('Confidence: ${_analysisResult?.confidence != null ? "${(_analysisResult!.confidence * 100).toStringAsFixed(1)}%" : "N/A"}'),
                              if (_analysisResult?.fileType != null)
                                Text('File Type: ${_analysisResult!.fileType}'),
                              if (_analysisResult?.analysisDetails != null) ...[
                                const SizedBox(height: 8),
                                ExpansionTile(
                                  title: const Text('Analysis Details'),
                                  children: [
                                    Padding(
                                      padding: const EdgeInsets.all(16.0),
                                      child: Text(
                                        _analysisResult!.analysisDetails,
                                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                          fontFamily: 'monospace',
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                              ],
                            ],
                          ),
                        ),
                        ElevatedButton(
                          onPressed: _selectedFile != null ? _analyzeFile : null,
                          child: const Text('Analyze'),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Manual Override Section
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Manual Override',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: DropdownButtonFormField<String>(
                            decoration: const InputDecoration(labelText: 'Algorithm'),
                            value: _manualAlgorithm,
                            items: _algorithms.map((String value) {
                              return DropdownMenuItem<String>(
                                value: value,
                                child: Text(value),
                              );
                            }).toList(),
                            onChanged: (String? newValue) {
                              setState(() {
                                _manualAlgorithm = newValue;
                              });
                            },
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: DropdownButtonFormField<String>(
                            decoration: const InputDecoration(labelText: 'Key Size'),
                            value: _manualKeySize,
                            items: _keySizes.map((String value) {
                              return DropdownMenuItem<String>(
                                value: value,
                                child: Text('$value bits'),
                              );
                            }).toList(),
                            onChanged: (String? newValue) {
                              setState(() {
                                _manualKeySize = newValue;
                              });
                            },
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: DropdownButtonFormField<String>(
                            decoration: const InputDecoration(labelText: 'Mode'),
                            value: _manualMode,
                            items: _modes.map((String value) {
                              return DropdownMenuItem<String>(
                                value: value,
                                child: Text(value),
                              );
                            }).toList(),
                            onChanged: (String? newValue) {
                              setState(() {
                                _manualMode = newValue;
                              });
                            },
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Attack Configuration Section
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Attack Configuration',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 12),
                    
                    // GPU Acceleration Section
                    Card(
                      color: Colors.blue.shade50,
                      child: Padding(
                        padding: const EdgeInsets.all(12.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Icon(Icons.computer, color: Colors.blue.shade700),
                                const SizedBox(width: 8),
                                Text(
                                  'GPU Acceleration',
                                  style: TextStyle(
                                    fontSize: 16, 
                                    fontWeight: FontWeight.bold,
                                    color: Colors.blue.shade700,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            SwitchListTile(
                              title: const Text('Enable GPU Acceleration'),
                              subtitle: Text(_useGPU ? 'Using GPU for faster attacks' : 'Using CPU only'),
                              value: _useGPU,
                              onChanged: (bool value) {
                                setState(() {
                                  _useGPU = value;
                                });
                              },
                            ),
                            if (_useGPU) ...[
                              const SizedBox(height: 12),
                              DropdownButtonFormField<String>(
                                decoration: const InputDecoration(labelText: 'GPU Platform'),
                                value: _gpuPlatform,
                                items: _availableGPUPlatforms.map((String value) {
                                  return DropdownMenuItem<String>(
                                    value: value,
                                    child: Text(value),
                                  );
                                }).toList(),
                                onChanged: (String? newValue) {
                                  setState(() {
                                    _gpuPlatform = newValue ?? 'Auto Detect';
                                  });
                                },
                              ),
                              const SizedBox(height: 12),
                              Container(
                                padding: const EdgeInsets.all(8),
                                decoration: BoxDecoration(
                                  border: Border.all(color: Colors.grey.shade300),
                                  borderRadius: BorderRadius.circular(8),
                                ),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    const Text('GPU Information:', style: TextStyle(fontWeight: FontWeight.bold)),
                                    const SizedBox(height: 4),
                                    Text(_gpuInfo, style: const TextStyle(fontSize: 12)),
                                  ],
                                ),
                              ),
                            ],
                          ],
                        ),
                      ),
                    ),
                    
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Expanded(
                          child: DropdownButtonFormField<String>(
                            decoration: const InputDecoration(labelText: 'Attack Method'),
                            value: _attackMethod,
                            items: _attackMethods.map((String value) {
                              return DropdownMenuItem<String>(
                                value: value,
                                child: Text(value),
                              );
                            }).toList(),
                            onChanged: (String? newValue) {
                              setState(() {
                                _attackMethod = newValue!;
                              });
                            },
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: DropdownButtonFormField<String>(
                            decoration: const InputDecoration(labelText: 'Performance Mode'),
                            value: _performanceMode,
                            items: _performanceModes.map((String value) {
                              return DropdownMenuItem<String>(
                                value: value,
                                child: Text(value),
                              );
                            }).toList(),
                            onChanged: (String? newValue) {
                              setState(() {
                                _performanceMode = newValue!;
                              });
                            },
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            
            const SizedBox(height: 16),
            
            // Progress and Control Section
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Decryption Progress',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 12),
                    LinearProgressIndicator(value: _progress),
                    const SizedBox(height: 8),
                    Text(_statusMessage),
                    const SizedBox(height: 12),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        ElevatedButton.icon(
                          onPressed: _selectedFile != null && !_isDecrypting ? _startDecryption : null,
                          icon: const Icon(Icons.play_arrow),
                          label: const Text('Start Decryption'),
                        ),
                        ElevatedButton.icon(
                          onPressed: _isDecrypting ? _stopDecryption : null,
                          icon: const Icon(Icons.stop),
                          label: const Text('Stop'),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _pickFile() async {
    FilePickerResult? result = await FilePicker.platform.pickFiles();
    
    if (result != null) {
      setState(() {
        _selectedFile = File(result.files.single.path!);
        _analysisResult = null;
        _progress = 0.0;
        _statusMessage = 'File selected. Click Analyze to detect encryption parameters.';
      });
    }
  }

  Future<void> _analyzeFile() async {
    if (_selectedFile == null) return;
    
    setState(() {
      _statusMessage = 'Analyzing file...';
    });
    
    try {
      final bytes = await _selectedFile!.readAsBytes();
      final analysisResult = bindings.NativeBindings.analyzeFile(bytes);
      
      setState(() {
        _analysisResult = analysisResult;
        _statusMessage = 'Analysis complete. Ready for decryption.';
      });
      
    } catch (e) {
      setState(() {
        _statusMessage = 'Analysis failed: ${e.toString()}';
      });
    }
  }

  Future<void> _startDecryption() async {
    if (_selectedFile == null) return;
    
    setState(() {
      _isDecrypting = true;
      _progress = 0.0;
      _statusMessage = 'Starting decryption...';
    });
    
    try {
      final bytes = await _selectedFile!.readAsBytes();
      
      // Determine encryption parameters
      bindings.NativeAlgorithm algorithm = _getAlgorithm();
      bindings.NativeMode mode = _getMode();
      int keySize = _getKeySize();
      bindings.NativeAttackMethod attackMethod = _getAttackMethod();
      bindings.NativePerformanceMode performanceMode = _getPerformanceMode();
      
      // Start decryption with progress callback
      bindings.DecryptionResult result;
      
      if (_useGPU) {
        // Use GPU-accelerated decryption
        setState(() {
          _statusMessage = 'Starting GPU-accelerated decryption...';
        });
        
        // Mock GPU decryption for now (would be actual GPU call)
        result = await bindings.NativeBindings.decryptFile(
          data: bytes,
          algorithm: algorithm,
          mode: mode,
          keySize: keySize,
          attackMethod: attackMethod,
          performanceMode: performanceMode,
          onProgress: (progress, status) {
            setState(() {
              _progress = progress;
              _statusMessage = '$status (GPU: ~${(10000 * (1 + (performanceMode == bindings.NativePerformanceMode.performance ? 5 : 1)))} keys/sec)';
            });
          },
        );
      } else {
        result = await bindings.NativeBindings.decryptFile(
          data: bytes,
          algorithm: algorithm,
          mode: mode,
          keySize: keySize,
          attackMethod: attackMethod,
          performanceMode: performanceMode,
          onProgress: (progress, status) {
            setState(() {
              _progress = progress;
              _statusMessage = status;
            });
          },
        );
      }
      
      if (result.success) {
        setState(() {
          _statusMessage = 'Decryption completed successfully! '
              'Time: ${result.timeTaken.toStringAsFixed(2)}s, '
              'Attempts: ${result.attemptsMade}'
              '${result.keyFound != null ? ', Key: ${result.keyFound}' : ''}';
        });
        
        // Optionally save decrypted file
        if (result.data != null) {
          await _saveDecryptedFile(result.data!);
        }
      } else {
        setState(() {
          _statusMessage = 'Decryption failed: ${result.errorMessage ?? "Unknown error"}';
        });
      }
      
    } catch (e) {
      setState(() {
        _statusMessage = 'Decryption error: ${e.toString()}';
      });
    } finally {
      setState(() {
        _isDecrypting = false;
      });
    }
  }
  
  Future<void> _saveDecryptedFile(Uint8List data) async {
    try {
      final directory = _selectedFile!.parent;
      final originalName = _selectedFile!.path.split('/').last;
      final decryptedPath = '${directory.path}/decrypted_$originalName';
      
      final decryptedFile = File(decryptedPath);
      await decryptedFile.writeAsBytes(data);
      
      setState(() {
        _statusMessage += '\nDecrypted file saved to: $decryptedPath';
      });
    } catch (e) {
      setState(() {
        _statusMessage += '\nFailed to save decrypted file: ${e.toString()}';
      });
    }
  }

  bindings.NativeAlgorithm _getAlgorithm() {
    if (_manualAlgorithm != null) {
      switch (_manualAlgorithm!) {
        case 'AES': return bindings.NativeAlgorithm.aes;
        case 'DES': return bindings.NativeAlgorithm.des;
        case '3DES': return bindings.NativeAlgorithm.tripleDes;
        case 'Blowfish': return bindings.NativeAlgorithm.blowfish;
        case 'Twofish': return bindings.NativeAlgorithm.twofish;
        case 'RC4': return bindings.NativeAlgorithm.rc4;
        case 'ChaCha20': return bindings.NativeAlgorithm.chacha20;
        default: return bindings.NativeAlgorithm.aes;
      }
    }
    return _analysisResult?.detectedAlgorithm ?? bindings.NativeAlgorithm.aes;
  }

  bindings.NativeMode _getMode() {
    if (_manualMode != null) {
      switch (_manualMode!) {
        case 'ECB': return bindings.NativeMode.ecb;
        case 'CBC': return bindings.NativeMode.cbc;
        case 'CFB': return bindings.NativeMode.cfb;
        case 'OFB': return bindings.NativeMode.ofb;
        case 'CTR': return bindings.NativeMode.ctr;
        case 'GCM': return bindings.NativeMode.gcm;
        default: return bindings.NativeMode.cbc;
      }
    }
    return _analysisResult?.detectedMode ?? bindings.NativeMode.cbc;
  }

  int _getKeySize() {
    if (_manualKeySize != null) {
      return int.tryParse(_manualKeySize!) ?? 256;
    }
    return _analysisResult?.detectedKeySize ?? 256;
  }

  bindings.NativeAttackMethod _getAttackMethod() {
    switch (_attackMethod) {
      case 'Brute Force': return bindings.NativeAttackMethod.bruteForce;
      case 'Dictionary Attack': return bindings.NativeAttackMethod.dictionary;
      case 'Rainbow Tables': return bindings.NativeAttackMethod.rainbowTable;
      case 'Known Plaintext': return bindings.NativeAttackMethod.knownPlaintext;
      case 'Chosen Plaintext': return bindings.NativeAttackMethod.chosenPlaintext;
      case 'Side Channel': return bindings.NativeAttackMethod.sideChannel;
      default: return bindings.NativeAttackMethod.bruteForce;
    }
  }

  bindings.NativePerformanceMode _getPerformanceMode() {
    switch (_performanceMode) {
      case 'Efficiency (1 core)': return bindings.NativePerformanceMode.efficiency;
      case 'Normal (3 cores)': return bindings.NativePerformanceMode.normal;
      case 'Performance (All cores)': return bindings.NativePerformanceMode.performance;
      default: return bindings.NativePerformanceMode.normal;
    }
  }

  void _stopDecryption() {
    setState(() {
      _isDecrypting = false;
      _statusMessage = 'Decryption stopped by user.';
    });
    bindings.NativeBindings.stopDecryption();
  }

  String _formatFileSize(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    if (bytes < 1024 * 1024 * 1024) return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
    return '${(bytes / (1024 * 1024 * 1024)).toStringAsFixed(1)} GB';
  }
}