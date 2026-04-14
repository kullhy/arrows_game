import 'board_point.dart';
import 'direction.dart';
import 'game_generator_models.dart';
import 'generation_criterion.dart';
import 'snake.dart';

class SnakeRecursiveParams {
  final GameGeneratorConfig config;
  final List<List<bool>> occupied;
  final List<Snake> snakes;
  final List<BoardPoint> body;
  final Set<BoardPoint> forbidden;
  final Criterion criterion;
  final Direction? prevDir;

  const SnakeRecursiveParams({
    required this.config,
    required this.occupied,
    required this.snakes,
    required this.body,
    required this.forbidden,
    required this.criterion,
    this.prevDir,
  });

  SnakeRecursiveParams copyWith({
    List<BoardPoint>? body,
    Direction? prevDir,
  }) {
    return SnakeRecursiveParams(
      config: config,
      occupied: occupied,
      snakes: snakes,
      body: body ?? this.body,
      forbidden: forbidden,
      criterion: criterion,
      prevDir: prevDir ?? this.prevDir,
    );
  }
}
