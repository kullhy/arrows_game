import 'board_point.dart';
import 'snake.dart';

class CriterionParams {
  final List<BoardPoint> body;
  final BoardPoint point;
  final List<Snake> snakes;
  final int width;
  final int height;
  final Set<BoardPoint> forbiddenPoints;
  final List<List<bool>> occupied;

  const CriterionParams({
    required this.body,
    required this.point,
    required this.snakes,
    required this.width,
    required this.height,
    required this.forbiddenPoints,
    required this.occupied,
  });
}

abstract class Criterion {
  bool isSatisfied(CriterionParams params);
}

class NextToExistingSnakeCriterion implements Criterion {
  static const _allDirections = [
    (-1, -1), (0, -1), (1, -1), (-1, 0),
    (1, 0), (-1, 1), (0, 1), (1, 1)
  ];

  @override
  bool isSatisfied(CriterionParams params) {
    for (final (dx, dy) in _allDirections) {
      final nx = params.point.x + dx;
      final ny = params.point.y + dy;
      if (nx >= 0 && nx < params.width && ny >= 0 && ny < params.height && params.occupied[nx][ny]) {
        return true;
      }
    }
    
    final bodyWithoutLast = params.body.length > 1 ? params.body.sublist(0, params.body.length - 1) : <BoardPoint>[];
    for (final segment in bodyWithoutLast) {
      for (final (dx, dy) in _allDirections) {
        if (params.point.x + dx == segment.x && params.point.y + dy == segment.y) {
          return true;
        }
      }
    }
    
    return false;
  }
}

class AlwaysTrueCriterion implements Criterion {
  @override
  bool isSatisfied(CriterionParams params) => true;
}
