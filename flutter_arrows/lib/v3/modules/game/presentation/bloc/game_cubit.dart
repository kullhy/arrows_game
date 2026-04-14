import 'dart:async';
import 'package:flutter/services.dart';
import 'package:flutter_arrows/v3/modules/game/application/services/game_preferences.dart';
import 'package:flutter_arrows/v3/modules/game/application/services/sound_service.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../application/models/game_constants.dart';
import '../../application/models/game_level.dart';
import '../../application/models/snake.dart';
import '../../application/services/game_generator.dart';
import '../../application/services/solvability_checker.dart';
import '../../application/services/level_progression.dart';
import '../theme_colors.dart';
import 'game_state.dart';

class GameCubit extends Cubit<GameState> {
  final GameGenerator _gameGenerator;
  final SoundService _soundService;
  final GamePreferences _prefs;
  int _currentLevel = 1;
  int _currentTheme = 0;
  int _score = 0;

  // Undo stack: stores previous GamePlaying states
  final List<GamePlaying> _undoStack = [];
  static const int _maxUndoDepth = 10;

  GameCubit({
    required GameGenerator gameGenerator,
    required GamePreferences prefs,
    required SoundService soundService,
  })  : _gameGenerator = gameGenerator,
        _prefs = prefs,
        _soundService = soundService,
        super(const GameLoading(0.0)) {
    _currentLevel = _prefs.currentLevel;
    _score = _prefs.score;
    _currentTheme = _prefs.themeIndex;
    loadOrRegenerateLevel();
  }

  void loadOrRegenerateLevel() async {
    emit(const GameLoading(0.0));
    _undoStack.clear();
    await Future.delayed(const Duration(milliseconds: 50));

    final config = LevelProgression.calculateLevelConfiguration(levelNum: _currentLevel);

    final params = GenerationParams(
      width: config.width,
      height: config.height,
      maxSnakeLength: config.maxSnakeLength,
      onProgress: (p) => emit(GameLoading(p)),
    );

    final level = _gameGenerator.generateSolvableLevel(params);
    final playing = GamePlaying(
      levelNumber: _currentLevel,
      level: level,
      lives: level.recommendedLives,
      maxLives: level.recommendedLives,
      totalSnakes: level.snakes.length,
      themeIndex: _currentTheme,
      score: _score,
    );
    emit(playing);

    // Start entry animation
    _animateSnakeEntry(playing);
  }

  void generateCustomLevel(int size, bool fillBoard, {BoardShape? boardShape}) async {
    emit(const GameLoading(0.0));
    _undoStack.clear();
    await Future.delayed(const Duration(milliseconds: 50));

    final params = GenerationParams(
      width: size,
      height: size,
      maxSnakeLength: GameConstants.minSnakeLengthMax,
      fillTheBoard: fillBoard,
      boardShape: boardShape,
      onProgress: (p) => emit(GameLoading(p)),
    );

    final level = _gameGenerator.generateSolvableLevel(params);
    final playing = GamePlaying(
      levelNumber: _currentLevel,
      level: level,
      lives: level.recommendedLives,
      maxLives: level.recommendedLives,
      totalSnakes: level.snakes.length,
      themeIndex: _currentTheme,
      score: _score,
    );
    emit(playing);
    _animateSnakeEntry(playing);
  }

  void generateDailyChallenge() async {
    emit(const GameLoading(0.0));
    _undoStack.clear();
    await Future.delayed(const Duration(milliseconds: 50));

    // Use date as seed
    final now = DateTime.now();
    final seed = now.year * 10000 + now.month * 100 + now.day;
    
    // Check if already completed today
    if (_prefs.dailyChallengeDate == seed.toString() && _prefs.dailyChallengeCompleted) {
      // Show already done? For now just let them play but maybe don't award much.
    }

    final config = LevelProgression.calculateLevelConfiguration(levelNum: 50); // Harder

    final params = GenerationParams(
      width: config.width,
      height: config.height,
      maxSnakeLength: config.maxSnakeLength,
      seed: seed,
      onProgress: (p) => emit(GameLoading(p)),
    );

    final level = _gameGenerator.generateSolvableLevel(params);
    final playing = GamePlaying(
      levelNumber: 0, // 0 for daily
      level: level,
      lives: 1, // Only 1 life for daily!
      maxLives: 1,
      totalSnakes: level.snakes.length,
      themeIndex: _currentTheme,
      score: _score,
    );
    emit(playing);
    _animateSnakeEntry(playing);
  }

