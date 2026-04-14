import 'board_point.dart';
import 'direction.dart';
import 'snake.dart';

class GameGeneratorConfig {
  final int width;
  final int height;
  final int maxSnakeLength;
  final bool fillTheBoard;
  final List<List<bool>> walls;

  const GameGeneratorConfig({
    required this.width,
    required this.height,
    required this.maxSnakeLength,
    required this.fillTheBoard,
    required this.walls,
  });
}

class GenerationContext {
  final GameGeneratorConfig config;
  final List<List<bool>> occupied;
  final List<Snake> snakes;
  final Set<(BoardPoint, Direction)> frontierCandidates;

  GenerationContext({
    required this.config,
    required this.occupied,
    required this.snakes,
    required this.frontierCandidates,
  });
}
