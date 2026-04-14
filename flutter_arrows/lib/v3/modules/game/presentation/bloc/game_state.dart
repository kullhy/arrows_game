import 'package:equatable/equatable.dart';
import '../../application/models/game_level.dart';
import '../../application/models/board_point.dart';

sealed class GameState extends Equatable {
  const GameState();

  @override
  List<Object?> get props => [];
}

class GameLoading extends GameState {
  final double progress;

  const GameLoading(this.progress);

  @override
  List<Object?> get props => [progress];
}

class GamePlaying extends GameState {
  final int levelNumber;
  final GameLevel level;
  final int lives;
  final int maxLives;
  final int totalSnakes;
  final Map<int, double> removalProgress;
  final Map<int, double> entryProgress; 
  final int? flashingSnakeId;
  final double guidanceAlpha;
  final int themeIndex;
  final bool cameraShake; 
  final int score; 

  const GamePlaying({
    this.levelNumber = 1,
    required this.level,
    required this.lives,
    required this.maxLives,
    required this.totalSnakes,
    this.removalProgress = const {},
    this.entryProgress = const {},
    this.flashingSnakeId,
    this.guidanceAlpha = 0.0,
    this.themeIndex = 0,
    this.cameraShake = false,
    this.score = 0,
  });

  GamePlaying copyWith({
    int? levelNumber,
    GameLevel? level,
    int? lives,
    int? maxLives,
    int? totalSnakes,
    Map<int, double>? removalProgress,
    Map<int, double>? entryProgress,
    int? flashingSnakeId,
    bool clearFlashing = false,
    double? guidanceAlpha,
    int? themeIndex,
    bool? cameraShake,
    int? score,
  }) {
    return GamePlaying(
      levelNumber: levelNumber ?? this.levelNumber,
      level: level ?? this.level,
      lives: lives ?? this.lives,
      maxLives: maxLives ?? this.maxLives,
      totalSnakes: totalSnakes ?? this.totalSnakes,
      removalProgress: removalProgress ?? this.removalProgress,
      entryProgress: entryProgress ?? this.entryProgress,
      flashingSnakeId: clearFlashing ? null : (flashingSnakeId ?? this.flashingSnakeId),
      guidanceAlpha: guidanceAlpha ?? this.guidanceAlpha,
      themeIndex: themeIndex ?? this.themeIndex,
      cameraShake: cameraShake ?? this.cameraShake,
      score: score ?? this.score,
    );
  }

  @override
  List<Object?> get props => [
        levelNumber,
        level,
        lives,
        maxLives,
        totalSnakes,
        removalProgress,
        entryProgress,
        flashingSnakeId,
        guidanceAlpha,
        themeIndex,
        cameraShake,
        score,
      ];
}

class GameWon extends GameState {
  final int score;
  const GameWon({this.score = 0});

  @override
  List<Object?> get props => [score];
}

class GameOver extends GameState {
  final int score;
  final bool isBombExplosion;
  final BoardPoint? explodingBombPos;
  
  const GameOver({
    this.score = 0, 
    this.isBombExplosion = false,
    this.explodingBombPos,
  });

  @override
  List<Object?> get props => [score, isBombExplosion, explodingBombPos];
}