  /// Animate snakes appearing one by one with stagger
  Future<void> _animateSnakeEntry(GamePlaying initialState) async {
    final snakeIds = initialState.level.snakes.map((s) => s.id).toList();
    final totalFrames = 15; // frames per snake

    for (int sIdx = 0; sIdx < snakeIds.length; sIdx++) {
      for (int frame = 0; frame <= totalFrames; frame++) {
        if (isClosed) return;
        await Future.delayed(const Duration(milliseconds: 8));
        final current = state;
        if (current is! GamePlaying) return;

        final newEntry = Map<int, double>.from(current.entryProgress);
        newEntry[snakeIds[sIdx]] = frame / totalFrames;
        emit(current.copyWith(entryProgress: newEntry));
      }

      // Stagger: wait a tiny bit before next snake starts
      if (sIdx < snakeIds.length - 1) {
        await Future.delayed(const Duration(milliseconds: 20));
      }
    }

    // Clear entry progress once all done
    if (state is GamePlaying) {
      emit((state as GamePlaying).copyWith(entryProgress: const {}));
    }
  }

  void restartLevel() {
    loadOrRegenerateLevel();
  }

  bool get canUndo => _undoStack.isNotEmpty;

  void jumpToLevel(int levelNum) {
    _currentLevel = levelNum;
    loadOrRegenerateLevel();
  }

  void undo() {
    if (_undoStack.isEmpty) return;
    final previous = _undoStack.removeLast();
    emit(previous);
    _soundService.playClick();
    HapticFeedback.lightImpact();
  }

  void onSnakeTapped(int snakeId) {
    final currentState = state;
    if (currentState is! GamePlaying) return;
    if (currentState.removalProgress.containsKey(snakeId)) return;
    if (currentState.entryProgress.isNotEmpty) return; // Wait for entry animation

    final level = currentState.level;
    final tappedSnake = level.snakes.firstWhere((s) => s.id == snakeId);

    final isObstructed = SolvabilityChecker.isLineOfSightObstructed(
      level,
      tappedSnake,
      ignoreIds: currentState.removalProgress.keys.toSet(),
    );

    if (isObstructed) {
      _handleWrongTap(currentState, snakeId);
    } else {
      // Save state for undo before modifying
      if (_undoStack.length >= _maxUndoDepth) _undoStack.removeAt(0);
      _undoStack.add(currentState);

      // Success feedback
      _soundService.playCorrect();
      HapticFeedback.lightImpact();
      _startRemovalAnimation(snakeId, currentState);
    }
  }

  void _handleWrongTap(GamePlaying currentState, int snakeId) {
    final newLives = currentState.lives - 1;

    // Feedback for wrong tap
    _soundService.playWrong();
    HapticFeedback.heavyImpact();

    // Flash the snake red + trigger camera shake
    emit(currentState.copyWith(
      lives: newLives > 0 ? newLives : currentState.lives,
      flashingSnakeId: snakeId,
      cameraShake: true,
    ));

    // Clear shake after short delay
    Future.delayed(const Duration(milliseconds: 200), () {
      if (isClosed) return;
      final current = state;
      if (current is GamePlaying && current.cameraShake) {
        emit(current.copyWith(cameraShake: false));
      }
    });

    // Clear flash and apply life loss / game over
    Future.delayed(const Duration(milliseconds: 500), () {
      if (isClosed) return;
      final current = state;
      if (current is GamePlaying && current.flashingSnakeId == snakeId) {
        if (newLives <= 0) {
          emit(GameOver(score: current.score));
        } else {
          emit(current.copyWith(
            lives: newLives,
            clearFlashing: true,
          ));
        }
      }
    });
  }

