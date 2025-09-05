# Android to QT Framework Migration Summary

## Problem Statement
Based on the request: "UI관련 부분을 전부 지우고 깔끔하게 재작성해줘. QT기반으로"

The original project was an Android application using Kotlin and Jetpack Compose. The requirement was to completely rewrite the UI using QT framework for a clean, cross-platform desktop application.

## Solution: Complete Migration to QT/C++

### Why QT?
1. **Cross-Platform**: Runs on Windows, Linux, macOS
2. **Native Performance**: C++ provides better performance for cryptographic operations
3. **Modern UI**: QT Widgets provide a clean, modern interface
4. **Better Threading**: QT's threading model is excellent for parallel processing
5. **Professional Look**: Native desktop experience

### Migration Overview

#### 1. Framework Change
- **From**: Android/Kotlin/Jetpack Compose
- **To**: QT6/C++/QT Widgets
- **Build System**: CMake and QMake support

#### 2. Architecture Changes
- **From**: MVVM with StateFlow (Android)
- **To**: Object-oriented with QT signals/slots
- **Threading**: Kotlin Coroutines → QThread workers
- **UI**: Jetpack Compose → QT Widgets

#### 3. Core Components Ported

##### UI Components
- `MainActivity.kt` → `MainWindow.h/cpp` - Main application window
- `SpadeAceApp.kt` → `MainWindow.cpp` - Tab-based interface
- `DecryptionScreen.kt` → `DecryptionWindow.h/cpp` - Attack configuration UI
- `FileOperationsScreen.kt` → `FileOperationsWindow.h/cpp` - File browser
- `SettingsScreen.kt` → `SettingsWindow.h/cpp` - Settings interface
- `TextDecryptionScreen.kt` → Integrated into DecryptionWindow

##### Core Logic
- `DecryptionEngine.kt` → `DecryptionEngine.h/cpp` - Multi-threaded attack engine
- `Models.kt` → `Models.h/cpp` - Data structures and enums
- `GpuDetector.kt` → Integrated into SettingsWindow
- New: `CryptoUtils.h/cpp` - Encryption utilities

#### 4. Technology Stack Changes

| Component | Android | QT |
|-----------|---------|-----|
| Language | Kotlin | C++17 |
| UI Framework | Jetpack Compose | QT6 Widgets |
| Build System | Gradle | CMake/QMake |
| Crypto Library | BouncyCastle | OpenSSL |
| Threading | Coroutines | QThread |
| Packaging | APK | Native executables |

#### 5. File Structure Changes

**Before (Android)**:
```
app/src/main/java/com/binah/spadeace/
├── ui/screens/          # Compose screens
├── ui/theme/           # Material theme
├── core/               # Business logic
├── data/               # Data models
└── MainActivity.kt     # Entry point
```

**After (QT)**:
```
src/
├── core/               # Business logic (C++)
├── data/               # Data models (C++)
├── ui/                 # Window classes
├── MainWindow.h/cpp    # Main window
└── main.cpp           # Entry point
```

#### 6. Build System Migration

**Removed**:
- `build.gradle`, `settings.gradle`
- `gradlew`, `gradle.properties`
- Android manifest and resources
- APK build scripts

**Added**:
- `CMakeLists.txt` - CMake build configuration
- `SpadeAce.pro` - QMake project file
- `.github/workflows/build-qt.yml` - Cross-platform CI/CD

#### 7. Features Preserved

All core functionality has been preserved:
- ✅ 7 attack types (Brute Force, Dictionary, etc.)
- ✅ Multi-threading support
- ✅ File browser and operations
- ✅ Performance settings and optimization
- ✅ Hardware acceleration options
- ✅ Real-time progress tracking
- ✅ Dark theme interface

#### 8. New Capabilities

QT version adds:
- ✅ Cross-platform support (Windows/Linux/macOS)
- ✅ Better performance with native C++
- ✅ Professional desktop UI experience
- ✅ Better memory management
- ✅ Native file system integration

### Build Instructions

**Prerequisites**:
- QT 6.0+
- CMake 3.16+
- OpenSSL 1.1.1+
- C++17 compliant compiler

**Build Commands**:
```bash
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release
cmake --build . --config Release
```

### Migration Benefits

1. **Performance**: C++ provides better cryptographic performance
2. **Cross-Platform**: Works on all major desktop platforms
3. **Native Experience**: Better integration with desktop environments
4. **Maintainability**: Cleaner, more structured codebase
5. **Professional**: Desktop application suitable for professional use
- `NavigationBar` → `BottomNavigation`
- `containerColor` → `backgroundColor`

### Files Modified
```
app/build.gradle
app/src/main/res/values/themes.xml
app/src/main/res/values-night/themes.xml  
app/src/main/res/values/colors.xml
app/src/main/java/com/binah/spadeace/MainActivity.kt
app/src/main/java/com/binah/spadeace/ui/SpadeAceApp.kt
app/src/main/java/com/binah/spadeace/ui/theme/Theme.kt
app/src/main/java/com/binah/spadeace/ui/theme/Type.kt
app/src/main/java/com/binah/spadeace/ui/screens/DecryptionScreen.kt
app/src/main/java/com/binah/spadeace/ui/screens/FileOperationsScreen.kt
app/src/main/java/com/binah/spadeace/ui/screens/SettingsScreen.kt
app/src/main/java/com/binah/spadeace/ui/screens/TextDecryptionScreen.kt
```

## Result
- ✅ Eliminates the "Theme.Material3.DayNight not found" error
- ✅ Provides more stable theming with automatic light/dark switching
- ✅ Better compatibility across Android versions
- ✅ Simplified theme management
- ✅ No breaking changes to app functionality

## Future Considerations
This migration ensures long-term stability. If you want to return to Material3 in the future, you would need to:
1. Implement custom DayNight theme handling
2. Use `isSystemInDarkTheme()` for manual theme switching
3. Or wait for Material3 to provide built-in DayNight support

For now, Material2 provides the most stable and reliable theming solution.