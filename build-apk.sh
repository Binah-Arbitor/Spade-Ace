#!/bin/bash

# APK Build Verification Script
# This script validates the build environment and builds APKs safely

set -e

echo "🔍 Starting APK Build Verification..."

# Function to check network connectivity
check_connectivity() {
    echo "📡 Checking network connectivity..."
    
    # Check if we can reach Google's Android repository
    if ping -c 1 dl.google.com >/dev/null 2>&1; then
        echo "✅ Google repository is reachable"
    else
        echo "❌ Cannot reach Google repository"
        return 1
    fi
    
    # Check if we can reach Maven Central
    if ping -c 1 repo1.maven.org >/dev/null 2>&1; then
        echo "✅ Maven Central is reachable"
    else
        echo "⚠️ Cannot reach Maven Central"
    fi
    
    # Check if we can reach Gradle services
    if ping -c 1 services.gradle.org >/dev/null 2>&1; then
        echo "✅ Gradle services are reachable"
    else
        echo "⚠️ Cannot reach Gradle services"
    fi
}

# Function to validate build environment
validate_environment() {
    echo "🔧 Validating build environment..."
    
    # Check Java version
    JAVA_VERSION=$(java -version 2>&1 | head -1)
    echo "Java: $JAVA_VERSION"
    
    # Check available memory
    echo "Available memory: $(free -h | grep '^Mem:' | awk '{print $7}')"
    
    # Check disk space
    echo "Available disk space: $(df -h . | tail -1 | awk '{print $4}')"
    
    # Ensure gradlew is executable
    chmod +x gradlew
    
    # Check Gradle version
    echo "Gradle wrapper version: $(./gradlew --version | grep '^Gradle' | head -1)"
}

# Function to build with retries
build_with_retry() {
    local build_type=$1
    local max_attempts=3
    
    for attempt in $(seq 1 $max_attempts); do
        echo "🔨 Building $build_type APK (attempt $attempt/$max_attempts)..."
        
        if [ "$build_type" = "debug" ]; then
            if ./gradlew assembleDebug --stacktrace --no-daemon; then
                echo "✅ Debug APK built successfully"
                return 0
            fi
        elif [ "$build_type" = "release" ]; then
            if ./gradlew assembleRelease --stacktrace --no-daemon; then
                echo "✅ Release APK built successfully"
                return 0
            fi
        fi
        
        if [ $attempt -lt $max_attempts ]; then
            echo "⏳ Build failed, cleaning and retrying in 30 seconds..."
            ./gradlew clean --no-daemon || true
            sleep 30
        fi
    done
    
    echo "❌ Failed to build $build_type APK after $max_attempts attempts"
    return 1
}

# Function to verify APK integrity
verify_apk() {
    local apk_path=$1
    
    if [ ! -f "$apk_path" ]; then
        echo "❌ APK not found: $apk_path"
        return 1
    fi
    
    # Check file size
    local size=$(stat -c%s "$apk_path" 2>/dev/null || echo "0")
    if [ "$size" -lt 1000000 ]; then
        echo "⚠️ Warning: APK size is only $size bytes"
    else
        echo "✅ APK size: $size bytes"
    fi
    
    # Verify it's a valid ZIP file
    if file "$apk_path" | grep -q "Zip archive"; then
        echo "✅ APK is a valid ZIP archive"
    else
        echo "❌ APK is not a valid ZIP archive"
        return 1
    fi
    
    # Try to list contents
    if unzip -l "$apk_path" > /dev/null 2>&1; then
        echo "✅ APK structure is valid"
    else
        echo "❌ APK structure is invalid"
        return 1
    fi
    
    return 0
}

# Main execution
main() {
    local build_type=${1:-both}
    
    echo "🚀 APK Build Verification Started"
    echo "Build type: $build_type"
    echo "Timestamp: $(date)"
    echo "Working directory: $(pwd)"
    echo "----------------------------------------"
    
    # Validate environment
    validate_environment
    
    # Check connectivity
    if ! check_connectivity; then
        echo "⚠️ Network connectivity issues detected"
        echo "Attempting offline build..."
    fi
    
    # Clean first
    echo "🧹 Cleaning project..."
    ./gradlew clean --no-daemon || echo "Clean failed, continuing..."
    
    # Build based on type
    if [ "$build_type" = "debug" ] || [ "$build_type" = "both" ]; then
        if build_with_retry "debug"; then
            if verify_apk "app/build/outputs/apk/debug/app-debug.apk"; then
                echo "✅ Debug APK verified successfully"
            fi
        fi
    fi
    
    if [ "$build_type" = "release" ] || [ "$build_type" = "both" ]; then
        if build_with_retry "release"; then
            if verify_apk "app/build/outputs/apk/release/app-release.apk"; then
                echo "✅ Release APK verified successfully"
            fi
        fi
    fi
    
    echo "----------------------------------------"
    echo "🎉 APK Build Verification Completed"
    
    # List all APK files created
    echo "📦 Generated APK files:"
    find app/build/outputs/apk -name "*.apk" -type f -exec ls -lh {} \; || echo "No APK files found"
}

# Execute main function with all arguments
main "$@"