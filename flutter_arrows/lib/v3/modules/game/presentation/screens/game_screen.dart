import 'dart:math';
import 'dart:ui';
import 'package:confetti/confetti.dart';
import 'package:flutter/material.dart';
import 'package:flutter_arrows/dependency_injection.dart';
import 'package:flutter_arrows/v3/modules/game/application/models/snake.dart';
import 'package:flutter_arrows/v3/modules/game/application/services/game_preferences.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import '../bloc/game_cubit.dart';
import '../bloc/game_state.dart';
import '../widgets/arrows_board_widget.dart';
import '../theme_colors.dart';

class GameScreen extends StatefulWidget {
  const GameScreen({super.key});

  @override
  State<GameScreen> createState() => _GameScreenState();
}

class _GameScreenState extends State<GameScreen> with SingleTickerProviderStateMixin {
  late ConfettiController _confettiController;
  late AnimationController _shakeController;
  late Animation<double> _shakeAnimation;
  
  // Track level intro visibility
  int? _lastProcessedLevelId;
  bool _introDismissed = false;

  @override
  void initState() {
    super.initState();
    _confettiController = ConfettiController(duration: const Duration(seconds: 3));
    _shakeController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 300),
    );
    _shakeAnimation = Tween<double>(begin: 0, end: 8)
        .chain(
          CurveTween(curve: Curves.elasticIn),
        )
        .animate(_shakeController);
  }

  @override
  void dispose() {
    _confettiController.dispose();
    _shakeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider.value(
      value: sl<GameCubit>(),
      child: BlocConsumer<GameCubit, GameState>(
        listener: (blocContext, state) {
          if (state is GameWon) {
            _confettiController.play();
          }
          if (state is GamePlaying) {
            if (state.cameraShake) {
              _shakeController.forward(from: 0);
            }
            // Logic to show intro overlay for new levels
            if (_lastProcessedLevelId != state.level.id) {
              _lastProcessedLevelId = state.level.id;
              _introDismissed = false;
            }
          }
        },
        builder: (blocContext, state) {
          int themeIdx = 0;
          if (state is GamePlaying) themeIdx = state.themeIndex;
          final currentTheme = ThemeColors.allThemes[themeIdx];

          final prefs = sl<GamePreferences>();
          final hasBomb = state is GamePlaying && state.level.snakes.any((s) => s.type == SnakeType.bomb);
          final hasLock = state is GamePlaying && state.level.snakes.any((s) => s.type == SnakeType.locked);
          
          final showBombIntro = hasBomb && !prefs.hasSeenBombIntro;
          final showLockIntro = hasLock && !prefs.hasSeenLockIntro;

          final isPlaying = state is GamePlaying;
          final isShaking = isPlaying ? (state as GamePlaying).cameraShake : false;
          
          final showIntro = !isShaking && (showBombIntro || showLockIntro) && !_introDismissed;

          return AnimatedBuilder(
            animation: _shakeAnimation,
            builder: (animContext, child) {
              final shakeVal = _shakeAnimation.value;
              final offsetX = shakeVal * sin(_shakeController.value * pi * 6);
              return Transform.translate(
                offset: Offset(offsetX, 0),
                child: child,
              );
            },
            child: Scaffold(
              body: Stack(
                children: [
                  // --- PREMIUM BACKGROUND ---
                  Container(
                    decoration: const BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                        colors: [Color(0xFF0F170F), Color(0xFF1B2E1B)],
                      ),
                    ),
                  ),

                  // --- MAIN CONTENT ---
                  SafeArea(
                    child: Column(
                      children: [
                        _buildPremiumTopBar(blocContext, state, currentTheme),
                        Expanded(
                          child: Padding(
                            padding: const EdgeInsets.only(bottom: 100),
                            child: Stack(
                              children: [
                                InteractiveViewer(
                                  boundaryMargin: const EdgeInsets.all(300),
                                  minScale: 0.1,
                                  maxScale: 10.0,
                                  child: const Center(
                                    child: Padding(
                                      padding: EdgeInsets.all(40.0),
                                      child: ArrowsBoardWidget(),
                                    ),
                                  ),
                                ),
                                
                                // Guidance toggle FAB (Right)
                                Positioned(
                                  right: 24,
                                  bottom: 24,
                                  child: _buildFab(
                                    icon: Icons.grid_3x3_rounded,
                                    isActive: state is GamePlaying && state.guidanceAlpha > 0,
                                    onTap: () => blocContext.read<GameCubit>().toggleGuidance(),
                                    theme: currentTheme,
                                  ),
                                ),
                                
                                // Undo FAB (Left)
                                Positioned(
                                  left: 24,
                                  bottom: 24,
                                  child: _buildFab(
                                    icon: Icons.undo_rounded,
                                    onTap: () => blocContext.read<GameCubit>().undo(),
                                    theme: currentTheme,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),

                  // === OVERLAYS ===
                  if (state is GameLoading) _buildLoadingOverlay(currentTheme, state.progress),
                  if (state is GameWon) _buildWinOverlay(currentTheme, state.score, blocContext),
                  if (state is GameOver) ...[
                    _buildGameOverOverlay(currentTheme, state.score, blocContext),
                    if (state.isBombExplosion) _buildExplosionFlash(state),
                  ],
                  
                  // NEW: Level Intro/Tutorial Overlay
                  if (showIntro) _buildLevelIntroOverlay(
                    state.level.snakes, 
                    () {
                      if (hasBomb) prefs.hasSeenBombIntro = true;
                      if (hasLock) prefs.hasSeenLockIntro = true;
                      setState(() => _introDismissed = true);
                    }
                  ),

                  // === CONFETTI ===
                  Align(
                    alignment: Alignment.topCenter,
                    child: ConfettiWidget(
                      confettiController: _confettiController,
                      blastDirectionality: BlastDirectionality.explosive,
                      colors: const [Colors.amber, Colors.green, Colors.white, Colors.orange],
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildPremiumTopBar(BuildContext context, GameState state, ThemeColors theme) {
    return Container(
      margin: const EdgeInsets.fromLTRB(16, 8, 16, 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.08),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.white12),
      ),
      child: Column(
        children: [
          Row(
            children: [
              _barButton(Icons.arrow_back_ios_new_rounded, () => context.pop()),
              const SizedBox(width: 8),
              _barButton(Icons.refresh_rounded, () => context.read<GameCubit>().restartLevel()),
              const Spacer(),
              if (state is GamePlaying) _buildLives(state.lives, state.maxLives),
              const Spacer(),
              if (state is GamePlaying) 
                Text('LEVEL ${sl<GamePreferences>().currentLevel}', 
                   style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w900, fontSize: 13, letterSpacing: 1)),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              if (state is GamePlaying) ...[
                _buildStatChip(Icons.stars_rounded, '${sl<GamePreferences>().coins}', Colors.amber),
                const SizedBox(width: 8),
                _buildStatChip(Icons.emoji_events_rounded, '${state.score}', theme.accent),
              ],
              const Spacer(),
              _barButton(Icons.palette_rounded, () => context.read<GameCubit>().cycleTheme()),
              const SizedBox(width: 8),
              _buildHintButton(context, theme),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildLives(int lives, int maxLives) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.black45,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.white12),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.favorite_rounded, color: Color(0xFFFF4444), size: 16),
          const SizedBox(width: 6),
          Text(
            '$lives / $maxLives',
            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w900, fontSize: 13),
          ),
        ],
      ),
    );
  }

  Widget _buildLevelIntroOverlay(List<Snake> snakes, VoidCallback onDismiss) {
    final prefs = sl<GamePreferences>();
    final showBomb = snakes.any((s) => s.type == SnakeType.bomb) && !prefs.hasSeenBombIntro;
    final showLock = snakes.any((s) => s.type == SnakeType.locked) && !prefs.hasSeenLockIntro;

    return BackdropFilter(
      filter: ImageFilter.blur(sigmaX: 10, sigmaY: 10),
      child: Container(
        color: Colors.black54,
        child: Center(
          child: Container(
            margin: const EdgeInsets.all(32),
            padding: const EdgeInsets.all(32),
            decoration: BoxDecoration(
              color: const Color(0xFF1B2E1B),
              borderRadius: BorderRadius.circular(40),
              border: Border.all(color: Colors.white24),
              boxShadow: [BoxShadow(color: Colors.black, blurRadius: 40)],
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.auto_awesome, color: Colors.amber, size: 60),
                const SizedBox(height: 16),
                const Text("NEW CHALLENGE!", style: TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w900)),
                const SizedBox(height: 24),
                
                if (showBomb) _buildIntroRow(Icons.timer_rounded, Colors.redAccent, "BOMB ALERT!", "Clear these snakes before the timer hits ZERO, or it's Game Over!"),
                if (showLock) _buildIntroRow(Icons.lock_rounded, Colors.orangeAccent, "LOCKED PATHS", "Find and clear the KEY snake first to unlock the chains!"),
                
                const SizedBox(height: 32),
                ElevatedButton(
                  onPressed: onDismiss,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF4CAF50),
                    padding: const EdgeInsets.symmetric(horizontal: 60, vertical: 20),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(30)),
                  ),
                  child: const Text("I'M READY", style: TextStyle(color: Colors.white, fontWeight: FontWeight.w900, fontSize: 16)),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildIntroRow(IconData icon, Color color, String title, String desc) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 20),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(color: color.withOpacity(0.15), shape: BoxShape.circle),
            child: Icon(icon, color: color, size: 24),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: TextStyle(color: color, fontWeight: FontWeight.w900, fontSize: 14)),
                const SizedBox(height: 4),
                Text(desc, style: const TextStyle(color: Colors.white70, fontSize: 12, height: 1.4)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatChip(IconData icon, String label, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.black38,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        children: [
          Icon(icon, color: color, size: 14),
          const SizedBox(width: 4),
          Text(label, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w900, fontSize: 12)),
        ],
      ),
    );
  }

  Widget _buildHintButton(BuildContext context, ThemeColors theme) {
    return GestureDetector(
      onTap: () {
        if (!context.read<GameCubit>().onHintRequested()) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Not enough coins!')));
        }
      },
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          gradient: const LinearGradient(colors: [Color(0xFF81C784), Color(0xFF388E3C)]),
          borderRadius: BorderRadius.circular(15),
          boxShadow: [BoxShadow(color: Colors.black45, offset: Offset(0, 2), blurRadius: 4)],
        ),
        child: Row(
          children: [
            const Icon(Icons.stars_rounded, color: Colors.amber, size: 12),
            const Text(" 10", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 10)),
            const SizedBox(width: 6),
            const Icon(Icons.lightbulb_rounded, color: Colors.white, size: 14),
          ],
        ),
      ),
    );
  }

  Widget _barButton(IconData icon, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 36, height: 36,
        decoration: BoxDecoration(color: Colors.white.withOpacity(0.05), shape: BoxShape.circle),
        child: Icon(icon, color: Colors.white70, size: 18),
      ),
    );
  }

  Widget _buildFab({required IconData icon, bool isActive = false, required VoidCallback onTap, required ThemeColors theme}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 60, height: 60,
        decoration: BoxDecoration(
          gradient: isActive 
              ? LinearGradient(colors: [theme.accent, theme.accent.withOpacity(0.7)])
              : LinearGradient(colors: [Colors.white.withOpacity(0.15), Colors.white.withOpacity(0.05)]),
          shape: BoxShape.circle,
          boxShadow: [BoxShadow(color: Colors.black45, blurRadius: 15, offset: const Offset(0, 8))],
          border: Border.all(color: Colors.white12),
        ),
        child: Icon(icon, color: isActive ? Colors.black : Colors.white70, size: 28),
      ),
    );
  }

  Widget _buildWinOverlay(ThemeColors theme, int score, BuildContext context) {
    return Container(
      color: Colors.black87,
      child: Center(
        child: Container(
          padding: const EdgeInsets.all(40),
          decoration: BoxDecoration(
            color: const Color(0xFF1B2E1B),
            borderRadius: BorderRadius.circular(40),
            border: Border.all(color: Colors.white12),
            boxShadow: [BoxShadow(color: Colors.green.withOpacity(0.2), blurRadius: 40)],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.emoji_events_rounded, color: Colors.amber, size: 100),
              const SizedBox(height: 16),
              const Text("LEVEL CLEAR!", style: TextStyle(color: Colors.white, fontSize: 32, fontWeight: FontWeight.w900)),
              Text("SCORE: $score", style: const TextStyle(color: Colors.amber, fontSize: 24, fontWeight: FontWeight.bold)),
              const SizedBox(height: 24),
              const CircularProgressIndicator(color: Colors.green),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildGameOverOverlay(ThemeColors theme, int score, BuildContext context) {
    final state = context.watch<GameCubit>().state;
    final isExplosion = state is GameOver && state.isBombExplosion;

    return FutureBuilder(
      future: Future.delayed(Duration(milliseconds: isExplosion ? 800 : 0)),
      builder: (context, snapshot) {
        final showContent = snapshot.connectionState == ConnectionState.done || !isExplosion;
        
        return Container(
          color: Colors.black.withOpacity(showContent ? 0.85 : 0.0),
          child: Center(
            child: AnimatedOpacity(
              opacity: showContent ? 1.0 : 0.0,
              duration: const Duration(milliseconds: 400),
              child: Container(
                padding: const EdgeInsets.all(40),
                decoration: BoxDecoration(
                  color: const Color(0xFF2E1B1B),
                  borderRadius: BorderRadius.circular(40),
                  border: Border.all(color: Colors.white12),
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.heart_broken_rounded, color: Colors.redAccent, size: 100),
                    const SizedBox(height: 16),
                    const Text("GAME OVER", style: TextStyle(color: Colors.white, fontSize: 32, fontWeight: FontWeight.w900)),
                    const SizedBox(height: 32),
                    ElevatedButton(
                      onPressed: () => context.read<GameCubit>().restartLevel(),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.redAccent,
                        padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 20),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(30)),
                      ),
                      child: const Text("TRY AGAIN", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
                    ),
                  ],
                ),
              ),
            ),
          ),
        );
      }
    );
  }

  Widget _buildLoadingOverlay(ThemeColors theme, double progress) {
    return Container(
      color: const Color(0xFF0F170F),
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            SizedBox(
              width: 100, height: 100,
              child: CircularProgressIndicator(value: progress > 0 ? progress : null, color: Colors.green, strokeWidth: 8),
            ),
            const SizedBox(height: 24),
            const Text("BUILDING LEVEL...", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
          ],
        ),
      ),
    );
  }

  Widget _buildExplosionFlash(GameOver state) {
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0.0, end: 1.0),
      duration: const Duration(milliseconds: 800),
      builder: (context, value, child) {
        return IgnorePointer(
          child: Opacity(
            opacity: (1.0 - value).clamp(0.0, 1.0),
            child: Container(
              decoration: BoxDecoration(
                gradient: RadialGradient(
                  center: state.explodingBombPos != null 
                    ? Alignment(
                        (state.explodingBombPos!.x / 4.0) - 1.0, 
                        (state.explodingBombPos!.y / 4.0) - 1.0, 
                      )
                    : Alignment.center,
                  radius: 2.5 * value,
                  colors: const [Colors.white, Colors.orange, Colors.red, Colors.transparent],
                  stops: const [0.0, 0.2, 0.5, 1.0],
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}
