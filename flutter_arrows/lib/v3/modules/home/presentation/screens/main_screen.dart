import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class MainScreen extends StatelessWidget {
  final StatefulNavigationShell navigationShell;

  const MainScreen({super.key, required this.navigationShell});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          // --- PREMIUM MESH GRADIENT BACKGROUND ---
          Positioned.fill(
            child: Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    Color(0xFF0F170F), // Deep emerald black
                    Color(0xFF1B2E1B), // Dark forest
                    Color(0xFF0A0F0A),
                  ],
                ),
              ),
            ),
          ),
          
          // Subtle accent glow
          Positioned(
            top: -100,
            right: -50,
            child: Container(
              width: 300,
              height: 300,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: const Color(0xFF4CAF50).withOpacity(0.05),
              ),
            ),
          ),

          // Main view
          navigationShell,

          // --- FIXED FLOATING PREMIUM NAV BAR ---
          Positioned(
            left: 20,
            right: 20,
            bottom: 24,
            child: _buildGlassNavigationBar(context),
          ),
        ],
      ),
    );
  }

  Widget _buildGlassNavigationBar(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(35),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 15, sigmaY: 15),
        child: Container(
          height: 70,
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.05),
            borderRadius: BorderRadius.circular(35),
            border: Border.all(color: Colors.white.withOpacity(0.1)),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.2),
                blurRadius: 10,
                offset: const Offset(0, 5),
              ),
            ],
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              _buildNavItem(context, 0, Icons.grid_view_rounded, 'Generator'),
              _buildMainNavItem(context, 1, Icons.play_arrow_rounded),
              _buildNavItem(context, 2, Icons.settings_rounded, 'Settings'),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem(BuildContext context, int index, IconData icon, String label) {
    final isSelected = navigationShell.currentIndex == index;
    final color = isSelected ? const Color(0xFF4CAF50) : Colors.white38;

    return GestureDetector(
      onTap: () => navigationShell.goBranch(index, initialLocation: index == navigationShell.currentIndex),
      behavior: HitTestBehavior.opaque,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: color, size: 26),
          const SizedBox(height: 2),
          Text(
            label,
            style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.bold, letterSpacing: 0.5),
          ),
        ],
      ),
    );
  }

  Widget _buildMainNavItem(BuildContext context, int index, IconData icon) {
    final isSelected = navigationShell.currentIndex == index;
    
    return GestureDetector(
      onTap: () => navigationShell.goBranch(index, initialLocation: index == navigationShell.currentIndex),
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF81C784), Color(0xFF43A047)],
          ),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF43A047).withOpacity(0.4),
              blurRadius: 15,
              spreadRadius: 2,
            ),
          ],
        ),
        child: Icon(icon, color: Colors.white, size: 32),
      ),
    );
  }
}
