import 'dart:math';

import '../models/board_point.dart';
import '../models/direction.dart';
import '../models/extra_parameter_objects.dart';
import '../models/game_generator_models.dart';
import '../models/game_level.dart';
import '../models/generation_criterion.dart';
import '../models/snake.dart';
import 'generation_utils.dart';
import 'solvability_checker.dart';

class SnakeBuilder {
  int _lastId = 0;
  final Random rnd;
  final double straightPreference;

  SnakeBuilder(this.rnd, this.straightPreference);

  // In Kotlin it used AtomicInteger, here we use a local counter mapped to generate sequential IDs.
  int _nextId() {
    _lastId++;
    return _lastId;
  }

  Snake? buildFirstSnake(GameGeneratorConfig config, List<List<bool>> occupied) {
    BoardPoint head;
    var attempts = 0;
    do {
      head = BoardPoint(rnd.nextInt(config.width), rnd.nextInt(config.height));
      if (attempts++ > 100) return null; // GameConstants.FIRST_SNAKE_MAX_ATTEMPTS
    } while (config.walls[head.x][head.y]);

    final direction = Direction.values[rnd.nextInt(Direction.values.length)];
    final forbidden = GenerationUtils.forbiddenPoints(head, direction, config.width, config.height);
    
    final params = SnakeRecursiveParams(
      config: config,
      occupied: occupied,
      snakes: [],
      body: [head],
      forbidden: forbidden,
      criterion: AlwaysTrueCriterion(),
    );
    
    final body = _buildSnakeRecursive(params);
    return Snake(id: _nextId(), body: body, headDirection: direction);
  }

  Snake? buildNextSnake(GenerationContext context) {
    final candidates = context.frontierCandidates.toList()..shuffle(rnd);
    Snake? bestSnake;
    
    for (final candidate in candidates) {
      final head = candidate.$1;
      final direction = candidate.$2;
      final snake = _tryBuildNextSnake(context, head, direction);
      if (snake == null) continue;
      
      if (snake.body.length >= context.config.maxSnakeLength) return snake;
      
      if (bestSnake == null || snake.body.length > bestSnake.body.length) {
        bestSnake = snake;
      }
    }
    return bestSnake;
  }

  Snake? _tryBuildNextSnake(GenerationContext ctx, BoardPoint head, Direction dir) {
    final isFree = GenerationUtils.isFreeAt(head, ctx.occupied, ctx.config);
    final hasLoS = isFree && GenerationUtils.hasClearLoS(head, dir, ctx.occupied, ctx.config.width, ctx.config.height);

    if (hasLoS) {
      final forbidden = GenerationUtils.forbiddenPoints(head, dir, ctx.config.width, ctx.config.height);
      final params = SnakeRecursiveParams(
        config: ctx.config,
        occupied: ctx.occupied,
        snakes: ctx.snakes,
        body: [head],
        forbidden: forbidden,
        criterion: NextToExistingSnakeCriterion(),
      );
      
      return Snake(id: _nextId(), body: _buildSnakeRecursive(params), headDirection: dir);
    } else {
      return null;
    }
  }

  Snake? buildLastSnake(GenerationContext context) {
    final criterion = NextToExistingSnakeCriterion();
    final candidates = _getFreeCandidates(context, criterion);

    for (final candidate in candidates) {
      final head = candidate.$1;
      final dir = candidate.$2;
      if (!context.config.walls[head.x][head.y]) {
        final snake = _tryBuildBestSnake(context, head, dir, criterion);
        if (snake != null && snake.body.length >= context.config.maxSnakeLength) {
          return snake;
        }
      }
    }
    
    return _findAnyResolvableSnake(context, candidates, criterion);
  }

