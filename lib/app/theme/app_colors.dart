import 'package:flutter/material.dart';

abstract class AppColors {
  // Light Mode
  static const lightBackground = Color(0xFFFFFFFF);
  static const lightSurface = Color(0xFFF7F8FA);
  static const lightTextPrimary = Color(0xFF1A1A1A);
  static const lightTextSecondary = Color(0xFF6B7280);
  static const lightBorder = Color(0xFFE5E7EB);
  static const lightAccent = Color(0xFF6B7280);

  // Dark Mode
  static const darkBackground = Color(0xFF121212);
  static const darkSurface = Color(0xFF1E1E1E);
  static const darkTextPrimary = Color(0xFFEDEDED);
  static const darkTextSecondary = Color(0xFF9CA3AF);
  static const darkBorder = Color(0xFF2D2D2D);
  static const darkAccent = Color(0xFF6B7280);

  // Priority Indicators
  static const priorityNormal = Color(0xFF9CA3AF);
  static const priorityEmail = Color(0xFFF5B94C);
  static const priorityAlarm = Color(0xFFEF6B6B);
}
