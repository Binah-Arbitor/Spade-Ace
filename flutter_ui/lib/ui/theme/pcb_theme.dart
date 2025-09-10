import 'package:flutter/material.dart';

/// PCB/Cyber-tech color palette and theme configuration
class PCBColors {
  // Primary colors
  static const Color deepOffBlack = Color(0xFF0A0A0A);
  static const Color primaryTeal = Color(0xFF00FFFF);
  static const Color primaryCyan = Color(0xFF00BFFF);
  static const Color limeGreen = Color(0xFF32FF32);
  
  // Text colors
  static const Color lightGray = Color(0xFFE0E0E0);
  static const Color white = Color(0xFFFFFFFF);
  
  // UI element colors
  static const Color cardBackground = Color(0xFF1A1A1A);
  static const Color borderColor = Color(0xFF333333);
  static const Color glowTeal = Color(0xFF00FFFF);
  
  // Status colors
  static const Color successGreen = Color(0xFF32FF32);
  static const Color warningYellow = Color(0xFFFFD700);
  static const Color errorRed = Color(0xFFFF3333);
  static const Color infoBlue = Color(0xFF00BFFF);
}

class PCBTheme {
  static ThemeData get darkTheme {
    return ThemeData(
      brightness: Brightness.dark,
      primaryColor: PCBColors.primaryTeal,
      scaffoldBackgroundColor: PCBColors.deepOffBlack,
      colorScheme: const ColorScheme.dark(
        primary: PCBColors.primaryTeal,
        secondary: PCBColors.limeGreen,
        surface: PCBColors.cardBackground,
        background: PCBColors.deepOffBlack,
        onPrimary: PCBColors.deepOffBlack,
        onSecondary: PCBColors.deepOffBlack,
        onSurface: PCBColors.lightGray,
        onBackground: PCBColors.lightGray,
      ),
      
      // Card theme
      cardTheme: CardTheme(
        color: PCBColors.cardBackground,
        elevation: 4,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
          side: const BorderSide(
            color: PCBColors.borderColor,
            width: 1,
          ),
        ),
      ),
      
      // Button themes
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: PCBColors.primaryTeal,
          foregroundColor: PCBColors.deepOffBlack,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(4),
          ),
          elevation: 2,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
        ),
      ),
      
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: PCBColors.primaryTeal,
          side: const BorderSide(color: PCBColors.primaryTeal),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(4),
          ),
        ),
      ),
      
      // Text themes
      textTheme: const TextTheme(
        headlineLarge: TextStyle(
          fontFamily: 'FiraCode',
          color: PCBColors.white,
          fontSize: 32,
          fontWeight: FontWeight.bold,
        ),
        headlineMedium: TextStyle(
          fontFamily: 'FiraCode',
          color: PCBColors.white,
          fontSize: 24,
          fontWeight: FontWeight.bold,
        ),
        bodyLarge: TextStyle(
          fontFamily: 'FiraCode',
          color: PCBColors.lightGray,
          fontSize: 16,
        ),
        bodyMedium: TextStyle(
          fontFamily: 'FiraCode',
          color: PCBColors.lightGray,
          fontSize: 14,
        ),
        labelLarge: TextStyle(
          fontFamily: 'IBMPlexMono',
          color: PCBColors.lightGray,
          fontSize: 12,
          fontWeight: FontWeight.w500,
        ),
      ),
      
      // Input decoration theme
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: PCBColors.cardBackground,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(4),
          borderSide: const BorderSide(color: PCBColors.borderColor),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(4),
          borderSide: const BorderSide(color: PCBColors.borderColor),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(4),
          borderSide: const BorderSide(color: PCBColors.primaryTeal),
        ),
        labelStyle: const TextStyle(color: PCBColors.lightGray),
        hintStyle: TextStyle(color: PCBColors.lightGray.withOpacity(0.7)),
      ),
      
      // Progress indicator theme
      progressIndicatorTheme: const ProgressIndicatorThemeData(
        color: PCBColors.primaryTeal,
        linearTrackColor: PCBColors.borderColor,
      ),
      
      // Expansion tile theme
      expansionTileTheme: const ExpansionTileThemeData(
        iconColor: PCBColors.primaryTeal,
        collapsedIconColor: PCBColors.lightGray,
        textColor: PCBColors.white,
        collapsedTextColor: PCBColors.lightGray,
      ),
      
      // Dropdown theme
      dropdownMenuTheme: DropdownMenuThemeData(
        menuStyle: MenuStyle(
          backgroundColor: MaterialStateProperty.all(PCBColors.cardBackground),
          side: MaterialStateProperty.all(
            const BorderSide(color: PCBColors.borderColor),
          ),
        ),
        textStyle: const TextStyle(
          fontFamily: 'FiraCode',
          color: PCBColors.lightGray,
        ),
      ),
    );
  }
  
  // Glow effect for active elements
  static BoxDecoration get glowDecoration {
    return BoxDecoration(
      borderRadius: BorderRadius.circular(4),
      boxShadow: [
        BoxShadow(
          color: PCBColors.primaryTeal.withOpacity(0.3),
          blurRadius: 8,
          spreadRadius: 2,
        ),
      ],
    );
  }
  
  // PCB circuit pattern decoration
  static BoxDecoration get circuitDecoration {
    return BoxDecoration(
      border: Border.all(
        color: PCBColors.borderColor,
        width: 1,
      ),
      borderRadius: BorderRadius.circular(4),
      gradient: LinearGradient(
        begin: Alignment.topLeft,
        end: Alignment.bottomRight,
        colors: [
          PCBColors.cardBackground,
          PCBColors.cardBackground.withOpacity(0.8),
        ],
      ),
    );
  }
}