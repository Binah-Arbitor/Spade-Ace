#!/bin/bash

# Spade Ace Build Script
# Builds the native C++ library and Flutter application

set -e

echo "🔨 Building Spade Ace Decryption Tool"

# Check dependencies
echo "📋 Checking dependencies..."

if ! command -v flutter &> /dev/null; then
    echo "❌ Flutter SDK not found. Please install Flutter."
    exit 1
fi

if ! command -v cmake &> /dev/null; then
    echo "❌ CMake not found. Please install CMake."
    exit 1
fi

# Install Flutter dependencies
echo "📦 Installing Flutter dependencies..."
flutter pub get

# Build native library
echo "🔧 Building native C++ library..."
cd native/cpp

# Create build directory
if [ ! -d "build" ]; then
    mkdir build
fi

cd build

# Configure and build
cmake ..
make -j$(nproc)

# Return to project root
cd ../../..

# Build Flutter app
echo "🏗️ Building Flutter application..."

# Determine platform and build accordingly
case "$OSTYPE" in
    linux*)
        echo "Building for Linux..."
        flutter build linux --release
        ;;
    darwin*)
        echo "Building for macOS..."
        flutter build macos --release
        ;;
    msys*|cygwin*)
        echo "Building for Windows..."
        flutter build windows --release
        ;;
    *)
        echo "Unknown OS type: $OSTYPE"
        echo "Building default..."
        flutter build linux --release
        ;;
esac

echo "✅ Build complete!"
echo "📁 Output directory: build/"

# Run tests
echo "🧪 Running tests..."
flutter test

echo "🎉 All done! Spade Ace is ready to use."