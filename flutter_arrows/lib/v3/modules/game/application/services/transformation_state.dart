import 'dart:math';
import 'package:flutter/material.dart';

import '../models/board_point.dart';
import '../models/game_constants.dart';

class TransformationState {
  double scale = GameConstants.defaultScale;
  double offsetX = 0.0;
  double offsetY = 0.0;

  void reset() {
    scale = GameConstants.defaultScale;
    offsetX = 0.0;
    offsetY = 0.0;
  }

  void transform(Offset pan, double zoom) {
    scale = (scale * zoom).clamp(GameConstants.minScale, GameConstants.maxScale);
    offsetX += pan.dx;
    offsetY += pan.dy;
  }
}
