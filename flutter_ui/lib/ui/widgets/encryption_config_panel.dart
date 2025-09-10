import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../theme/pcb_theme.dart';
import '../../services/app_state_provider.dart';
import '../../models/app_models.dart';

class EncryptionConfigPanel extends StatefulWidget {
  const EncryptionConfigPanel({super.key});

  @override
  State<EncryptionConfigPanel> createState() => _EncryptionConfigPanelState();
}

class _EncryptionConfigPanelState extends State<EncryptionConfigPanel> {
  final _passwordController = TextEditingController();
  bool _showPassword = false;

  @override
  void dispose() {
    _passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<AppStateProvider>(
      builder: (context, provider, child) {
        final config = provider.state.config;
        final isProcessing = provider.state.status == ProcessingStatus.processing;
        
        return Card(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Header
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(8),
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
                        Icons.security,
                        color: PCBColors.primaryTeal,
                        size: 20,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Text(
                      'Encryption Configuration',
                      style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),
                
                // Algorithm selection
                _buildConfigRow(
                  'Algorithm',
                  DropdownButtonFormField<EncryptionAlgorithm>(
                    value: config.algorithm,
                    onChanged: isProcessing ? null : (value) {
                      if (value != null) {
                        provider.updateAlgorithm(value);
                      }
                    },
                    items: EncryptionAlgorithm.values.map((algorithm) {
                      return DropdownMenuItem(
                        value: algorithm,
                        child: Text(
                          algorithm.displayName,
                          style: const TextStyle(
                            fontFamily: 'FiraCode',
                          ),
                        ),
                      );
                    }).toList(),
                    decoration: _buildInputDecoration('Select encryption algorithm'),
                  ),
                ),
                
                const SizedBox(height: 16),
                
                // Key size selection
                _buildConfigRow(
                  'Key Size (bits)',
                  DropdownButtonFormField<int>(
                    value: config.keySize,
                    onChanged: isProcessing ? null : (value) {
                      if (value != null) {
                        provider.updateKeySize(value);
                      }
                    },
                    items: config.algorithm.supportedKeySizes.map((size) {
                      return DropdownMenuItem(
                        value: size,
                        child: Text(
                          '$size bits',
                          style: const TextStyle(
                            fontFamily: 'FiraCode',
                          ),
                        ),
                      );
                    }).toList(),
                    decoration: _buildInputDecoration('Key strength'),
                  ),
                ),
                
                const SizedBox(height: 16),
                
                // Operation mode selection
                _buildConfigRow(
                  'Operation Mode',
                  DropdownButtonFormField<OperationMode>(
                    value: config.mode,
                    onChanged: isProcessing ? null : (value) {
                      if (value != null) {
                        provider.updateMode(value);
                      }
                    },
                    items: AlgorithmHelper.getSupportedModes(config.algorithm)
                        .map((mode) {
                      return DropdownMenuItem(
                        value: mode,
                        child: Text(
                          mode.displayName,
                          style: const TextStyle(
                            fontFamily: 'FiraCode',
                          ),
                        ),
                      );
                    }).toList(),
                    decoration: _buildInputDecoration('Cipher operation mode'),
                  ),
                ),
                
                const SizedBox(height: 16),
                
                // Password input
                _buildConfigRow(
                  'Password',
                  TextFormField(
                    controller: _passwordController,
                    onChanged: (value) => provider.updatePassword(value),
                    obscureText: !_showPassword,
                    enabled: !isProcessing,
                    decoration: _buildInputDecoration('Enter encryption password').copyWith(
                      suffixIcon: IconButton(
                        icon: Icon(
                          _showPassword ? Icons.visibility_off : Icons.visibility,
                          color: PCBColors.lightGray,
                        ),
                        onPressed: () {
                          setState(() {
                            _showPassword = !_showPassword;
                          });
                        },
                      ),
                    ),
                    style: const TextStyle(
                      fontFamily: 'FiraCode',
                    ),
                  ),
                ),
                
                // Password strength indicator
                if (_passwordController.text.isNotEmpty) ...[
                  const SizedBox(height: 8),
                  _buildPasswordStrengthIndicator(_passwordController.text),
                ],
                
                const SizedBox(height: 20),
                
                // Configuration summary
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    border: Border.all(color: PCBColors.borderColor),
                    borderRadius: BorderRadius.circular(6),
                    color: PCBColors.deepOffBlack.withOpacity(0.3),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Configuration Summary',
                        style: Theme.of(context).textTheme.labelLarge?.copyWith(
                          color: PCBColors.primaryTeal,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 8),
                      _buildSummaryRow('Algorithm:', config.algorithm.displayName),
                      _buildSummaryRow('Key Size:', '${config.keySize} bits'),
                      _buildSummaryRow('Mode:', config.mode.displayName),
                      _buildSummaryRow('Password:', config.password.isEmpty 
                          ? 'Not set' 
                          : '${config.password.length} characters'),
                    ],
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
  
  Widget _buildConfigRow(String label, Widget child) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: Theme.of(context).textTheme.labelLarge?.copyWith(
            color: PCBColors.lightGray,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 6),
        child,
      ],
    );
  }
  
  InputDecoration _buildInputDecoration(String hint) {
    return InputDecoration(
      hintText: hint,
      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6),
        borderSide: const BorderSide(color: PCBColors.borderColor),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6),
        borderSide: const BorderSide(color: PCBColors.borderColor),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(6),
        borderSide: const BorderSide(color: PCBColors.primaryTeal, width: 2),
      ),
      filled: true,
      fillColor: PCBColors.cardBackground.withOpacity(0.5),
    );
  }
  
  Widget _buildSummaryRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: PCBColors.lightGray.withOpacity(0.8),
            ),
          ),
          Text(
            value,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: PCBColors.white,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }
  
  Widget _buildPasswordStrengthIndicator(String password) {
    final strength = _calculatePasswordStrength(password);
    Color color;
    String label;
    
    if (strength < 0.3) {
      color = PCBColors.errorRed;
      label = 'Weak';
    } else if (strength < 0.6) {
      color = PCBColors.warningYellow;
      label = 'Medium';
    } else if (strength < 0.8) {
      color = PCBColors.primaryTeal;
      label = 'Strong';
    } else {
      color = PCBColors.limeGreen;
      label = 'Very Strong';
    }
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Password Strength: $label',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: color,
                fontWeight: FontWeight.w500,
              ),
            ),
            Text(
              '${(strength * 100).toInt()}%',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                color: color,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
        const SizedBox(height: 4),
        ClipRRect(
          borderRadius: BorderRadius.circular(2),
          child: LinearProgressIndicator(
            value: strength,
            backgroundColor: PCBColors.borderColor,
            valueColor: AlwaysStoppedAnimation<Color>(color),
            minHeight: 4,
          ),
        ),
      ],
    );
  }
  
  double _calculatePasswordStrength(String password) {
    if (password.isEmpty) return 0.0;
    
    double strength = 0.0;
    
    // Length
    if (password.length >= 8) strength += 0.2;
    if (password.length >= 12) strength += 0.1;
    if (password.length >= 16) strength += 0.1;
    
    // Character variety
    if (password.contains(RegExp(r'[a-z]'))) strength += 0.15;
    if (password.contains(RegExp(r'[A-Z]'))) strength += 0.15;
    if (password.contains(RegExp(r'[0-9]'))) strength += 0.15;
    if (password.contains(RegExp(r'[!@#$%^&*(),.?":{}|<>]'))) strength += 0.15;
    
    return strength.clamp(0.0, 1.0);
  }
}