  List<(BoardPoint, Direction)> _getFreeCandidates(GenerationContext context, Criterion crit) {
    final candidates = <(BoardPoint, Direction)>[];
    for (var x = 0; x < context.config.width; x++) {
      for (var y = 0; y < context.config.height; y++) {
        if (!context.occupied[x][y]) {
          final point = BoardPoint(x, y);
          final p = CriterionParams(
            body: const [],
            point: point,
            snakes: context.snakes,
            width: context.config.width,
            height: context.config.height,
            forbiddenPoints: const {},
            occupied: context.occupied,
          );
          if (crit.isSatisfied(p)) {
            for (final dir in Direction.values) {
              candidates.add((point, dir));
            }
          }
        }
      }
    }
    return candidates;
  }

  Snake? _tryBuildBestSnake(GenerationContext ctx, BoardPoint head, Direction dir, Criterion crit) {
    final forbidden = GenerationUtils.forbiddenPoints(head, dir, ctx.config.width, ctx.config.height);
    final params = SnakeRecursiveParams(
      config: ctx.config,
      occupied: ctx.occupied,
      snakes: ctx.snakes,
      body: [head],
      forbidden: forbidden,
      criterion: crit,
    );
    
    final body = _buildSnakeRecursive(params);
    final snake = Snake(id: _nextId(), body: body, headDirection: dir);
    
    final level = GameLevel(
      id: -1, 
      width: ctx.config.width, 
      height: ctx.config.height, 
      snakes: [...ctx.snakes, snake],
    );
    
    if (SolvabilityChecker.isResolvable(level)) {
      return snake;
    } else {
      return null;
    }
  }

  Snake? _findAnyResolvableSnake(GenerationContext ctx, List<(BoardPoint, Direction)> cands, Criterion crit) {
    Snake? best;
    for (final cand in cands) {
      final head = cand.$1;
      final dir = cand.$2;
      if (!ctx.config.walls[head.x][head.y]) {
        final snake = _tryBuildBestSnake(ctx, head, dir, crit);
        if (snake != null) {
          if (best == null || snake.body.length > best.body.length) {
            best = snake;
          }
        }
      }
    }
    return best;
  }

  List<BoardPoint> _buildSnakeRecursive(SnakeRecursiveParams params) {
    if (params.body.length >= params.config.maxSnakeLength) return params.body;
    
    final tail = params.body.last;
    final possible = Direction.values.toList()..shuffle(rnd);
    final validDirs = possible.where((dir) => _canPlaceSegment(params, tail + dir)).toList();

    if (validDirs.isEmpty) {
      return params.body;
    } else {
      return _findBestRecursiveSnake(params, validDirs);
    }
  }

  List<BoardPoint> _findBestRecursiveSnake(SnakeRecursiveParams params, List<Direction> possible) {
    final tail = params.body.last;
    final ordered = GenerationUtils.getOrderedDirections(possible, params.prevDir, straightPreference, rnd);
    
    var best = params.body;
    for (final direction in ordered) {
      final nextParams = params.copyWith(
        body: [...params.body, tail + direction],
        prevDir: direction,
      );
      final candidate = _buildSnakeRecursive(nextParams);
      if (candidate.length >= params.config.maxSnakeLength) return candidate;
      if (candidate.length > best.length) best = candidate;
    }
    return best;
  }

  bool _canPlaceSegment(SnakeRecursiveParams params, BoardPoint next) {
    final isInside = GenerationUtils.isInside(next, params.config.width, params.config.height);
    if (!isInside) return false;

    final isBasicFree = !params.forbidden.contains(next) && 
                       !params.body.contains(next) &&
                       !params.config.walls[next.x][next.y] && 
                       !params.occupied[next.x][next.y];

    if (isBasicFree) {
      final cParams = CriterionParams(
        body: params.body,
        point: next,
        snakes: params.snakes,
        width: params.config.width,
        height: params.config.height,
        forbiddenPoints: params.forbidden,
        occupied: params.occupied,
      );
      return params.criterion.isSatisfied(cParams);
    } else {
      return false;
    }
  }
}
