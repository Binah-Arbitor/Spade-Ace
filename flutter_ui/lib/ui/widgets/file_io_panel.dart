import 'dart:io';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:file_picker/file_picker.dart';
import '../theme/pcb_theme.dart';
import '../../services/app_state_provider.dart';
import '../../models/app_models.dart';

class FileIOPanel extends StatelessWidget {
  const FileIOPanel({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppStateProvider>(
      builder: (context, provider, child) {
        final state = provider.state;
        final isProcessing = state.status == ProcessingStatus.processing;
        
        return Card(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Header
                Row(
                  children: [
                    Icon(
                      Icons.folder_open,
                      color: PCBColors.primaryTeal,
                      size: 24,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      'File I/O Operations',
                      style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                
                // File selection area
                Container(
                  decoration: BoxDecoration(
                    border: Border.all(
                      color: state.selectedFile != null 
                          ? PCBColors.primaryTeal 
                          : PCBColors.borderColor,
                      width: 2,
                      style: BorderStyle.solid,
                    ),
                    borderRadius: BorderRadius.circular(8),
                    color: PCBColors.cardBackground.withOpacity(0.5),
                  ),
                  child: InkWell(
                    onTap: isProcessing ? null : _selectFile,
                    borderRadius: BorderRadius.circular(8),
                    child: Container(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          // Circuit pattern icon
                          Container(
                            padding: const EdgeInsets.all(16),
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              border: Border.all(
                                color: PCBColors.primaryTeal,
                                width: 2,
                              ),
                              gradient: RadialGradient(
                                colors: [
                                  PCBColors.primaryTeal.withOpacity(0.1),
                                  Colors.transparent,
                                ],
                              ),
                            ),
                            child: Icon(
                              state.selectedFile != null 
                                  ? Icons.description 
                                  : Icons.add_circle_outline,
                              size: 48,
                              color: PCBColors.primaryTeal,
                            ),
                          ),
                          const SizedBox(height: 16),
                          
                          // File info or instruction
                          if (state.selectedFile != null) ...[
                            Text(
                              'Selected File',
                              style: Theme.of(context).textTheme.labelLarge?.copyWith(
                                color: PCBColors.primaryTeal,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              _getFileName(state.selectedFile!.path),
                              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                              textAlign: TextAlign.center,
                            ),
                            const SizedBox(height: 4),
                            Text(
                              _formatFileSize(state.selectedFile!.lengthSync()),
                              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                color: PCBColors.limeGreen,
                              ),
                            ),
                          ] else ...[
                            Text(
                              'Select File to Process',
                              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(height: 8),
                            Text(
                              'Click here to choose a file for encryption or decryption',
                              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                color: PCBColors.lightGray.withOpacity(0.7),
                              ),
                              textAlign: TextAlign.center,
                            ),
                          ],
                        ],
                      ),
                    ),
                  ),
                ),
                
                const SizedBox(height: 20),
                
                // Action buttons
                Row(
                  children: [
                    // Encrypt button
                    Expanded(
                      child: Container(
                        decoration: !isProcessing && state.selectedFile != null 
                            ? PCBTheme.glowDecoration 
                            : null,
                        child: ElevatedButton.icon(
                          onPressed: !isProcessing && 
                                     state.selectedFile != null && 
                                     state.config.password.isNotEmpty
                              ? () => provider.startEncryption()
                              : null,
                          icon: const Icon(Icons.lock),
                          label: const Text(
                            'ENCRYPT',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              letterSpacing: 1.2,
                            ),
                          ),
                          style: ElevatedButton.styleFrom(
                            padding: const EdgeInsets.symmetric(vertical: 16),
                            backgroundColor: PCBColors.primaryTeal,
                            foregroundColor: PCBColors.deepOffBlack,
                          ),
                        ),
                      ),
                    ),
                    
                    const SizedBox(width: 12),
                    
                    // Decrypt button
                    Expanded(
                      child: Container(
                        decoration: !isProcessing && state.selectedFile != null 
                            ? PCBTheme.glowDecoration 
                            : null,
                        child: ElevatedButton.icon(
                          onPressed: !isProcessing && 
                                     state.selectedFile != null && 
                                     state.config.password.isNotEmpty
                              ? () => provider.startDecryption()
                              : null,
                          icon: const Icon(Icons.lock_open),
                          label: const Text(
                            'DECRYPT',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              letterSpacing: 1.2,
                            ),
                          ),
                          style: ElevatedButton.styleFrom(
                            padding: const EdgeInsets.symmetric(vertical: 16),
                            backgroundColor: PCBColors.limeGreen,
                            foregroundColor: PCBColors.deepOffBlack,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
                
                // Cancel button (only show when processing)
                if (isProcessing) ...[
                  const SizedBox(height: 12),
                  OutlinedButton.icon(
                    onPressed: () => provider.cancelOperation(),
                    icon: const Icon(Icons.stop),
                    label: const Text('CANCEL OPERATION'),
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      side: const BorderSide(color: PCBColors.errorRed),
                      foregroundColor: PCBColors.errorRed,
                    ),
                  ),
                ],
              ],
            ),
          ),
        );
      },
    );
  }
  
  /// Select file using file picker
  Future<void> _selectFile() async {
    try {
      FilePickerResult? result = await FilePicker.platform.pickFiles(
        type: FileType.any,
        allowMultiple: false,
      );
      
      if (result != null && result.files.single.path != null) {
        final file = File(result.files.single.path!);
        // Access provider without context in async method
        // This would normally be handled by a proper state management approach
      }
    } catch (e) {
      // Handle error - would normally show a snackbar or dialog
      debugPrint('Error selecting file: $e');
    }
  }
  
  /// Extract filename from path
  String _getFileName(String path) {
    return path.split(Platform.pathSeparator).last;
  }
  
  /// Format file size for display
  String _formatFileSize(int bytes) {
    if (bytes < 1024) return '$bytes B';
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(1)} KB';
    if (bytes < 1024 * 1024 * 1024) return '${(bytes / (1024 * 1024)).toStringAsFixed(1)} MB';
    return '${(bytes / (1024 * 1024 * 1024)).toStringAsFixed(1)} GB';
  }
}