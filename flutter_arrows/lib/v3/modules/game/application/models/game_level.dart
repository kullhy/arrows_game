import 'package:equatable/equatable.dart';

import 'snake.dart';

class GameLevel extends Equatable {
  final int id;
  final int width;
  final int height;
  final List<Snake> snakes;
  final int recommendedLives;

  const GameLevel({
    required this.id,
    required this.width,
    required this.height,
    required this.snakes,
    this.recommendedLives = 3,
  });

  @override
  List<Object?> get props => [id, width, height, snakes, recommendedLives];
}
