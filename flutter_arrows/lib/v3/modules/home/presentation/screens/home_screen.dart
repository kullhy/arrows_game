import 'package:flutter/material.dart';
import 'package:flutter_arrows/dependency_injection.dart';
import 'package:flutter_arrows/v3/modules/game/application/services/game_preferences.dart';
import 'package:flutter_arrows/v3/modules/game/presentation/bloc/game_cubit.dart';
import 'package:flutter_arrows/v3/modules/game/presentation/bloc/game_state.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final prefs = sl<GamePreferences>();

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(vertical: 40),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // --- PREMIUM LOGO ---
              _buildModernLogo(),
              const SizedBox(height: 30),

              // --- COIN & CURRENCY HUB ---
              _buildCurrencyHub(prefs.coins),
              const SizedBox(height: 50),

              // --- LEVEL DASHBOARD (GLASS CARD) ---
              _buildLevelDashboard(prefs),
              const SizedBox(height: 50),

              // --- MAIN CTA: 3D PLAY BUTTON ---
              _buildBigPlayButton(context),
              const SizedBox(height: 30),

              // --- SECONDARY ACTION TILES ---
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 40),
                child: Row(
                  children: [
                    Expanded(
                      child: _buildGlassActionTile(
                        'DAILY',
                        Icons.calendar_today_rounded,
                        const Color(0xFFFF5252),
                        () {
                          sl<GameCubit>().generateDailyChallenge();
                          context.push('/game');
                        },
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: _buildGlassActionTile(
                        'LEVELS',
                        Icons.map_rounded,
                        const Color(0xFF2196F3),
                        () => context.push('/home/levels'),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 100), // Nav bar spacer
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildModernLogo() {
    return Column(
      children: [
        Stack(
          alignment: Alignment.center,
          children: [
            Container(
              width: 100,
              height: 100,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [const Color(0xFF4CAF50).withOpacity(0.4), Colors.transparent],
                ),
              ),
            ),
            const Text(
              '▲',
              style: TextStyle(
                fontSize: 70,
                color: Colors.white,
                shadows: [
                  Shadow(color: Colors.black45, offset: Offset(0, 5), blurRadius: 10),
                ],
              ),
            ),
          ],
        ),
        const Text(
          'ARROWS',
          style: TextStyle(
            fontSize: 42,
            fontWeight: FontWeight.w900,
            color: Colors.white,
            letterSpacing: 2,
            shadows: [
              Shadow(color: Colors.black26, offset: Offset(0, 4), blurRadius: 8),
            ],
          ),
        ),
        Container(
          height: 3,
          width: 50,
          decoration: BoxDecoration(
            color: const Color(0xFF4CAF50),
            borderRadius: BorderRadius.circular(2),
          ),
        )
      ],
    );
  }

  Widget _buildCurrencyHub(int coins) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.05),
        borderRadius: BorderRadius.circular(25),
        border: Border.all(color: Colors.white.withOpacity(0.1)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.stars_rounded, color: Colors.amber, size: 22),
          const SizedBox(width: 8),
          Text(
            '$coins',
            style: const TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.w900,
              fontSize: 18,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLevelDashboard(GamePreferences prefs) {
    return Container(
      width: 240,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.05),
        borderRadius: BorderRadius.circular(30),
        border: Border.all(color: Colors.white.withOpacity(0.1)),
        boxShadow: [
          BoxShadow(color: Colors.black26, blurRadius: 20, offset: Offset(0, 10))
        ],
      ),
      child: Column(
        children: [
          Text(
            'CHAPTER 1',
            style: TextStyle(
              color: Colors.white.withOpacity(0.4),
              fontSize: 12,
              fontWeight: FontWeight.bold,
              letterSpacing: 1.5,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'LEVEL ${prefs.currentLevel}',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 32,
              fontWeight: FontWeight.w900,
            ),
          ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: List.generate(3, (i) {
              final stars = prefs.getStarsForLevel(prefs.currentLevel);
              return Icon(
                Icons.star_rounded, 
                color: i < stars ? Colors.amber : Colors.white10, 
                size: 24
              );
            }),
          ),
        ],
      ),
    );
  }

  Widget _buildBigPlayButton(BuildContext context) {
    return GestureDetector(
      onTap: () => context.push('/game'),
      child: Container(
        width: 140,
        height: 140,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFF81C784), Color(0xFF2E7D32)],
          ),
          boxShadow: [
            BoxShadow(
              color: const Color(0xFF1B5E20).withOpacity(0.8),
              offset: const Offset(0, 8),
              blurRadius: 0,
            ),
            BoxShadow(
              color: const Color(0xFF43A047).withOpacity(0.4),
              offset: const Offset(0, 15),
              blurRadius: 25,
            ),
          ],
        ),
        child: const Icon(
          Icons.play_arrow_rounded,
          color: Colors.white,
          size: 80,
        ),
      ),
    );
  }

  Widget _buildGlassActionTile(String label, IconData icon, Color color, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(25),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 20),
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.05),
          borderRadius: BorderRadius.circular(25),
          border: Border.all(color: Colors.white.withOpacity(0.05)),
        ),
        child: Column(
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: color.withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: Icon(icon, color: color, size: 28),
            ),
            const SizedBox(height: 10),
            Text(
              label,
              style: const TextStyle(
                color: Colors.white70,
                fontSize: 10,
                fontWeight: FontWeight.w900,
                letterSpacing: 1,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
