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
        width: 7, height: 9, 
        targetSnakeCount: 16, 
        maxSnakeLength: 6,
        bombProbability: 0.25, lockCount: 2, minDependencyDepth: 3,
      );
    }

    // Board Size Scaling: Grow slowly from 4x5 to 8x10
    int width = (4 + (levelNum / 25)).floor().clamp(4, 8);
    int height = (5 + (levelNum / 20)).floor().clamp(5, 10);

    // Density Scaling: 
    // Start with 5 snakes (Level 1)
    // Add 1 snake every 4 levels
    int targetSnakes = (5 + (levelNum / 4)).floor().clamp(5, 35);
    
    // Snake Length
    int maxLen = levelNum < 15 ? 4 : 6;

    // Challenge Scaling
    double bombProb = 0.0;
    if (levelNum >= 10) {
      // 10% chance at lvl 10, up to 40% at lvl 100
      bombProb = (0.1 + (levelNum - 10) * 0.0035).clamp(0.1, 0.4);
    }

    int locks = 0;
    if (levelNum > 20) {
      // 1 lock at lvl 21, up to 4 locks at lvl 100
      locks = (1 + (levelNum - 20) ~/ 20).clamp(1, 4);
    }

    // Dependency Depth: How many mandatory moves to solve the core puzzle
    int minDepth = 2; // User requested at least 2
    if (levelNum > 40) minDepth = 3;
    if (levelNum > 80) minDepth = 4;

    return LevelConfiguration(
      width: width,
      height: height,
      targetSnakeCount: targetSnakes,
      maxSnakeLength: maxLen,
      bombProbability: bombProb,
      lockCount: locks,
      minDependencyDepth: minDepth,
    );
  }
}
