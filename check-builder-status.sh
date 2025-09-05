#!/bin/bash

# APK Builder Status Check Script
# Checks the health of build environment and suggests the best builder to use

set -e

echo "🔍 APK Builder Status Check"
echo "=========================="

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check command availability
check_command() {
    if command -v $1 >/dev/null 2>&1; then
        print_status $GREEN "✅ $1 is available"
        return 0
    else
        print_status $RED "❌ $1 is not available"
        return 1
    fi
}

# Function to check network connectivity
check_network() {
    local host=$1
    local name=$2
    
    if ping -c 1 -W 5 $host >/dev/null 2>&1; then
        print_status $GREEN "✅ $name is reachable"
        return 0
    else
        print_status $RED "❌ $name is unreachable"
        return 1
    fi
}

# System information
echo ""
print_status $BLUE "🖥️  System Information"
echo "OS: $(uname -s) $(uname -r)"
echo "Architecture: $(uname -m)"
echo "Available cores: $(nproc)"
echo "Available memory: $(free -h | grep '^Mem:' | awk '{print $7}' 2>/dev/null || echo 'Unknown')"
echo "Available disk: $(df -h . | tail -1 | awk '{print $4}' 2>/dev/null || echo 'Unknown')"

# Check required tools
echo ""
print_status $BLUE "🔧 Required Tools"
java_ok=false
gradle_ok=false

if check_command java; then
    java_version=$(java -version 2>&1 | head -1)
    echo "   $java_version"
    java_ok=true
fi

if [ -f "./gradlew" ]; then
    if [ -x "./gradlew" ]; then
        print_status $GREEN "✅ Gradle wrapper is executable"
        gradle_version=$(./gradlew --version 2>/dev/null | grep '^Gradle' | head -1 || echo "Unknown")
        echo "   $gradle_version"
        gradle_ok=true
    else
        print_status $YELLOW "⚠️ Gradle wrapper exists but is not executable"
        echo "   Run: chmod +x gradlew"
    fi
else
    print_status $RED "❌ Gradle wrapper not found"
fi

# Check build files
echo ""
print_status $BLUE "📁 Build Configuration"
build_files_ok=true

if [ -f "build.gradle" ]; then
    print_status $GREEN "✅ Root build.gradle found"
else
    print_status $RED "❌ Root build.gradle missing"
    build_files_ok=false
fi

if [ -f "app/build.gradle" ]; then
    print_status $GREEN "✅ App build.gradle found"
else
    print_status $RED "❌ App build.gradle missing"
    build_files_ok=false
fi

if [ -f "gradle.properties" ]; then
    print_status $GREEN "✅ gradle.properties found"
else
    print_status $YELLOW "⚠️ gradle.properties missing (optional)"
fi

# Check source code
if [ -d "app/src/main" ]; then
    print_status $GREEN "✅ Source code directory found"
else
    print_status $RED "❌ Source code directory missing"
    build_files_ok=false
fi

# Network connectivity check
echo ""
print_status $BLUE "🌐 Network Connectivity"
network_ok=0

check_network "8.8.8.8" "Google DNS" && ((network_ok++))
check_network "dl.google.com" "Google Android repository" && ((network_ok++))
check_network "repo1.maven.org" "Maven Central" && ((network_ok++))
check_network "services.gradle.org" "Gradle services" && ((network_ok++))

# Check for existing caches
echo ""
print_status $BLUE "💾 Cache Status"
if [ -d "~/.gradle/caches" ] || [ -d ".gradle" ]; then
    print_status $GREEN "✅ Gradle cache exists"
    cache_available=true
else
    print_status $YELLOW "⚠️ No Gradle cache found"
    cache_available=false
fi

# Calculate overall health
echo ""
print_status $BLUE "📊 Overall Status"
total_checks=0
passed_checks=0

# Count checks
$java_ok && ((passed_checks++))
((total_checks++))

$gradle_ok && ((passed_checks++))
((total_checks++))

$build_files_ok && ((passed_checks++))
((total_checks++))

[ $network_ok -gt 0 ] && ((passed_checks++))
((total_checks++))

health_score=$((passed_checks * 100 / total_checks))

if [ $health_score -ge 80 ]; then
    print_status $GREEN "✅ System health: $health_score% - Excellent"
elif [ $health_score -ge 60 ]; then
    print_status $YELLOW "⚠️ System health: $health_score% - Good"
else
    print_status $RED "❌ System health: $health_score% - Poor"
fi

# Recommendations
echo ""
print_status $BLUE "💡 Recommendations"

if [ $health_score -ge 80 ]; then
    if [ $network_ok -ge 3 ]; then
        echo "🚀 Use: Stable APK Builder (recommended)"
        echo "   - Full-featured build with all optimizations"
        echo "   - Command: Use GitHub Actions workflow"
    else
        echo "🔧 Use: Ultra-Stable APK Builder"
        echo "   - Better network error handling"
        echo "   - Command: Use GitHub Actions workflow"
    fi
elif [ $health_score -ge 50 ]; then
    if [ $network_ok -ge 2 ]; then
        echo "🔧 Use: Ultra-Stable APK Builder"
        echo "   - Conservative build with extensive diagnostics"
        echo "   - Command: Use GitHub Actions workflow"
    else
        echo "🌐 Use: Offline APK Builder"
        echo "   - Build with cached dependencies"
        echo "   - Command: Use GitHub Actions workflow"
    fi
else
    echo "🔄 Use: Fallback APK Builder"
    echo "   - Minimal requirements, maximum compatibility"
    echo "   - Command: Use GitHub Actions workflow"
fi

# Local build options
echo ""
if $java_ok && $gradle_ok && $build_files_ok; then
    echo "🏠 Local Build Options:"
    if [ $network_ok -ge 2 ]; then
        echo "   ./build-apk.sh both    # Recommended"
    else
        echo "   ./gradlew --offline assembleDebug   # If cache available"
    fi
    echo "   ./gradlew clean assembleDebug       # Debug only"
    echo "   ./gradlew clean assembleRelease     # Release only"
fi

# Quick fixes
if [ $health_score -lt 80 ]; then
    echo ""
    print_status $BLUE "🔧 Quick Fixes"
    
    if ! $java_ok; then
        echo "• Install Java 17: apt-get install openjdk-17-jdk"
    fi
    
    if ! $gradle_ok && [ -f "./gradlew" ]; then
        echo "• Make gradlew executable: chmod +x gradlew"
    fi
    
    if [ $network_ok -eq 0 ]; then
        echo "• Check internet connection and firewall settings"
        echo "• Consider using Offline APK Builder for cached builds"
    fi
    
    if ! $build_files_ok; then
        echo "• Ensure you're in the project root directory"
        echo "• Check if build files were committed to git"
    fi
fi

echo ""
print_status $GREEN "🎉 Status check complete!"

# Exit with appropriate code
if [ $health_score -ge 50 ]; then
    exit 0
else
    exit 1
fi