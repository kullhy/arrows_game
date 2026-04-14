import '../models/board_point.dart';
import '../models/direction.dart';
import '../models/game_level.dart';
import '../models/snake.dart';

class LoSParams {
  final BoardPoint head;
  final Direction dir;
  final int sId;
  final List<List<int>> grid;
  final int w;
  final int h;

  const LoSParams({
    required this.head,
    required this.dir,
    required this.sId,
    required this.grid,
    required this.w,
    required this.h,
  });
}

class SolvabilityChecker {
  static const int _solvabilityIterationMargin = 10;

  static bool isResolvable(GameLevel level) {
    final grid = _createGrid(level);
    final snakeMap = {for (var s in level.snakes) s.id: s};
    final remaining = snakeMap.keys.toSet();
    final maxIter = level.snakes.length + _solvabilityIterationMargin;
    var iter = 0;

    while (remaining.isNotEmpty && iter < maxIter) {
      iter++;
      int? removable;
      for (final sId in remaining) {
        final s = snakeMap[sId]!;
        // Check lock dependency
        bool isLocked = s.type == SnakeType.locked && s.lockParentId != null && remaining.contains(s.lockParentId);
        
        if (!isLocked && _hasCleanLoS(LoSParams(
            head: s.body.first,
            dir: s.headDirection,
            sId: sId,
            grid: grid,
            w: level.width,
            h: level.height))) {
          removable = sId;
          break;
        }
      }

      if (removable == null) return false;

      for (final p in snakeMap[removable]!.body) {
        grid[p.x][p.y] = 0;
      }
      remaining.remove(removable);
    }
    return remaining.isEmpty;
  }

  static int? findRemovableSnake(GameLevel level, [Set<int> ignoreIds = const {}]) {
    final grid = _createGrid(level, ignoreIds: ignoreIds);
    for (final s in level.snakes) {
      if (ignoreIds.contains(s.id)) continue;
      
      // Check lock dependency
      bool isLocked = s.type == SnakeType.locked && s.lockParentId != null && 
          level.snakes.any((key) => key.id == s.lockParentId && !ignoreIds.contains(key.id));

      if (!isLocked && _hasCleanLoS(LoSParams(
          head: s.body.first,
          dir: s.headDirection,
          sId: s.id,
          grid: grid,
          w: level.width,
          h: level.height))) {
        return s.id;
      }
    }
    return null;
  }

  static bool isLineOfSightObstructed(GameLevel level, Snake snake, {Set<int> ignoreIds = const {}}) {
    // Check if snake is locked by a key still on board
    if (snake.type == SnakeType.locked && snake.lockParentId != null) {
      final keyExists = level.snakes.any((s) => s.id == snake.lockParentId && !ignoreIds.contains(s.id));
      if (keyExists) return true;
    }

    final head = snake.body.first;
    final direction = snake.headDirection;
    var current = head + direction;

    while (_isInside(current, level.width, level.height)) {
      final isOccupied = level.snakes.any((other) =>
          !ignoreIds.contains(other.id) && other.body.contains(current));
      if (isOccupied) return true;
      current += direction;
    }
    return false;
  }

  static bool isObstructedBy(GameLevel level, Snake target, Snake blocker) {
    final head = target.body.first;
    final direction = target.headDirection;
    var current = head + direction;

    while (_isInside(current, level.width, level.height)) {
      if (blocker.body.contains(current)) return true;
      current += direction;
    }
    return false;
  }

  static List<List<int>> _createGrid(GameLevel level, {Set<int> ignoreIds = const {}}) {
    final grid = List.generate(level.width, (_) => List.filled(level.height, 0));
    for (final s in level.snakes) {
      if (ignoreIds.contains(s.id)) continue;
      for (final p in s.body) {
        grid[p.x][p.y] = s.id;
      }
    }
    return grid;
  }

  static bool _hasCleanLoS(LoSParams params) {
    var curr = params.head + params.dir;
    while (_isInside(curr, params.w, params.h)) {
      if (params.grid[curr.x][curr.y] != 0 && params.grid[curr.x][curr.y] != params.sId) {
        return false;
      }
      curr += params.dir;
    }
    return true;
  }

  static bool _isInside(BoardPoint p, int w, int h) {
    return p.x >= 0 && p.x < w && p.y >= 0 && p.y < h;
  }
}
