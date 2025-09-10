# Spade Ace Flutter UI

This directory contains the Flutter UI implementation for the Spade Ace high-performance encryption utility. The UI features a PCB/cyber-tech aesthetic with real-time logging capabilities and advanced cryptographic configuration options.

## Features

### 🎨 PCB/Cyber-Tech Aesthetic
- **Dark Mode Theme**: Deep off-black background with teal and lime green accents
- **Circuit Pattern Elements**: Subtle PCB-inspired borders and decorations
- **Neon Glow Effects**: Active elements with animated glow effects
- **Technical Typography**: Fira Code and IBM Plex Mono fonts for technical feel

### 🔐 Encryption Configuration
- **Algorithm Selection**: Support for AES, Serpent, Twofish, RC6, Blowfish, CAST, Camellia, IDEA
- **Dynamic Key Sizes**: Algorithm-dependent key size options (128/192/256 bits)
- **Operation Modes**: CBC, GCM, ECB, CFB, OFB, CTR modes
- **Password Strength Indicator**: Real-time password strength analysis

### ⚡ Advanced Settings
- **Multithreading Control**: Configurable thread count with system detection
- **Hardware Acceleration**: Optional GPU acceleration support
- **Performance Optimization**: Real-time performance estimation
- **System Information**: CPU core detection and utilization monitoring

### 📊 Real-Time Logging
- **Live Console**: Terminal-style log display with syntax highlighting
- **Log Levels**: INFO, WARNING, ERROR, DEBUG with color coding
- **Auto-scroll**: Automatic scrolling to latest entries
- **Log Management**: Clear logs functionality with entry count display

### 📁 File Operations
- **File Selection**: Intuitive file picker with drag-and-drop style interface
- **File Information**: Display of selected file name and size
- **Encryption/Decryption**: Large, prominent action buttons with glow effects
- **Progress Tracking**: Real-time progress indication with percentage display

## Project Structure

```
flutter_ui/
├── lib/
│   ├── main.dart                    # Main app entry point with splash screen
│   ├── models/
│   │   └── app_models.dart          # Data models and enums
│   ├── services/
│   │   └── app_state_provider.dart  # Application state management
│   └── ui/
│       ├── screens/
│       │   └── main_screen.dart     # Main application screen
│       ├── theme/
│       │   └── pcb_theme.dart       # PCB/cyber-tech theme definition
│       └── widgets/
│           ├── file_io_panel.dart           # File selection and operations
│           ├── encryption_config_panel.dart # Encryption configuration
│           ├── advanced_settings_panel.dart # Advanced settings
│           ├── log_console_panel.dart       # Real-time log console
│           └── status_display_area.dart     # Status and progress display
├── pubspec.yaml                     # Dependencies and assets
└── assets/
    ├── fonts/                       # Custom fonts (Fira Code, IBM Plex Mono)
    ├── images/                      # App images and icons
    └── icons/                       # Custom icons
```

## Dependencies

### Core Flutter Packages
- `flutter`: Main Flutter framework
- `provider`: State management
- `cupertino_icons`: iOS-style icons

### File Operations
- `file_picker`: File selection functionality
- `path_provider`: System path access

### System Integration
- `device_info_plus`: System information detection
- `isolate`: Multi-threading support

### Utilities
- `rxdart`: Reactive stream extensions
- `json_annotation`: JSON serialization
- `flutter_staggered_animations`: UI animations

## Key Components

### AppStateProvider
Central state management using Provider pattern:
- Manages encryption configuration
- Handles file selection and processing
- Provides real-time logging
- Simulates backend communication

### PCB Theme
Custom theme implementing cyber-tech aesthetics:
- Color palette with teal/cyan and lime green accents
- Custom button styles with glow effects
- Technical typography with monospace fonts
- Circuit-pattern decorations

### Widget Architecture
Modular widget design:
- **FileIOPanel**: File selection and main operations
- **EncryptionConfigPanel**: Algorithm and security settings
- **AdvancedSettingsPanel**: Performance and system settings
- **LogConsolePanel**: Real-time logging display
- **StatusDisplayArea**: Progress and system status

## UI Layout

The application uses a single-screen, multi-panel layout:

1. **Top**: App bar with branding and system indicators
2. **Main Content**: Scrollable area containing:
   - File I/O panel (prominent, center)
   - Configuration panels (side-by-side)
   - Log console (expandable, full-width)
3. **Bottom**: Status display with progress indicators

## Theme Colors

```dart
// Primary colors
deepOffBlack: #0A0A0A     // Main background
primaryTeal: #00FFFF      // Primary accent (buttons, highlights)
limeGreen: #32FF32        // Success states, indicators
lightGray: #E0E0E0        // Primary text
cardBackground: #1A1A1A   // Panel backgrounds
borderColor: #333333      // Borders and dividers

// Status colors
errorRed: #FF3333         // Errors
warningYellow: #FFD700    // Warnings
infoBlue: #00BFFF         // Information
```

## Animation Features

- **Pulse animations** for active processing states
- **Glow effects** for interactive elements
- **Smooth transitions** between states
- **Auto-scroll animations** in log console
- **Progress animations** with visual feedback

## Backend Integration Preparation

The UI is designed to communicate with a C++ backend using:
- **Isolates** for background processing
- **Stream communication** for real-time logs
- **Event-driven architecture** for responsive updates
- **Configurable threading** to match backend capabilities

## Usage

1. **File Selection**: Click the central file selection area
2. **Configuration**: Set encryption algorithm, key size, and mode
3. **Advanced Settings**: Adjust thread count and hardware acceleration
4. **Processing**: Click ENCRYPT or DECRYPT buttons
5. **Monitoring**: View real-time logs and progress in console
6. **Status**: Monitor progress in bottom status bar

## Notes

- The current implementation includes simulated backend communication for demonstration
- Font assets should be added to the `assets/fonts/` directory
- The UI is responsive and adapts to different screen sizes
- All animations are optimized for smooth performance
- The theme can be easily customized by modifying `pcb_theme.dart`