  Future<void> _startRemovalAnimation(int snakeId, GamePlaying initialState) async {
    final totalFrames = 25;
    for (var i = 1; i <= totalFrames; i++) {
      if (isClosed) return;
      await Future.delayed(const Duration(milliseconds: 14));
      final current = state;
      if (current is GamePlaying) {
        final newMap = Map<int, double>.from(current.removalProgress);
        final double t = i / totalFrames;
        // Ease-in-out cubic for smooth slide-out
        newMap[snakeId] = t < 0.5 ? 4 * t * t * t : 1 - ((-2 * t + 2) * (-2 * t + 2) * (-2 * t + 2)) / 2;
        emit(current.copyWith(removalProgress: newMap));
      }
    }

    // Complete removal
    final current = state;
    if (current is GamePlaying) {
      final remainingSnakes = current.level.snakes.where((s) => s.id != snakeId).toList();
      final newLevel = GameLevel(
        id: current.level.id,
        width: current.level.width,
        height: current.level.height,
        snakes: remainingSnakes,
      );

      final newMap = Map<int, double>.from(current.removalProgress)..remove(snakeId);
      final newScore = current.score + 10;
      
      // Update bombs
      bool bombExploded = false;
      final updatedSnakes = remainingSnakes.map((s) {
        if (s.type == SnakeType.bomb) {
          final newTimer = s.bombTimer - 1;
          if (newTimer <= 0) bombExploded = true;
          return s.copyWith(bombTimer: newTimer);
        }
        return s;
      }).toList();

      if (bombExploded) {
        _soundService.playWrong();
        HapticFeedback.vibrate();
        emit(GameOver(score: newScore));
        return;
      }

      final updatedLevel = GameLevel(
        id: current.level.id,
        width: current.level.width,
        height: current.level.height,
        snakes: updatedSnakes,
      );

      if (updatedSnakes.isEmpty) {
        // Level complete bonus
        final finalScore = newScore + (current.lives * 50);
        
        // Calculate stars
        int stars = 1;
        if (current.lives >= current.maxLives) stars = 3;
        else if (current.lives >= current.maxLives / 2) stars = 2;

        // Save progress
        _prefs.setStarsForLevel(_currentLevel, stars);
        _currentLevel++;
        _prefs.currentLevel = _currentLevel;
        _score = finalScore;
        _prefs.score = _score;
        _prefs.addCoins(stars * 10);

        _soundService.playWin();
        HapticFeedback.heavyImpact();
        emit(GameWon(score: finalScore));
        
        Future.delayed(const Duration(milliseconds: 2500), () {
          loadOrRegenerateLevel();
        });
      } else {
        if (current.levelNumber == 0 && updatedSnakes.isEmpty) {
          // Special handling for daily challenge completion
          final now = DateTime.now();
          final seed = now.year * 10000 + now.month * 100 + now.day;
          _prefs.dailyChallengeDate = seed.toString();
          _prefs.dailyChallengeCompleted = true;
          _prefs.addCoins(100); // Big reward
        }

        emit(current.copyWith(
          level: updatedLevel,
          removalProgress: newMap,
          score: newScore,
        ));
      }
    }
  }

  bool onHintRequested() {
    final current = state;
    if (current is! GamePlaying) return false;

    // Hint costs 10 coins
    if (!_prefs.spendCoins(10)) return false;

    final removableId = SolvabilityChecker.findRemovableSnake(
      current.level,
      current.removalProgress.keys.toSet(),
    );
    if (removableId != null) {
      HapticFeedback.selectionClick();
      emit(current.copyWith(flashingSnakeId: removableId));

      Future.delayed(const Duration(milliseconds: 1200), () {
        if (isClosed) return;
        final nowState = state;
        if (nowState is GamePlaying && nowState.flashingSnakeId == removableId) {
          emit(nowState.copyWith(clearFlashing: true));
        }
      });
      return true;
    }
    return false;
  }

  void setGuidanceAlpha(double alpha) {
    final current = state;
    if (current is GamePlaying) {
      emit(current.copyWith(guidanceAlpha: alpha));
    }
  }

  void toggleGuidance() {
    final current = state;
    if (current is GamePlaying) {
      setGuidanceAlpha(current.guidanceAlpha == 0.0 ? 1.0 : 0.0);
    }
  }

  void cycleTheme() {
    final current = state;
    if (current is GamePlaying) {
      _currentTheme = (_currentTheme + 1) % ThemeColors.allThemes.length;
      _prefs.themeIndex = _currentTheme;
      emit(current.copyWith(themeIndex: _currentTheme));
    }
  }
}
