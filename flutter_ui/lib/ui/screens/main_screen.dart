import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../widgets/file_io_panel.dart';
import '../widgets/encryption_config_panel.dart';
import '../widgets/advanced_settings_panel.dart';
import '../widgets/log_console_panel.dart';
import '../widgets/status_display_area.dart';
import '../theme/pcb_theme.dart';
import '../../services/app_state_provider.dart';

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  final ScrollController _scrollController = ScrollController();

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _buildAppBar(),
      body: Column(
        children: [
          // Main content area
          Expanded(
            child: Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [
                    PCBColors.deepOffBlack,
                    PCBColors.deepOffBlack.withOpacity(0.95),
                  ],
                ),
              ),
              child: _buildMainContent(),
            ),
          ),
          // Status display area (fixed at bottom)
          const StatusDisplayArea(),
        ],
      ),
    );
  }

  PreferredSizeWidget _buildAppBar() {
    return AppBar(
      backgroundColor: PCBColors.cardBackground,
      elevation: 0,
      title: Row(
        children: [
          // App icon with circuit pattern
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: RadialGradient(
                colors: [
                  PCBColors.primaryTeal.withOpacity(0.3),
                  Colors.transparent,
                ],
              ),
              border: Border.all(
                color: PCBColors.primaryTeal,
                width: 2,
              ),
            ),
            child: Icon(
              Icons.security,
              color: PCBColors.primaryTeal,
              size: 24,
            ),
          ),
          const SizedBox(width: 16),
          
          // App title
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'SPADE ACE',
                style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  letterSpacing: 2,
                  fontSize: 20,
                ),
              ),
              Text(
                'High-Performance Encryption Utility',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: PCBColors.primaryTeal,
                  letterSpacing: 1,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
          
          const Spacer(),
          
          // System info
          Consumer<AppStateProvider>(
            builder: (context, provider, child) {
              return _buildSystemBadges(provider.state);
            },
          ),
        ],
      ),
      bottom: PreferredSize(
        preferredSize: const Size.fromHeight(1),
        child: Container(
          height: 1,
          decoration: BoxDecoration(
            gradient: LinearGradient(
              colors: [
                Colors.transparent,
                PCBColors.primaryTeal.withOpacity(0.5),
                Colors.transparent,
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildSystemBadges(AppState state) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        // CPU cores badge
        _buildBadge(
          '${state.maxThreads} Cores',
          PCBColors.limeGreen,
          Icons.memory,
        ),
        const SizedBox(width: 8),
        
        // Crypto++ backend badge
        _buildBadge(
          'Crypto++',
          PCBColors.primaryTeal,
          Icons.code,
        ),
      ],
    );
  }

  Widget _buildBadge(String label, Color color, IconData icon) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        border: Border.all(color: color.withOpacity(0.5)),
        borderRadius: BorderRadius.circular(12),
        color: color.withOpacity(0.1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            icon,
            size: 12,
            color: color,
          ),
          const SizedBox(width: 4),
          Text(
            label,
            style: TextStyle(
              fontFamily: 'IBMPlexMono',
              fontSize: 10,
              color: color,
              fontWeight: FontWeight.bold,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMainContent() {
    return Scrollbar(
      controller: _scrollController,
      thumbVisibility: true,
      child: SingleChildScrollView(
        controller: _scrollController,
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            // File I/O Panel (main interaction area)
            const FileIOPanel(),
            
            const SizedBox(height: 16),
            
            // Configuration panels row
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Left column - Encryption configuration
                Expanded(
                  flex: 2,
                  child: const EncryptionConfigPanel(),
                ),
                
                const SizedBox(width: 16),
                
                // Right column - Advanced settings
                Expanded(
                  flex: 1,
                  child: const AdvancedSettingsPanel(),
                ),
              ],
            ),
            
            const SizedBox(height: 16),
            
            // Log console panel (full width)
            const LogConsolePanel(),
            
            // Bottom spacer for better scrolling
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }
}

/// Custom file selection widget integrated with the main screen
class FileSelectionButton extends StatelessWidget {
  const FileSelectionButton({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppStateProvider>(
      builder: (context, provider, child) {
        return ElevatedButton.icon(
          onPressed: () => _selectFile(provider),
          icon: const Icon(Icons.folder_open),
          label: const Text('SELECT FILE'),
          style: ElevatedButton.styleFrom(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
            backgroundColor: PCBColors.primaryTeal,
            foregroundColor: PCBColors.deepOffBlack,
          ),
        );
      },
    );
  }

  Future<void> _selectFile(AppStateProvider provider) async {
    // File selection logic would be implemented here
    // This is a placeholder for the actual file picker integration
  }
}