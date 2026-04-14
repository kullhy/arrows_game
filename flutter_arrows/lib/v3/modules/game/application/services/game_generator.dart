import 'dart:math';

import '../models/board_point.dart';
import '../models/direction.dart';
import '../models/game_constants.dart';
import '../models/game_generator_models.dart';
import '../models/game_level.dart';
import '../models/snake.dart';
import 'generation_utils.dart';
import 'snake_builder.dart';

abstract class BoardShape {
  List<List<bool>> getWalls(int targetWidth, int targetHeight);
}

class GenerationParams {
  final int width;
  final int height;
  final int maxSnakeLength;
  final bool fillTheBoard;
  final BoardShape? boardShape;
  final Function(double)? onProgress;

  final int? seed;
  
  const GenerationParams({
    required this.width,
    required this.height,
    required this.maxSnakeLength,
    this.fillTheBoard = false,
    this.boardShape,
    this.onProgress,
    this.seed,
  });
}

class GameGenerator {
  double _straightPreference;
  final Random _rnd;
  late SnakeBuilder _snakeBuilder;

  GameGenerator()
      : _straightPreference = GameConstants.defaultStraightPreference,
        _rnd = Random();

  void _initSnakeBuilder(int? seed) {
    final random = seed != null ? Random(seed) : Random();
    _snakeBuilder = SnakeBuilder(random, _straightPreference);
  }

  double get straightPreference => _straightPreference;

  set straightPreference(double value) {
    assert(value >= 0.0 && value <= 1.0, "straightPreference must be in [0, 1]");
    _straightPreference = value;
    _snakeBuilder = SnakeBuilder(_rnd, value);
  }

  GameLevel generateSolvableLevel(GenerationParams params) {
    final width = params.fillTheBoard
        ? min(params.width, GameConstants.maxFillBoardSize)
        : params.width;
    final height = params.fillTheBoard
        ? min(params.height, GameConstants.maxFillBoardSize)
        : params.height;

    final walls = params.boardShape?.getWalls(width, height) ??
        List.generate(width, (_) => List.filled(height, false));

    final config = GameGeneratorConfig(
      width: width,
      height: height,
      maxSnakeLength: params.maxSnakeLength,
      fillTheBoard: params.fillTheBoard,
      walls: walls,
    );

    final context = GenerationContext(
      config: config,
      occupied: List.generate(width, (_) => List.filled(height, false)),
      snakes: [],
      frontierCandidates: {},
    );

    _initSnakeBuilder(params.seed);

    final totalCells = GenerationUtils.countValidCells(width, height, walls);
    _generateInitialSnakes(context, totalCells, params.onProgress);

    if (params.fillTheBoard) {
      _fillRemainingBoard(context, totalCells, params.onProgress);
    }

    final postProcessedSnakes = _postProcessSnakes(context.snakes, params.seed);
    final levelId = params.seed ?? DateTime.now().millisecondsSinceEpoch;

    // --- CALCULATE DIFFICULTY LIVES ---
    int lives = 5;
    double density = context.snakes.fold<int>(0, (sum, s) => sum + s.body.length) / totalCells;
    if (density > 0.5) lives--;
    if (density > 0.75) lives--;
    int specialCount = postProcessedSnakes.where((s) => s.type != SnakeType.normal).length;
    if (specialCount > 5) lives--;
    if (specialCount > 10) lives--;
    lives = max(1, lives);

    return GameLevel(id: levelId, width: width, height: height, snakes: postProcessedSnakes, recommendedLives: lives);
  }

