import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../theme/pcb_theme.dart';
import '../../services/app_state_provider.dart';
import '../../models/app_models.dart';

class StatusDisplayArea extends StatefulWidget {
  const StatusDisplayArea({super.key});

  @override
  State<StatusDisplayArea> createState() => _StatusDisplayAreaState();
}

class _StatusDisplayAreaState extends State<StatusDisplayArea>
    with TickerProviderStateMixin {
  late AnimationController _pulseController;
  late AnimationController _glowController;
  late Animation<double> _pulseAnimation;
  late Animation<double> _glowAnimation;

  @override
  void initState() {
    super.initState();
    
    _pulseController = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    );
    
    _glowController = AnimationController(
      duration: const Duration(milliseconds: 2000),
      vsync: this,
    );

    _pulseAnimation = Tween<double>(
      begin: 0.5,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _pulseController,
      curve: Curves.easeInOut,
    ));

    _glowAnimation = Tween<double>(
      begin: 0.3,
      end: 0.8,
    ).animate(CurvedAnimation(
      parent: _glowController,
      curve: Curves.easeInOut,
    ));

    _pulseController.repeat(reverse: true);
    _glowController.repeat(reverse: true);
  }

  @override
  void dispose() {
    _pulseController.dispose();
    _glowController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<AppStateProvider>(
      builder: (context, provider, child) {
        final state = provider.state;
        
        return Container(
          decoration: BoxDecoration(
            color: PCBColors.cardBackground,
            border: Border(
              top: BorderSide(color: PCBColors.borderColor, width: 1),
            ),
            boxShadow: [
              BoxShadow(
                color: _getStatusColor(state.status).withOpacity(0.1),
                blurRadius: 8,
                offset: const Offset(0, -2),
              ),
            ],
          ),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Progress indicator
                if (state.status == ProcessingStatus.processing) ...[
                  _buildProgressIndicator(state.progress),
                  const SizedBox(height: 12),
                ],
                
                // Status row
                Row(
                  children: [
                    // Status icon with animation
                    AnimatedBuilder(
                      animation: _pulseAnimation,
                      builder: (context, child) {
                        return Container(
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            border: Border.all(
                              color: _getStatusColor(state.status),
                              width: 2,
                            ),
                            gradient: RadialGradient(
                              colors: [
                                _getStatusColor(state.status).withOpacity(
                                  state.status == ProcessingStatus.processing 
                                      ? _pulseAnimation.value * 0.3 
                                      : 0.2
                                ),
                                Colors.transparent,
                              ],
                            ),
                          ),
                          child: Icon(
                            _getStatusIcon(state.status),
                            color: _getStatusColor(state.status),
                            size: 16,
                          ),
                        );
                      },
                    ),
                    
                    const SizedBox(width: 12),
                    
                    // Status text
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            _getStatusTitle(state.status),
                            style: Theme.of(context).textTheme.labelLarge?.copyWith(
                              color: _getStatusColor(state.status),
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            state.statusMessage,
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              color: PCBColors.lightGray.withOpacity(0.9),
                            ),
                          ),
                        ],
                      ),
                    ),
                    
                    // Progress percentage (when processing)
                    if (state.status == ProcessingStatus.processing) ...[
                      const SizedBox(width: 12),
                      AnimatedBuilder(
                        animation: _glowAnimation,
                        builder: (context, child) {
                          return Container(
                            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                            decoration: BoxDecoration(
                              color: PCBColors.primaryTeal.withOpacity(0.1),
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(
                                color: PCBColors.primaryTeal.withOpacity(_glowAnimation.value),
                                width: 1,
                              ),
                              boxShadow: [
                                BoxShadow(
                                  color: PCBColors.primaryTeal.withOpacity(_glowAnimation.value * 0.3),
                                  blurRadius: 4,
                                ),
                              ],
                            ),
                            child: Text(
                              '${(state.progress * 100).toInt()}%',
                              style: Theme.of(context).textTheme.labelLarge?.copyWith(
                                fontFamily: 'IBMPlexMono',
                                color: PCBColors.primaryTeal,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          );
                        },
                      ),
                    ],
                    
                    // System status indicators
                    const SizedBox(width: 16),
                    _buildSystemIndicators(state),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildProgressIndicator(double progress) {
    return Column(
      children: [
        // Linear progress bar with glow effect
        Container(
          height: 4,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(2),
            boxShadow: [
              BoxShadow(
                color: PCBColors.primaryTeal.withOpacity(0.5),
                blurRadius: 4,
              ),
            ],
          ),
          child: ClipRRect(
            borderRadius: BorderRadius.circular(2),
            child: LinearProgressIndicator(
              value: progress,
              backgroundColor: PCBColors.borderColor,
              valueColor: const AlwaysStoppedAnimation<Color>(PCBColors.primaryTeal),
            ),
          ),
        ),
        
        // Progress details
        const SizedBox(height: 6),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Progress',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: PCBColors.lightGray.withOpacity(0.7),
              ),
            ),
            Text(
              '${(progress * 100).toStringAsFixed(1)}% Complete',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: PCBColors.primaryTeal,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildSystemIndicators(AppState state) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        // Thread count indicator
        _buildIndicator(
          Icons.memory,
          '${state.config.threadCount}T',
          PCBColors.limeGreen,
          'Threads active',
        ),
        
        const SizedBox(width: 12),
        
        // GPU acceleration indicator
        if (state.config.useHardwareAcceleration)
          _buildIndicator(
            Icons.speed,
            'GPU',
            PCBColors.warningYellow,
            'Hardware acceleration enabled',
          ),
        
        const SizedBox(width: 12),
        
        // Connection status indicator
        _buildIndicator(
          Icons.link,
          'C++',
          state.status == ProcessingStatus.processing 
              ? PCBColors.limeGreen 
              : PCBColors.lightGray,
          'Backend connection',
        ),
      ],
    );
  }

  Widget _buildIndicator(IconData icon, String label, Color color, String tooltip) {
    return Tooltip(
      message: tooltip,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 4),
        decoration: BoxDecoration(
          border: Border.all(color: color.withOpacity(0.5)),
          borderRadius: BorderRadius.circular(4),
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
      ),
    );
  }

  Color _getStatusColor(ProcessingStatus status) {
    switch (status) {
      case ProcessingStatus.ready:
        return PCBColors.lightGray;
      case ProcessingStatus.processing:
        return PCBColors.primaryTeal;
      case ProcessingStatus.completed:
        return PCBColors.limeGreen;
      case ProcessingStatus.error:
        return PCBColors.errorRed;
      case ProcessingStatus.cancelled:
        return PCBColors.warningYellow;
    }
  }

  IconData _getStatusIcon(ProcessingStatus status) {
    switch (status) {
      case ProcessingStatus.ready:
        return Icons.radio_button_unchecked;
      case ProcessingStatus.processing:
        return Icons.sync;
      case ProcessingStatus.completed:
        return Icons.check_circle;
      case ProcessingStatus.error:
        return Icons.error;
      case ProcessingStatus.cancelled:
        return Icons.cancel;
    }
  }

  String _getStatusTitle(ProcessingStatus status) {
    switch (status) {
      case ProcessingStatus.ready:
        return 'Ready';
      case ProcessingStatus.processing:
        return 'Processing';
      case ProcessingStatus.completed:
        return 'Completed';
      case ProcessingStatus.error:
        return 'Error';
      case ProcessingStatus.cancelled:
        return 'Cancelled';
    }
  }
}