# GitHub Actions Workflows

This directory contains the GitHub Actions workflows for the Spade Ace project.

## Available Workflows

### 🧪 Android CI (`android-ci.yml`)
- **Purpose**: Continuous integration for code quality
- **Triggers**: Push to main/develop, Pull requests to main
- **Actions**: Run tests, lint checks
- **Duration**: ~2-3 minutes

### 🏗️ Build APK (`build-apk.yml`)
- **Purpose**: Build Android APK files
- **Triggers**: Manual dispatch, version tags (v*)
- **Options**: Debug, Release build types
- **Artifacts**: APK files uploaded to GitHub Actions
- **Duration**: ~5-10 minutes

### 🚀 Release (`release.yml`)
- **Purpose**: Create GitHub releases with APK
- **Triggers**: Version tags (v*)  
- **Actions**: Build release APK, create GitHub release, attach APK
- **Duration**: ~5-10 minutes

## How to Use

### Running Manual Builds
1. Go to Actions tab in GitHub
2. Select "Build APK" workflow
3. Click "Run workflow"
4. Choose build type (debug/release)
5. Click "Run workflow"

### Creating Releases
1. Create and push a version tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. The Release workflow will automatically trigger
3. Check the Releases page for the generated release

## Build Requirements
- Java 17
- Android SDK
- Gradle wrapper (included)
- Network connectivity for dependencies

## Troubleshooting
- Check workflow logs in the Actions tab
- Ensure all source files are committed
- Verify gradle.properties settings
- For local builds, use: `./gradlew assembleDebug`