  List<Snake> _postProcessSnakes(List<Snake> snakes, int? seed) {
    if (snakes.length < 5) return snakes;
    final rand = seed != null ? Random(seed) : Random();
    final result = List<Snake>.from(snakes);

    // --- OPTIMIZED LOCK/KEY SELECTION (MAX DIFFICULTY) ---
    // Rule: Pick 'Boss' snakes from the VERY FIRST placed (last removed) 
    // and 'Key' snakes from the VERY LAST placed (first removed).
    int snakesCount = snakes.length;
    int lockLimit = max(1, snakesCount ~/ 10);
    
    for (int l = 0; l < lockLimit; l++) {
      // Pick a 'Boss' from the first 20% of snakes
      int bossIdx = rand.nextInt(max(1, snakesCount ~/ 5));
      // Pick a 'Key' from the last 30% of snakes
      int keyIdx = snakesCount - 1 - rand.nextInt(max(1, snakesCount ~/ 3));

      if (bossIdx < keyIdx) {
        final keyId = result[keyIdx].id;
        result[bossIdx] = Snake(
          id: result[bossIdx].id,
          body: result[bossIdx].body,
          headDirection: result[bossIdx].headDirection,
          type: SnakeType.locked,
          lockParentId: keyId,
        );
        result[keyIdx] = Snake(
          id: result[keyIdx].id,
          body: result[keyIdx].body,
          headDirection: result[keyIdx].headDirection,
          type: SnakeType.key,
        );
      }
    }

    // --- ADD BOMBS (STILL RANDOM) ---
    for (int i = 0; i < result.length; i++) {
      if (result[i].type == SnakeType.normal && rand.nextDouble() < 0.1) {
        result[i] = Snake(
          id: result[i].id,
          body: result[i].body,
          headDirection: result[i].headDirection,
          type: SnakeType.bomb,
          bombTimer: rand.nextInt(8) + 4, // More aggressive timers
        );
      }
    }

    return result;
  }

  void _generateInitialSnakes(GenerationContext context, int totalCells, Function(double)? onProgress) {
    var snake = _snakeBuilder.buildFirstSnake(context.config, context.occupied);
    while (snake != null) {
      _addSnakeToContext(context, snake);
      onProgress?.call(_calculateProgress(context.snakes, totalCells));
      snake = _snakeBuilder.buildNextSnake(context);
    }
  }

  void _addSnakeToContext(GenerationContext context, Snake snake) {
    context.snakes.add(snake);
    for (final p in snake.body) {
      context.occupied[p.x][p.y] = true;
    }
    for (final p in snake.body) {
      for (final dir in Direction.values) {
        context.frontierCandidates.remove((p, dir));
      }
    }
    _updateFrontierWithSnake(context, snake);
  }

  void _updateFrontierWithSnake(GenerationContext context, Snake snake) {
    for (final segment in snake.body) {
      for (final dir in Direction.values) {
        final neighbor = segment + dir;
        if (GenerationUtils.isFreeAt(neighbor, context.occupied, context.config)) {
          _addFrontierCandidatesForPoint(context, neighbor);
        }
      }
    }
  }

  void _addFrontierCandidatesForPoint(GenerationContext context, BoardPoint p) {
    for (final headDir in Direction.values) {
      final hasLoS = GenerationUtils.hasClearLoS(
        p, 
        headDir, 
        context.occupied, 
        context.config.width, 
        context.config.height
      );
      if (hasLoS) {
        context.frontierCandidates.add((p, headDir));
      }
    }
  }

  void _fillRemainingBoard(GenerationContext context, int totalCells, Function(double)? onProgress) {
    var lastSnake = _snakeBuilder.buildLastSnake(context);
    while (lastSnake != null) {
      context.snakes.add(lastSnake);
      onProgress?.call(_calculateProgress(context.snakes, totalCells));
      for (final p in lastSnake.body) {
        context.occupied[p.x][p.y] = true;
      }
      lastSnake = _snakeBuilder.buildLastSnake(context);
    }
  }

  double _calculateProgress(List<Snake> snakes, int totalCells) {
    final sum = snakes.fold<int>(0, (prev, s) => prev + s.body.length);
    final progress = sum.toDouble() / totalCells.toDouble();
    return min(max(progress, 0.0), GameConstants.progressFactor.toDouble());
  }
}
