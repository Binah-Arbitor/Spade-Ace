import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../theme/pcb_theme.dart';
import '../../services/app_state_provider.dart';
import '../../models/app_models.dart';

class LogConsolePanel extends StatefulWidget {
  const LogConsolePanel({super.key});

  @override
  State<LogConsolePanel> createState() => _LogConsolePanelState();
}

class _LogConsolePanelState extends State<LogConsolePanel> {
  final ScrollController _scrollController = ScrollController();
  bool _isExpanded = true;
  bool _autoScroll = true;

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<AppStateProvider>(
      builder: (context, provider, child) {
        final logEntries = provider.state.logEntries;
        
        // Auto-scroll to bottom when new logs arrive
        if (_autoScroll && logEntries.isNotEmpty) {
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (_scrollController.hasClients) {
              _scrollController.animateTo(
                _scrollController.position.maxScrollExtent,
                duration: const Duration(milliseconds: 300),
                curve: Curves.easeOut,
              );
            }
          });
        }

        return Card(
          child: ExpansionTile(
            title: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(6),
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: PCBColors.primaryTeal),
                    gradient: RadialGradient(
                      colors: [
                        PCBColors.primaryTeal.withOpacity(0.2),
                        Colors.transparent,
                      ],
                    ),
                  ),
                  child: Icon(
                    Icons.terminal,
                    color: PCBColors.primaryTeal,
                    size: 18,
                  ),
                ),
                const SizedBox(width: 12),
                Text(
                  'Real-time Log Console',
                  style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(
                    color: PCBColors.primaryTeal.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(color: PCBColors.primaryTeal.withOpacity(0.5)),
                  ),
                  child: Text(
                    '${logEntries.length}',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: PCBColors.primaryTeal,
                      fontWeight: FontWeight.bold,
                      fontSize: 10,
                    ),
                  ),
                ),
                const Spacer(),
                if (_isExpanded) ...[
                  // Auto-scroll toggle
                  IconButton(
                    icon: Icon(
                      _autoScroll ? Icons.vertical_align_bottom : Icons.vertical_align_center,
                      color: _autoScroll ? PCBColors.limeGreen : PCBColors.lightGray,
                      size: 20,
                    ),
                    onPressed: () {
                      setState(() {
                        _autoScroll = !_autoScroll;
                      });
                    },
                    tooltip: _autoScroll ? 'Disable auto-scroll' : 'Enable auto-scroll',
                  ),
                  // Clear logs button
                  IconButton(
                    icon: const Icon(
                      Icons.clear_all,
                      color: PCBColors.errorRed,
                      size: 20,
                    ),
                    onPressed: logEntries.isNotEmpty ? () => provider.clearLogs() : null,
                    tooltip: 'Clear all logs',
                  ),
                ],
              ],
            ),
            initiallyExpanded: true,
            onExpansionChanged: (expanded) {
              setState(() {
                _isExpanded = expanded;
              });
            },
            children: [
              Container(
                height: 300,
                margin: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: PCBColors.deepOffBlack,
                  border: Border.all(color: PCBColors.borderColor),
                  borderRadius: BorderRadius.circular(8),
                  boxShadow: [
                    BoxShadow(
                      color: PCBColors.primaryTeal.withOpacity(0.1),
                      blurRadius: 4,
                      inset: true,
                    ),
                  ],
                ),
                child: Column(
                  children: [
                    // Console header
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      decoration: BoxDecoration(
                        color: PCBColors.cardBackground,
                        borderRadius: const BorderRadius.only(
                          topLeft: Radius.circular(8),
                          topRight: Radius.circular(8),
                        ),
                        border: Border(
                          bottom: BorderSide(color: PCBColors.borderColor),
                        ),
                      ),
                      child: Row(
                        children: [
                          // Terminal dots (macOS style)
                          _buildTerminalDot(PCBColors.errorRed),
                          const SizedBox(width: 6),
                          _buildTerminalDot(PCBColors.warningYellow),
                          const SizedBox(width: 6),
                          _buildTerminalDot(PCBColors.limeGreen),
                          const SizedBox(width: 16),
                          Text(
                            'spade-ace-crypto.log',
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              fontFamily: 'IBMPlexMono',
                              color: PCBColors.lightGray.withOpacity(0.8),
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          const Spacer(),
                          Text(
                            'Live',
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              fontFamily: 'IBMPlexMono',
                              color: PCBColors.limeGreen,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(width: 8),
                          Container(
                            width: 6,
                            height: 6,
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              color: PCBColors.limeGreen,
                              boxShadow: [
                                BoxShadow(
                                  color: PCBColors.limeGreen.withOpacity(0.5),
                                  blurRadius: 4,
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                    // Log content
                    Expanded(
                      child: logEntries.isEmpty
                          ? _buildEmptyState()
                          : _buildLogContent(logEntries),
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

  Widget _buildTerminalDot(Color color) {
    return Container(
      width: 12,
      height: 12,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: color,
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.terminal,
            color: PCBColors.lightGray.withOpacity(0.3),
            size: 48,
          ),
          const SizedBox(height: 12),
          Text(
            'No logs available',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: PCBColors.lightGray.withOpacity(0.5),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Log messages will appear here during processing',
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: PCBColors.lightGray.withOpacity(0.4),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLogContent(List<LogEntry> logEntries) {
    return Scrollbar(
      controller: _scrollController,
      thumbVisibility: true,
      child: ListView.builder(
        controller: _scrollController,
        padding: const EdgeInsets.all(8),
        itemCount: logEntries.length,
        itemBuilder: (context, index) {
          final entry = logEntries[index];
          return _buildLogEntry(entry);
        },
      ),
    );
  }

  Widget _buildLogEntry(LogEntry entry) {
    final Color levelColor = _getLogLevelColor(entry.level);
    final String levelPrefix = _getLogLevelPrefix(entry.level);
    
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 1),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Timestamp
          Text(
            _formatTimestamp(entry.timestamp),
            style: TextStyle(
              fontFamily: 'IBMPlexMono',
              fontSize: 11,
              color: PCBColors.lightGray.withOpacity(0.6),
            ),
          ),
          const SizedBox(width: 8),
          // Level indicator
          Container(
            width: 6,
            height: 6,
            margin: const EdgeInsets.only(top: 6),
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: levelColor,
              boxShadow: [
                BoxShadow(
                  color: levelColor.withOpacity(0.5),
                  blurRadius: 2,
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          // Level text
          Container(
            width: 60,
            child: Text(
              levelPrefix,
              style: TextStyle(
                fontFamily: 'IBMPlexMono',
                fontSize: 11,
                color: levelColor,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          const SizedBox(width: 8),
          // Message
          Expanded(
            child: Text(
              entry.message,
              style: const TextStyle(
                fontFamily: 'IBMPlexMono',
                fontSize: 12,
                color: PCBColors.lightGray,
                height: 1.2,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Color _getLogLevelColor(LogLevel level) {
    switch (level) {
      case LogLevel.info:
        return PCBColors.infoBlue;
      case LogLevel.warning:
        return PCBColors.warningYellow;
      case LogLevel.error:
        return PCBColors.errorRed;
      case LogLevel.debug:
        return PCBColors.lightGray;
    }
  }

  String _getLogLevelPrefix(LogLevel level) {
    switch (level) {
      case LogLevel.info:
        return '[INFO]';
      case LogLevel.warning:
        return '[WARN]';
      case LogLevel.error:
        return '[ERR]';
      case LogLevel.debug:
        return '[DBG]';
    }
  }

  String _formatTimestamp(DateTime timestamp) {
    return '${timestamp.hour.toString().padLeft(2, '0')}:'
           '${timestamp.minute.toString().padLeft(2, '0')}:'
           '${timestamp.second.toString().padLeft(2, '0')}';
  }
}