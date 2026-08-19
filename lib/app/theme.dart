import 'package:flutter/material.dart';

class AppColors {
  static const Color carbon = Color(0xFF141519);
  static const Color raise = Color(0xFF1D1F26);
  static const Color line = Color(0xFF2A2C35);
  static const Color paper = Color(0xFFF1EADA);
  static const Color ink = Color(0xFF292318);
  static const Color inkSoft = Color(0xFF6E6454);
  static const Color signal = Color(0xFFE8A33D);
  static const Color muted = Color(0xFF8A877D);
  static const Color danger = Color(0xFFC85C4D);

  static const Color primary = signal;
  static const Color primaryContainer = raise;
  static const Color secondary = muted;
  static const Color surface = carbon;
  static const Color surfaceVariant = raise;
  static const Color background = carbon;
  static const Color error = danger;
  static const Color textPrimary = paper;
  static const Color textSecondary = muted;
  static const Color textMuted = Color(0xFF6E6B63);
  static const Color divider = line;
  static const Color pinned = signal;
  static const Color archived = muted;
}

class AppFonts {
  static const String mono = 'SpaceMono';
  static const String body = 'SpaceGrotesk';
}

class AppStrings {
  static const String appName = '.notes';
  static const String emptyStateTitle = 'No notes yet';
  static const String emptyStateSubtitle = 'Tap the blank page to start';
  static const String searchPlaceholder = 'Search notes';
  static const String confirmDeleteTitle = 'Delete Note';
  static const String confirmDeleteMessage = 'Are you sure you want to delete this note?';
  static const String deleteButton = 'Delete';
  static const String cancelButton = 'Cancel';
  static const String saveButton = 'Save';
  static const String noteTitleHint = 'Title';
  static const String noteContentHint = 'Start typing';
  static const String categoryHint = 'Category';
  static const String categoryEmpty = 'No Category';
}

class AppTheme {
  static const ColorScheme _scheme = ColorScheme.dark(
    primary: AppColors.signal,
    onPrimary: AppColors.ink,
    primaryContainer: AppColors.raise,
    onPrimaryContainer: AppColors.paper,
    secondary: AppColors.muted,
    onSecondary: AppColors.carbon,
    secondaryContainer: AppColors.raise,
    onSecondaryContainer: AppColors.paper,
    surface: AppColors.carbon,
    onSurface: AppColors.paper,
    surfaceContainerHighest: AppColors.raise,
    onSurfaceVariant: AppColors.muted,
    error: AppColors.danger,
    onError: AppColors.paper,
    outline: AppColors.line,
    outlineVariant: AppColors.line,
  );

  static ThemeData get dark {
    const scheme = _scheme;
    final base = ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: scheme,
      fontFamily: AppFonts.body,
    );

