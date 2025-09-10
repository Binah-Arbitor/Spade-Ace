import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../theme/pcb_theme.dart';
import '../../services/app_state_provider.dart';
import '../../models/app_models.dart';

class AdvancedSettingsPanel extends StatefulWidget {
  const AdvancedSettingsPanel({super.key});

  @override
  State<AdvancedSettingsPanel> createState() => _AdvancedSettingsPanelState();
}

class _AdvancedSettingsPanelState extends State<AdvancedSettingsPanel> {
  bool _isExpanded = false;

  @override
  Widget build(BuildContext context) {
    return Consumer<AppStateProvider>(
      builder: (context, provider, child) {
        final state = provider.state;
        final config = state.config;
        final isProcessing = state.status == ProcessingStatus.processing;

        return Card(
          child: ExpansionTile(
            title: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(6),
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: PCBColors.limeGreen),
                    gradient: RadialGradient(
                      colors: [
                        PCBColors.limeGreen.withOpacity(0.2),
                        Colors.transparent,
                      ],
                    ),
                  ),
                  child: Icon(
                    Icons.tune,
                    color: PCBColors.limeGreen,
                    size: 18,
                  ),
                ),
                const SizedBox(width: 12),
                Text(
                  'Advanced Settings',
                  style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const Spacer(),
                Text(
                  _isExpanded ? 'Hide' : 'Show',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: PCBColors.lightGray.withOpacity(0.7),
                  ),
                ),
              ],
            ),
            initiallyExpanded: false,
            onExpansionChanged: (expanded) {
              setState(() {
                _isExpanded = expanded;
              });
            },
            children: [
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Threading controls
                    _buildSettingSection(
                      'Multithreading Control',
                      Icons.memory,
                      [
                        _buildThreadSlider(provider, config, isProcessing, state.maxThreads),
                        const SizedBox(height: 8),
                        _buildThreadInfo(state.maxThreads, config.threadCount),
                      ],
                    ),
                    
                    const SizedBox(height: 20),
                    
                    // Hardware acceleration
                    _buildSettingSection(
                      'Hardware Acceleration',
                      Icons.speed,
                      [
                        SwitchListTile(
                          title: Text(
                            'Enable GPU Acceleration',
                            style: Theme.of(context).textTheme.bodyMedium,
                          ),
                          subtitle: Text(
                            'Utilize GPU for parallel processing (experimental)',
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              color: PCBColors.lightGray.withOpacity(0.7),
                            ),
                          ),
                          value: config.useHardwareAcceleration,
                          onChanged: isProcessing ? null : (value) {
                            provider.toggleHardwareAcceleration(value);
                          },
                          activeColor: PCBColors.limeGreen,
                          inactiveThumbColor: PCBColors.borderColor,
                          contentPadding: EdgeInsets.zero,
                        ),
                        
                        if (config.useHardwareAcceleration) ...[
                          const SizedBox(height: 12),
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              border: Border.all(color: PCBColors.warningYellow.withOpacity(0.5)),
                              borderRadius: BorderRadius.circular(6),
                              color: PCBColors.warningYellow.withOpacity(0.1),
                            ),
                            child: Row(
                              children: [
                                Icon(
                                  Icons.warning_amber_outlined,
                                  color: PCBColors.warningYellow,
                                  size: 18,
                                ),
                                const SizedBox(width: 8),
                                Expanded(
                                  child: Text(
                                    'GPU acceleration is experimental and may not be available on all devices.',
                                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                      color: PCBColors.warningYellow,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ],
                    ),
                    
                    const SizedBox(height: 20),
                    
                    // Performance optimization
                    _buildSettingSection(
                      'Performance Optimization',
                      Icons.trending_up,
                      [
                        _buildOptimizationInfo(config.threadCount, config.useHardwareAcceleration),
                      ],
                    ),
                    
                    const SizedBox(height: 20),
                    
                    // System information
                    _buildSettingSection(
                      'System Information',
                      Icons.info_outline,
                      [
                        _buildSystemInfo(state),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildSettingSection(String title, IconData icon, List<Widget> children) {
    return Container(
      decoration: PCBTheme.circuitDecoration,
      padding: const EdgeInsets.all(12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                icon,
                color: PCBColors.primaryTeal,
                size: 18,
              ),
              const SizedBox(width: 8),
              Text(
                title,
                style: Theme.of(context).textTheme.labelLarge?.copyWith(
                  color: PCBColors.primaryTeal,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          ...children,
        ],
      ),
    );
  }

  Widget _buildThreadSlider(AppStateProvider provider, EncryptionConfig config, 
                           bool isProcessing, int maxThreads) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Thread Count: ${config.threadCount}',
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
            fontWeight: FontWeight.w500,
          ),
        ),
        const SizedBox(height: 8),
        SliderTheme(
          data: SliderTheme.of(context).copyWith(
            activeTrackColor: PCBColors.primaryTeal,
            inactiveTrackColor: PCBColors.borderColor,
            thumbColor: PCBColors.primaryTeal,
            overlayColor: PCBColors.primaryTeal.withOpacity(0.3),
            valueIndicatorColor: PCBColors.primaryTeal,
            valueIndicatorTextStyle: const TextStyle(
              color: PCBColors.deepOffBlack,
              fontWeight: FontWeight.bold,
            ),
          ),
          child: Slider(
            value: config.threadCount.toDouble(),
            min: 1,
            max: maxThreads.toDouble(),
            divisions: maxThreads - 1,
            label: config.threadCount.toString(),
            onChanged: isProcessing ? null : (value) {
              provider.updateThreadCount(value.round());
            },
          ),
        ),
      ],
    );
  }

  Widget _buildThreadInfo(int maxThreads, int currentThreads) {
    final efficiency = (currentThreads / maxThreads * 100).round();
    Color efficiencyColor;
    
    if (efficiency <= 50) {
      efficiencyColor = PCBColors.limeGreen;
    } else if (efficiency <= 75) {
      efficiencyColor = PCBColors.warningYellow;
    } else {
      efficiencyColor = PCBColors.errorRed;
    }

    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        border: Border.all(color: PCBColors.borderColor.withOpacity(0.5)),
        borderRadius: BorderRadius.circular(4),
        color: PCBColors.deepOffBlack.withOpacity(0.3),
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'CPU Cores Available:',
                style: Theme.of(context).textTheme.bodySmall,
              ),
              Text(
                '$maxThreads',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: PCBColors.limeGreen,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Resource Utilization:',
                style: Theme.of(context).textTheme.bodySmall,
              ),
              Text(
                '$efficiency%',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: efficiencyColor,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildOptimizationInfo(int threadCount, bool useGpu) {
    final basePerformance = 1.0;
    final threadMultiplier = threadCount * 0.8; // Diminishing returns
    final gpuMultiplier = useGpu ? 1.5 : 1.0;
    final estimatedPerformance = basePerformance * threadMultiplier * gpuMultiplier;

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        border: Border.all(color: PCBColors.limeGreen.withOpacity(0.3)),
        borderRadius: BorderRadius.circular(6),
        color: PCBColors.limeGreen.withOpacity(0.05),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Estimated Performance Boost',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              fontWeight: FontWeight.bold,
              color: PCBColors.limeGreen,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '${estimatedPerformance.toStringAsFixed(1)}x baseline performance',
            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
              fontSize: 20,
              color: PCBColors.limeGreen,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Factors: ${threadCount} threads${useGpu ? ' + GPU acceleration' : ''}',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: PCBColors.lightGray.withOpacity(0.8),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSystemInfo(AppState state) {
    return Column(
      children: [
        _buildInfoRow('Max Threads:', '${state.maxThreads}'),
        _buildInfoRow('Current Config:', '${state.config.algorithm.displayName} ${state.config.keySize}-bit'),
        _buildInfoRow('Mode:', state.config.mode.displayName),
        _buildInfoRow('Status:', state.statusMessage),
      ],
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: PCBColors.lightGray.withOpacity(0.8),
            ),
          ),
          Text(
            value,
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: PCBColors.white,
              fontWeight: FontWeight.w500,
              fontFamily: 'IBMPlexMono',
            ),
          ),
        ],
      ),
    );
  }
}