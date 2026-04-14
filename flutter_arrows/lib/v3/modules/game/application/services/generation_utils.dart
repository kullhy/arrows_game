import 'dart:math';

import '../models/board_point.dart';
import '../models/direction.dart';
import '../models/game_generator_models.dart';

class GenerationUtils {
  static bool isInside(BoardPoint p, int w, int h) {
    return p.x >= 0 && p.x < w && p.y >= 0 && p.y < h;
  }

  static Set<BoardPoint> forbiddenPoints(BoardPoint head, Direction dir, int w, int h) {
    final points = <BoardPoint>{};
    var current = head + dir;
    while (isInside(current, w, h)) {
      points.add(current);
      current += dir;
    }
    return points;
  }

  static bool hasClearLoS(BoardPoint start, Direction dir, List<List<bool>> occupied, int w, int h) {
    var current = start + dir;
    while (isInside(current, w, h)) {
      if (occupied[current.x][current.y]) return false;
      current += dir;
    }
    return true;
  }

  static int countValidCells(int width, int height, List<List<bool>> walls) {
    var count = 0;
    for (var x = 0; x < width; x++) {
      for (var y = 0; y < height; y++) {
        if (!walls[x][y]) count++;
      }
    }
    return count;
  }

  static bool isFreeAt(BoardPoint p, List<List<bool>> occupied, GameGeneratorConfig config) {
    return isInside(p, config.width, config.height) &&
        !occupied[p.x][p.y] &&
        !config.walls[p.x][p.y];
  }

  static List<Direction> getOrderedDirections(
    List<Direction> possible,
    Direction? prevDir,
    double straightPreference,
    Random rnd,
  ) {
    if (prevDir == null || straightPreference <= 0.0) return possible;
    final shouldGoStraight = possible.contains(prevDir) && rnd.nextDouble() < straightPreference;
    if (shouldGoStraight) {
      final list = <Direction>[prevDir];
      list.addAll(possible.where((d) => d != prevDir));
      return list;
    } else {
      return possible;
    }
  }
}
