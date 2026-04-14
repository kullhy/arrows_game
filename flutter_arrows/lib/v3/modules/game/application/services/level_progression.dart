import '../models/game_constants.dart';

class LevelConfiguration {
  final int width;
  final int height;
  final int maxSnakeLength;
  final int maxLives;

  const LevelConfiguration(this.width, this.height, this.maxSnakeLength, this.maxLives);
}

class LevelProgression {
  static const int _extraSizeReductionInterval = 15;
  static const int _extraLivesReductionInterval = 12;
  static const int _levelSizeIncrementInterval = 2;
  static const int _minBoardSize = 4;
  static const int _extraSnakeLengthInterval = 8;

  static LevelConfiguration calculateLevelConfiguration({
    required int levelNum,
    int? forcedWidth,
    int? forcedHeight,
    int? forcedLives,
  }) {
    final safeLevel = levelNum < 1 ? 1 : levelNum;
    final progressionStep = safeLevel ~/ GameConstants.levelsPerProgressionStep;
    final sizeReduction = progressionStep * GameConstants.sizeReductionPerStep;
    final extraSizeReduction = safeLevel ~/ _extraSizeReductionInterval;
    final livesReduction = progressionStep * GameConstants.livesReductionPerStep +
        safeLevel ~/ _extraLivesReductionInterval;

    final baseH = GameConstants.baseBoardSize + (safeLevel - 1) ~/ _levelSizeIncrementInterval;
    final baseW = GameConstants.baseBoardSize + safeLevel ~/ _levelSizeIncrementInterval;

    var h = forcedHeight ?? (baseH - sizeReduction - extraSizeReduction);
    if (h < _minBoardSize) h = _minBoardSize;

    var w = forcedWidth ?? (baseW - sizeReduction - extraSizeReduction);
    if (w < _minBoardSize) w = _minBoardSize;

    var maxLives = forcedLives ?? (GameConstants.defaultInitialLives - livesReduction);
    if (maxLives < 1) maxLives = 1;
    if (maxLives > GameConstants.defaultInitialLives) maxLives = GameConstants.defaultInitialLives;

    var snakeLen = GameConstants.minSnakeLengthBase +
        safeLevel ~/ _levelSizeIncrementInterval +
        safeLevel ~/ _extraSnakeLengthInterval;
    if (snakeLen < GameConstants.minSnakeLengthMin) snakeLen = GameConstants.minSnakeLengthMin;
    if (snakeLen > GameConstants.minSnakeLengthMax) snakeLen = GameConstants.minSnakeLengthMax;

    return LevelConfiguration(w, h, snakeLen, maxLives);
  }
}
