# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Kotlin metadata for stability
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Keep BouncyCastle crypto classes - critical for app functionality
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Keep coroutines for async operations
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.** { *; }

# Keep Jetpack Compose classes for UI stability
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep app-specific model classes to prevent serialization issues
-keep class com.binah.spadeace.data.** { *; }
-keepclassmembers class com.binah.spadeace.data.** {
    <fields>;
    <methods>;
}

# Keep ViewModels
-keep class androidx.lifecycle.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Generic keep rules for reflection
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Keep enum classes stable
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Commons Codec for encoding/decoding
-keep class org.apache.commons.codec.** { *; }
-dontwarn org.apache.commons.codec.**

# Prevent obfuscation of critical crypto operations
-keepclassmembers class com.binah.spadeace.core.** {
    public <methods>;
}

# Aggressive optimization settings but with safety
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove debugging information in release builds but keep crash info
-keepattributes LineNumberTable
-renamesourcefileattribute SourceFile