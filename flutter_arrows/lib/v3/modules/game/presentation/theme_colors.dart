import 'package:flutter/material.dart';

class ThemeColors {
  final Color background;
  final Color accent;
  final Color snake;
  final Color topBarButton;
  final Color bottomBar;
  final Color flashingRed;
  final Color lightGray;

  const ThemeColors({
    required this.background,
    required this.accent,
    required this.snake,
    required this.topBarButton,
    required this.bottomBar,
    required this.flashingRed,
    required this.lightGray,
  });

  static const ThemeColors defaultTheme = ThemeColors(
    background: Color(0xFF1E281F),
    accent: Colors.orange,
    snake: Color(0xFFC57A18), // Orange
    topBarButton: Color(0xFF323B33),
    bottomBar: Color(0xFF141F16),
    flashingRed: Color(0xFFFF4500),
    lightGray: Color(0xFF2C352D),
  );

  static const ThemeColors neonCyberpunk = ThemeColors(
    background: Color(0xFF0F0F1B),
    accent: Color(0xFF00FFFF), // Cyan
    snake: Color(0xFFFF00FF), // Magenta
    topBarButton: Color(0xFF231B3A),
    bottomBar: Color(0xFF070211),
    flashingRed: Color(0xFFFF3333),
    lightGray: Color(0xFF2E2245),
  );

  static const ThemeColors candyJelly = ThemeColors(
    background: Color(0xFFFFF2F5),
    accent: Color(0xFFFF69B4), // Hot Pink
    snake: Color(0xFF5DBBDB), // Jelly Blue
    topBarButton: Color(0xFFFFE0E9),
    bottomBar: Color(0xFFFDEAF0),
    flashingRed: Color(0xFFFF4D4D),
    lightGray: Color(0xFFFFC2D6),
  );

  static const List<ThemeColors> allThemes = [defaultTheme, neonCyberpunk, candyJelly];
}
