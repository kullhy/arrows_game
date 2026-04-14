import 'package:flutter/material.dart';
import 'package:flutter_arrows/dependency_injection.dart';
import 'package:flutter_arrows/v3/modules/game/application/services/game_preferences.dart';
import 'package:flutter_arrows/v3/modules/game/presentation/bloc/game_cubit.dart';
import 'package:go_router/go_router.dart';

class LevelSelectScreen extends StatelessWidget {
  const LevelSelectScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final prefs = sl<GamePreferences>();
    final currentLevel = prefs.currentLevel;

    return Scaffold(
      backgroundColor: Colors.transparent, // Background handled by stack
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        title: const Text('WORLD MAP', style: TextStyle(fontWeight: FontWeight.w900, letterSpacing: 2)),
        centerTitle: true,
        backgroundColor: Colors.transparent,
        elevation: 0,
        foregroundColor: Colors.white,
      ),
      body: Stack(
        children: [
          // Background decoration
          Positioned.fill(
            child: Opacity(
              opacity: 0.3,
              child: CustomPaint(painter: _BackgroundPatternPainter()),
            ),
          ),
          GridView.builder(
            padding: const EdgeInsets.fromLTRB(24, 120, 24, 120),
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 3,
              crossAxisSpacing: 20,
              mainAxisSpacing: 20,
              childAspectRatio: 0.85,
            ),
            itemCount: 150,
            itemBuilder: (context, index) {
              final levelNum = index + 1;
              final isUnlocked = levelNum <= currentLevel;
              final isCurrent = levelNum == currentLevel;
              final stars = prefs.getStarsForLevel(levelNum);

              return _buildLevelJewel(context, levelNum, isUnlocked, isCurrent, stars);
            },
          ),
        ],
      ),
    );
  }

  Widget _buildLevelJewel(BuildContext context, int num, bool isUnlocked, bool isCurrent, int stars) {
    final color = isUnlocked 
        ? (isCurrent ? const Color(0xFF4CAF50) : const Color(0xFF81C784))
        : Colors.white12;

    return GestureDetector(
      onTap: isUnlocked ? () {
        sl<GameCubit>().jumpToLevel(num); 
        context.push('/game');
      } : null,
      child: Column(
        children: [
          Expanded(
            child: Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: isUnlocked ? [
                    color.withOpacity(1.0),
                    color.withOpacity(0.7),
                  ] : [Colors.white10, Colors.white10],
                ),
                borderRadius: BorderRadius.circular(20),
                boxShadow: isUnlocked ? [
                  BoxShadow(
                    color: color.withOpacity(0.5),
                    offset: const Offset(0, 6),
                    blurRadius: 0,
                  ),
                  if (isCurrent)
                    BoxShadow(
                      color: color.withOpacity(0.4),
                      blurRadius: 15,
                      spreadRadius: 2,
                    ),
                ] : [],
              ),
              child: Stack(
                alignment: Alignment.center,
                children: [
                   if (!isUnlocked)
                     const Icon(Icons.lock_rounded, size: 24, color: Colors.white24)
                   else
                     Text(
                       '$num',
                       style: const TextStyle(
                         color: Colors.white,
                         fontWeight: FontWeight.w900,
                         fontSize: 24,
                         shadows: [Shadow(color: Colors.black26, offset: Offset(0, 2), blurRadius: 4)],
                       ),
                     ),
                   
                   // Glossy highlight
                   Positioned(
                     top: 4,
                     left: 10,
                     child: Container(
                       width: 30,
                       height: 10,
                       decoration: BoxDecoration(
                         color: Colors.white24,
                         borderRadius: BorderRadius.circular(5),
                       ),
                     ),
                   ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 8),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: List.generate(3, (i) => Icon(
              Icons.star_rounded,
              size: 14,
              color: i < stars ? Colors.amber : Colors.white10,
            )),
          ),
        ],
      ),
    );
  }
}

class _BackgroundPatternPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = Colors.white.withOpacity(0.1)
      ..strokeWidth = 1.0;
    
    const spacing = 40.0;
    for (double i = 0; i < size.width + size.height; i += spacing) {
      canvas.drawLine(Offset(i, 0), Offset(0, i), paint);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
