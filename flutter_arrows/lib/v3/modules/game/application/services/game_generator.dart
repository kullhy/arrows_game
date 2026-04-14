import 'dart:math';
import 'level_progression.dart';
import '../models/board_point.dart';
import '../models/direction.dart';
import '../models/game_constants.dart';
import '../models/game_generator_models.dart';
import '../models/game_level.dart';
import '../models/snake.dart';
import 'generation_utils.dart';
import 'snake_builder.dart';
import 'solvability_checker.dart';

abstract class BoardShape {
  List<List<bool>> getWalls(int targetWidth, int targetHeight);
}

class GenerationParams {
  final int levelNumber;
  final LevelConfiguration config;
  final bool fillTheBoard;
  final BoardShape? boardShape;
  final Function(double)? onProgress;
  final int? seed;
  
  const GenerationParams({
    required this.levelNumber,
    required this.config,
    this.fillTheBoard = true,
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

  GameLevel generateSolvableLevel(GenerationParams params) {
    final width = params.config.width;
    final height = params.config.height;

    final walls = params.boardShape?.getWalls(width, height) ??
        List.generate(width, (_) => List.filled(height, false));

    final config = GameGeneratorConfig(
      width: width,
      height: height,
      maxSnakeLength: params.config.maxSnakeLength,
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
    
    // BUILD BACKBONE: Place initial snakes
    _generateInitialSnakes(context, totalCells, params.onProgress);
    
    // FILL GAPS: Deep search for remaining spots
    if (params.fillTheBoard) {
      _fillRemainingBoard(context, totalCells, params.onProgress);
    }

    // POST-PROCESS: Add specialized puzzle mechanics based on level configuration
    final postProcessedSnakes = _strategicPostProcess(
      context.snakes, 
      params.seed, 
      width, 
      height, 
      params.levelNumber,
      params.config,
    );

    final levelId = params.seed ?? DateTime.now().millisecondsSinceEpoch;

    // CALCULATE INTELLIGENT LIVES
    int rawLives = _calculateDynamicLives(postProcessedSnakes, totalCells, params.levelNumber);

    return GameLevel(
      id: levelId, 
      width: width, 
      height: height, 
      snakes: postProcessedSnakes, 
      recommendedLives: rawLives
    );
  }

  List<Snake> _strategicPostProcess(
    List<Snake> snakes, int? seed, int w, int h, int levelNum, LevelConfiguration config) {
    if (snakes.isEmpty) return snakes;
    final rand = seed != null ? Random(seed) : Random();
    final result = List<Snake>.from(snakes);

    // 1. VANILLA CHECK: Level 1-9 are vanilla pure puzzles
    if (levelNum > 0 && levelNum < 10) {
      return result;
    }

    // 2. CALCULATE BOTTLE-NECK SCORES
    final dependencies = _calculateDependencies(result, w, h);
    final sortedByBottleneck = List<int>.generate(result.length, (i) => i);
    sortedByBottleneck.sort((a, b) => dependencies[b].length.compareTo(dependencies[a].length));

    // 3. PLACE LOCKS (based on lockCount)
    if (config.lockCount > 0) {
      int actualLocks = min(config.lockCount, snakes.length ~/ 3);
      for (int l = 0; l < actualLocks; l++) {
        if (l >= sortedByBottleneck.length) break;
        int bossIdx = sortedByBottleneck[l];
        if (dependencies[bossIdx].isNotEmpty) {
          int keyId = dependencies[bossIdx].last; 
          int keyIdx = result.indexWhere((s) => s.id == keyId);
          if (keyIdx != -1 && keyIdx > bossIdx) {
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
      }
    }

    // 4. PLACE BOMBS (based on bombProbability)
    if (config.bombProbability > 0 || levelNum >= 10) {
      // Create a map for quick lookup of recursive dependencies
      final transitiveDeps = _calculateTransitiveDependencies(dependencies, snakes.map((s) => s.id).toList());

      for (int i = 0; i < result.length; i++) {
        // CONDITION: Must be a normal snake, meet probability, 
        // AND have at least the required depth from config
        final requiredMoves = transitiveDeps[i].length;
        if (result[i].type == SnakeType.normal && rand.nextDouble() < config.bombProbability && requiredMoves >= config.minDependencyDepth) {
          
          // Difficulty scale:
          int bonus = 2;
          if (levelNum > 40) bonus = 1;
          if (levelNum > 80) bonus = 0;
          
          int finalTimer = max(2, requiredMoves + bonus);

          result[i] = Snake(
            id: result[i].id,
            body: result[i].body,
            headDirection: result[i].headDirection,
            type: SnakeType.bomb,
            bombTimer: finalTimer,
          );
        }
      }

      // MANDATORY CHECK: If level >= 10 and still no bomb, force at least one!
      int bombCount = result.where((s) => s.type == SnakeType.bomb).length;
      if (bombCount == 0 && levelNum >= 10) {
        int bestIdx = -1;
        int maxD = -1;
        for (int i = 0; i < result.length; i++) {
          if (result[i].type == SnakeType.normal) {
            final d = transitiveDeps[i].length;
            if (d > maxD) {
              maxD = d;
              bestIdx = i;
            }
          }
        }
        if (bestIdx != -1 && maxD >= config.minDependencyDepth) {
          int bonus = (levelNum > 40) ? 1 : 2;
          result[bestIdx] = Snake(
            id: result[bestIdx].id,
            body: result[bestIdx].body,
            headDirection: result[bestIdx].headDirection,
            type: SnakeType.bomb,
            bombTimer: maxD + bonus,
          );
        }
      }
    }

    return result;
  }

  /// Recursively calculates all unique snakes that must be removed BEFORE a snake at index i.
  List<Set<int>> _calculateTransitiveDependencies(List<List<int>> directDeps, List<int> allIds) {
    final transitive = List.generate(directDeps.length, (_) => <int>{});
    final idToIndex = {for (int i = 0; i < allIds.length; i++) allIds[i]: i};

    for (int i = directDeps.length - 1; i >= 0; i--) {
      final visited = <int>{};
      final queue = List<int>.from(directDeps[i]);
      
      while (queue.isNotEmpty) {
        final currentId = queue.removeAt(0);
        if (visited.contains(currentId)) continue;
        visited.add(currentId);
        
        transitive[i].add(currentId);
        
        final idx = idToIndex[currentId];
        if (idx != null) {
          queue.addAll(directDeps[idx]);
        }
      }
    }
    return transitive;
  }

  /// Calculates a dependency map: snakeIndex -> list of snakeIds that BLOCK it
  List<List<int>> _calculateDependencies(List<Snake> snakes, int w, int h) {
    final deps = List.generate(snakes.length, (_) => <int>[]);
    final level = GameLevel(id: 0, width: w, height: h, snakes: snakes);
    
    for (int i = 0; i < snakes.length; i++) {
      final s = snakes[i];
      // Find what blocks s
      for (int j = 0; j < snakes.length; j++) {
        if (i == j) continue;
        if (SolvabilityChecker.isObstructedBy(level, s, snakes[j])) {
          deps[i].add(snakes[j].id);
        }
      }
    }
    return deps;
  }

  int _calculateDynamicLives(List<Snake> snakes, int totalCells, int levelNum) {
    // New player safety buffer
    if (levelNum > 0 && levelNum <= 5) return 5;
    
    double density = snakes.fold<int>(0, (sum, s) => sum + s.body.length) / totalCells;
    int complexity = snakes.where((s) => s.type != SnakeType.normal).length;
    
    int lives = 5;
    if (density > 0.6) lives--;
    if (density > 0.8) lives--;
    if (complexity > 4) lives--;
    if (complexity > 8) lives--;
    
    // For high levels, keep it tight
    if (levelNum > 30 && lives > 3) lives = 3;
    if (levelNum > 50 && lives > 2) lives = 2;
    
    return max(1, lives);
  }

  void _generateInitialSnakes(GenerationContext context, int totalCells, Function(double)? onProgress) {
    var snake = _snakeBuilder.buildFirstSnake(context.config, context.occupied);
    while (snake != null) {
      _addSnakeToContext(context, snake);
      onProgress?.call(_calculateProgress(context.snakes, totalCells));
      snake = _snakeBuilder.buildNextSnake(context);
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

  void _addSnakeToContext(GenerationContext context, Snake snake) {
    context.snakes.add(snake);
    for (final p in snake.body) context.occupied[p.x][p.y] = true;
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
      if (GenerationUtils.hasClearLoS(p, headDir, context.occupied, context.config.width, context.config.height)) {
        context.frontierCandidates.add((p, headDir));
      }
    }
  }

  double _calculateProgress(List<Snake> snakes, int totalCells) {
    final sum = snakes.fold<int>(0, (prev, s) => prev + s.body.length);
    return min(1.0, sum / totalCells);
  }
}
