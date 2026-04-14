import 'dart:math';
import 'package:flutter/material.dart';

import '../models/board_point.dart';
import '../models/game_constants.dart';
import '../models/game_level.dart';
import '../models/snake.dart';

class TapTransformationParams {
  final Offset tapOffset;
  final double containerWidthPx;
  final double containerHeightPx;
  final GameLevel level;
  final double scale;
  final double offsetX;
  final double offsetY;

  const TapTransformationParams({
    required this.tapOffset,
    required this.containerWidthPx,
    required this.containerHeightPx,
    required this.level,
    required this.scale,
    required this.offsetX,
    required this.offsetY,
  });
}

class InputHandler {
  BoardPoint transformTapToGrid(TapTransformationParams params) {
    final cellSize = min(
      params.containerWidthPx / params.level.width,
      params.containerHeightPx / params.level.height,
    );
    final boardWidth = cellSize * params.level.width;
    final boardHeight = cellSize * params.level.height;

    // Accounts for translation to center + user pan/zoom
    final baseLeft = (params.containerWidthPx - boardWidth) / 2;
    final baseTop = (params.containerHeightPx - boardHeight) / 2;

    // Apply scale/offset inversion
    final adjustedTapX = (params.tapOffset.dx - params.offsetX) / params.scale;
    final adjustedTapY = (params.tapOffset.dy - params.offsetY) / params.scale;

    final boardTapX = adjustedTapX - baseLeft;
    final boardTapY = adjustedTapY - baseTop;

    return BoardPoint(
      (boardTapX / cellSize).floor(),
      (boardTapY / cellSize).floor(),
    );
  }
  
  Snake? findTappedSnake(
    Offset localTap,
    double cellSize,
    double boardWidth, double boardHeight,
    double leftOffset, double topOffset,
    List<Snake> snakes,
  ) {
     final adjustedX = localTap.dx - leftOffset;
     final adjustedY = localTap.dy - topOffset;

     if (adjustedX < 0 || adjustedX > boardWidth || adjustedY < 0 || adjustedY > boardHeight) {
       return null; 
     }

     final gridX = (adjustedX / cellSize).floor();
     final gridY = (adjustedY / cellSize).floor();

     Snake? tappedSnake;
     double minDistance = double.infinity;

     for (final snake in snakes) {
       final head = snake.body.first;
       
       final headCx = (head.x * cellSize) + (cellSize / 2);
       final headCy = (head.y * cellSize) + (cellSize / 2);
       
       final tapToleranceRadius = GameConstants.defaultTolerance * cellSize;
       
       final tapOffsetX = headCx + snake.headDirection.dx * cellSize * GameConstants.tapAreaOffsetFactor;
       final tapOffsetY = headCy + snake.headDirection.dy * cellSize * GameConstants.tapAreaOffsetFactor;

       final dx = adjustedX - tapOffsetX;
       final dy = adjustedY - tapOffsetY;
       final distance = sqrt(dx * dx + dy * dy);

       if (distance <= tapToleranceRadius && distance < minDistance) {
         minDistance = distance;
         tappedSnake = snake;
       }
     }

     return tappedSnake;
  }
}
