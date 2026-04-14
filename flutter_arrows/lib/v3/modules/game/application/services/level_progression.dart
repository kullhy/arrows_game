class LevelConfiguration {
  final int width;
  final int height;
  final int targetSnakeCount;
  final int maxSnakeLength;
  final double bombProbability;
  final int lockCount;
  final int minDependencyDepth;

  const LevelConfiguration({
    required this.width,
    required this.height,
    required this.targetSnakeCount,
    required this.maxSnakeLength,
    required this.bombProbability,
    required this.lockCount,
    required this.minDependencyDepth,
  });
}

class LevelProgression {
  static LevelConfiguration calculateLevelConfiguration({required int levelNum}) {
    // 0 is for custom/daily
    if (levelNum == 0) {
      return const LevelConfiguration(
        width: 8, height: 10, 
        targetSnakeCount: 45, // Extreme
        maxSnakeLength: 6,
        bombProbability: 0.4, lockCount: 4, minDependencyDepth: 5,
      );
    }

    // --- CASE 1: INTRO (L1 - L3) ---
    // Keep intro simple but slightly tighter
    if (levelNum <= 3) {
      return LevelConfiguration(
        width: 4, height: 5,
        targetSnakeCount: (5 + levelNum * 2), 
        maxSnakeLength: 4,
        bombProbability: 0.0,
        lockCount: 0,
        minDependencyDepth: 1,
      );
    }

    // --- CASE 2: BOSS LEVELS (Every 4th level) ---
    final isBossLevel = levelNum % 4 == 0;
    
    // Extreme Base Progression after L3
    // We jump to 7x9 or 8x10 very quickly
    int baseWidth = (6 + (levelNum / 15)).floor().clamp(6, 8);
    int baseHeight = (8 + (levelNum / 10)).floor().clamp(8, 10);
    // Double the snake density
    int baseSnakes = (25 + (levelNum / 1.5)).floor().clamp(25, 60);

    if (isBossLevel) {
      // Boss levels are 8x10 instantly if level > 12
      return LevelConfiguration(
        width: (levelNum > 12 ? 8 : baseWidth + 1).clamp(6, 8),
        height: (levelNum > 12 ? 10 : baseHeight + 1).clamp(8, 10),
        targetSnakeCount: (baseSnakes * 1.6).floor().clamp(35, 75),
        maxSnakeLength: 6,
        bombProbability: (0.3 + (levelNum / 80)).clamp(0.3, 0.6),
        lockCount: (3 + (levelNum / 20)).floor().clamp(3, 6),
        minDependencyDepth: (levelNum < 12 ? 4 : 5), 
      );
    }

    // --- CASE 3: STANDARD LEVELS (L5, L6, L7, L9...) ---
    return LevelConfiguration(
      width: baseWidth,
      height: baseHeight,
      targetSnakeCount: baseSnakes,
      maxSnakeLength: 6,
      bombProbability: (0.2 + (levelNum / 150)).clamp(0.2, 0.5),
      lockCount: (levelNum > 10 ? 2 : 1), // At least 1 lock after L3
      minDependencyDepth: (levelNum < 30 ? 3 : 4),
    );
  }
}
