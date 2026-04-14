import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:flutter_arrows/v3/modules/game/application/models/game_constants.dart';
import 'package:flutter_arrows/v3/modules/game/presentation/bloc/game_cubit.dart';
import 'package:flutter_arrows/v3/modules/game/presentation/bloc/game_state.dart';
import 'package:flutter_arrows/v3/modules/game/presentation/theme_colors.dart';
import 'package:flutter_arrows/v3/modules/game/presentation/widgets/arrows_board_painter.dart';

class ArrowsBoardWidget extends StatefulWidget {
  const ArrowsBoardWidget({super.key});

  @override
  State<ArrowsBoardWidget> createState() => _ArrowsBoardWidgetState();
}

class _ArrowsBoardWidgetState extends State<ArrowsBoardWidget> with SingleTickerProviderStateMixin {
  late AnimationController _pulseController;

  @override
  void initState() {
    super.initState();
    _pulseController = AnimationController(
       vsync: this,
       duration: const Duration(milliseconds: GameConstants.flashPulseDuration),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _pulseController.dispose();
    super.dispose();
  }

  void _handleTap(BuildContext context, Offset localPosition, Size size, GamePlaying state) {
    final cellSize = min(size.width / state.level.width, size.height / state.level.height);
    final boardWidth = cellSize * state.level.width;
    final boardHeight = cellSize * state.level.height;

    final leftOffset = (size.width - boardWidth) / 2;
    final topOffset = (size.height - boardHeight) / 2;

    final adjustedX = localPosition.dx - leftOffset;
    final adjustedY = localPosition.dy - topOffset;

    if (adjustedX < 0 || adjustedX > boardWidth || adjustedY < 0 || adjustedY > boardHeight) {
      return;
    }

    int? tappedSnakeId;
    double minDistance = double.infinity;

    for (final snake in state.level.snakes) {
      if (state.removalProgress.containsKey(snake.id)) continue;

      for (final part in snake.body) {
        final partCx = (part.x * cellSize) + (cellSize / 2);
        final partCy = (part.y * cellSize) + (cellSize / 2);
        
        final tapToleranceRadius = GameConstants.defaultTolerance * cellSize;
        
        final dx = adjustedX - partCx;
        final dy = adjustedY - partCy;
        final distance = sqrt(dx * dx + dy * dy);

        if (distance <= tapToleranceRadius && distance < minDistance) {
          minDistance = distance;
          tappedSnakeId = snake.id;
        }
      }
    }

    if (tappedSnakeId != null) {
      context.read<GameCubit>().onSnakeTapped(tappedSnakeId);
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<GameCubit, GameState>(
      builder: (context, state) {
        // Only render the board for GamePlaying state
        // Loading, Won, GameOver are handled by GameScreen overlays
        if (state is GamePlaying) {
          return LayoutBuilder(
            builder: (context, constraints) {
              final size = Size(constraints.maxWidth, constraints.maxHeight);
              return GestureDetector(
                onTapUp: (details) => _handleTap(context, details.localPosition, size, state),
                child: AnimatedBuilder(
                  animation: _pulseController,
                  builder: (context, child) {
                    final pulseAlpha = GameConstants.flashMinAlpha + 
                        (_pulseController.value * (1.0 - GameConstants.flashMinAlpha));
                    
                    return CustomPaint(
                      size: size,
                      painter: ArrowsBoardPainter(
                        level: state.level,
                        flashingSnakeId: state.flashingSnakeId,
                        guidanceAlpha: state.guidanceAlpha,
                        flashPulseAlpha: pulseAlpha,
                        removalProgress: state.removalProgress,
                        entryProgress: state.entryProgress,
                        themeColors: ThemeColors.allThemes[state.themeIndex],
                      ),
                    );
                  },
                ),
              );
            },
          );
        }

        return const SizedBox.shrink();
      },
    );
  }
}