    return base.copyWith(
      scaffoldBackgroundColor: AppColors.carbon,
      textTheme: base.textTheme.copyWith(
        displaySmall: const TextStyle(
          fontFamily: AppFonts.mono,
          fontSize: 28,
          fontWeight: FontWeight.w700,
          height: 1.1,
          color: AppColors.paper,
        ),
        headlineMedium: const TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 26,
          fontWeight: FontWeight.w600,
          height: 1.15,
          color: AppColors.paper,
        ),
        titleLarge: const TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 20,
          fontWeight: FontWeight.w600,
          height: 1.25,
          color: AppColors.paper,
        ),
        titleMedium: const TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 16,
          fontWeight: FontWeight.w600,
          height: 1.3,
          color: AppColors.paper,
        ),
        titleSmall: const TextStyle(
          fontFamily: AppFonts.mono,
          fontSize: 13,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.5,
          color: AppColors.paper,
        ),
        bodyLarge: const TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 16,
          fontWeight: FontWeight.w400,
          height: 1.5,
          color: AppColors.paper,
        ),
        bodyMedium: const TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 14,
          fontWeight: FontWeight.w400,
          height: 1.45,
          color: AppColors.paper,
        ),
        bodySmall: const TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 13,
          fontWeight: FontWeight.w400,
          height: 1.4,
          color: AppColors.muted,
        ),
        labelLarge: const TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 14,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.2,
          color: AppColors.paper,
        ),
        labelMedium: const TextStyle(
          fontFamily: AppFonts.mono,
          fontSize: 12,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.5,
          color: AppColors.paper,
        ),
        labelSmall: const TextStyle(
          fontFamily: AppFonts.mono,
          fontSize: 11,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.6,
          color: AppColors.muted,
        ),
      ),
      appBarTheme: const AppBarTheme(
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: false,
        backgroundColor: AppColors.carbon,
        foregroundColor: AppColors.paper,
        titleTextStyle: TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 20,
          fontWeight: FontWeight.w600,
          color: AppColors.paper,
        ),
      ),
      cardTheme: const CardThemeData(
        elevation: 0,
        color: AppColors.paper,
        shadowColor: Colors.transparent,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.all(Radius.circular(14)),
        ),
      ),
      dialogTheme: DialogThemeData(
        backgroundColor: AppColors.raise,
        surfaceTintColor: Colors.transparent,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
        titleTextStyle: const TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 18,
          fontWeight: FontWeight.w600,
          color: AppColors.paper,
        ),
        contentTextStyle: const TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 14,
          height: 1.4,
          color: AppColors.muted,
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.raise,
        hintStyle: const TextStyle(
          fontFamily: AppFonts.mono,
          fontSize: 14,
          color: AppColors.muted,
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Colors.transparent),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Colors.transparent),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: AppColors.signal, width: 1.5),
        ),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: AppColors.paper,
        foregroundColor: AppColors.ink,
        elevation: 4,
        focusElevation: 4,
        hoverElevation: 6,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.all(Radius.circular(14)),
        ),
      ),
      switchTheme: SwitchThemeData(
        thumbColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return AppColors.paper;
          }
          return AppColors.muted;
        }),
        trackColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return AppColors.signal;
          }
          return AppColors.line;
        }),
        trackOutlineColor: const WidgetStatePropertyAll(Colors.transparent),
      ),
      checkboxTheme: CheckboxThemeData(
        fillColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return AppColors.signal;
          }
          return Colors.transparent;
        }),
        checkColor: const WidgetStatePropertyAll(AppColors.ink),
        side: const BorderSide(color: AppColors.muted, width: 1.5),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(4)),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: AppColors.raise,
        selectedColor: AppColors.signal,
        side: const BorderSide(color: AppColors.line),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(999)),
        labelStyle: const TextStyle(
          fontFamily: AppFonts.mono,
          fontSize: 12,
          fontWeight: FontWeight.w700,
          color: AppColors.muted,
        ),
        secondaryLabelStyle: const TextStyle(
          fontFamily: AppFonts.mono,
          fontSize: 12,
          fontWeight: FontWeight.w700,
          color: AppColors.ink,
        ),
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      ),
      tabBarTheme: const TabBarThemeData(
        labelColor: AppColors.signal,
        unselectedLabelColor: AppColors.muted,
        indicatorColor: AppColors.signal,
        indicatorSize: TabBarIndicatorSize.label,
        dividerColor: Colors.transparent,
        labelStyle: TextStyle(
          fontFamily: AppFonts.mono,
          fontSize: 13,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.5,
        ),
        unselectedLabelStyle: TextStyle(
          fontFamily: AppFonts.mono,
          fontSize: 13,
          fontWeight: FontWeight.w700,
          letterSpacing: 0.5,
        ),
      ),
      snackBarTheme: SnackBarThemeData(
        backgroundColor: AppColors.raise,
        contentTextStyle: const TextStyle(
          fontFamily: AppFonts.body,
          fontSize: 14,
          color: AppColors.paper,
        ),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
          side: const BorderSide(color: AppColors.line),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: AppColors.signal,
          foregroundColor: AppColors.ink,
          textStyle: const TextStyle(
            fontFamily: AppFonts.body,
            fontSize: 14,
            fontWeight: FontWeight.w700,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.signal,
          foregroundColor: AppColors.ink,
          elevation: 0,
          textStyle: const TextStyle(
            fontFamily: AppFonts.body,
            fontSize: 14,
            fontWeight: FontWeight.w700,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.signal,
          side: const BorderSide(color: AppColors.line, width: 1.5),
          textStyle: const TextStyle(
            fontFamily: AppFonts.body,
            fontSize: 14,
            fontWeight: FontWeight.w600,
          ),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: AppColors.signal,
          textStyle: const TextStyle(
            fontFamily: AppFonts.body,
            fontSize: 14,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      listTileTheme: const ListTileThemeData(
        textColor: AppColors.paper,
        iconColor: AppColors.muted,
      ),
    );
  }
}