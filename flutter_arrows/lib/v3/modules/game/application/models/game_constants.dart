import 'dart:math';

class GameConstants {
  // ====================
  // GAME ENGINE DEFAULTS
  // ====================
  static const int initialLives = 5;

  // ====================
  // GAME PROGRESSION
  // ====================
  static const int generatorUnlockLevel = 20;

  // ====================
  // CUSTOM GENERATOR UI
  // ====================
  static const double generatorMinSize = 20.0;
  static const double generatorMaxSize = 100.0;
  static const double generatorMaxSizeFillBoard = 35.0;
  static const double generatorDefaultSize = 35.0;
  static const String shapeTypeRectangular = "rectangular";
  static const int shapeIconSize = 32;

  // ====================
  // LEVEL GENERATION ENGINE
  // ====================
  static const double defaultStraightPreference = 0.90;
  static const int maxFillBoardSize = 35;
  static const double progressFactor = 1.0;
  static const int firstSnakeMaxAttempts = 100;

  // ====================
  // LEVEL PROGRESSION
  // ====================
  static const int baseBoardSize = 5;
  static const int sizeReductionPerStep = 3;
  static const int livesReductionPerStep = 1;
  static const int levelsPerProgressionStep = 10;
  static const int defaultInitialLives = 5;
  static const int minSnakeLengthBase = 3;
  static const int minSnakeLengthMin = 4;
  static const int minSnakeLengthMax = 30;

  // ====================
  // LEVEL MANAGER (SHAPE PROBABILITY)
  // ====================
  static const int minBoardSizeForShapes = 20;
  static const int maxBoardSizeForAlwaysShape = 100;
  static const double baseShapeProbability = 0.5;

  // ====================
  // ZOOM & PAN
  // ====================
  static const double defaultScale = 0.94;
  static const double minScale = 0.2;
  static const double maxScale = 6.0;

  // ====================
  // GAME FLOW & ANIMATIONS
  // ====================
  static const int gameWonExitDelay = 3000;
  static const int gamesBetweenInterstitials = 5;
  static const int guidanceAnimDuration = 500;
  static const int progressAnimDuration = 500;
  static const int progressBarWidth = 200;
  static const double debugCircleRadius = 20.0;
  static const int percentMultiplier = 100;

  // ====================
  // CONFETTI CELEBRATION ANIMATION
  // ====================
  static const double confettiMaxSpeed = 30.0;
  static const double confettiDamping = 0.9;
  static const int confettiSpread = 360;
  static const int confettiDurationMs = 100;
  static const int confettiEmitterMax = 100;
  static const double confettiRelX = 0.5;
  static const double confettiRelY = 0.3;

  // ====================
  // TAP ANIMATION
  // ====================
  static const int rippleDuration = 300;
  static const double rippleMaxRadius = 40.0;

  // ====================
  // SNAKE REMOVAL ANIMATION
  // ====================
  static const int removalFrameDelayMs = 16;
  static const int removalDurationHigh = 300;
  static const int removalDurationMedium = 600;
  static const int removalDurationLow = 900;

  // ====================
  // BOARD ENTRY ANIMATIONS
  // ====================
  static const double boardEntryScaleFrom = 0.92;
  static const int snakeEntryDurationMs = 450;
  static const int snakeEntryStaggerMs = 50;

  // ====================
  // BOARD RENDERING & VISUALS
  // ====================
  static const double boardBorderWidth = 2.0;
  static const double guidanceLineAlphaFactor = 0.4;
  static const double guidanceDashOn = 10.0;
  static const double guidanceDashOff = 10.0;
  static const double tapAreaAlpha = 0.3;
  static const double singleBlockTailFactor = 0.2;
  static const double arrowHeadSizeFactor = 0.2;
  static const double boardStrokeWidthFactor = 0.18;
  static const double boardCornerRadiusFactor = 0.35;
  static const double snakeMoveDistFactor = 1.0;
  static const double arrowHeadStrokeWidthFactor = 0.3;
  static const int flashDurationMs = 3000;
  static const int flashPulseDuration = 250;
  static const double flashMinAlpha = 0.2;
  static const double arrowHeadCenterFactor = 0.5;

  // ====================
  // ARROW DIRECTION ANGLES
  // ====================
  static const double angleUp = 270.0;
  static const double angleDown = 90.0;
  static const double angleLeft = 180.0;
  static const double angleRight = 0.0;
  static const double angleTriangleOffset = 2.094;
  static const double degToRad = pi / 180.0;

  // ====================
  // VIEW MODEL
  // ====================
  static const int stopTimeoutMillis = 5000;

  // ====================
  // USER PREFERENCES DEFAULTS
  // ====================
  static const int defaultLevel = 1;
  static const int defaultLives = 5;

  // ====================
  // ADS
  // ====================
  static const int requiredAdCountForAdFree = 30;

  // ====================
  // INPUT HANDLING
  // ====================
  static const double cellCenter = 0.5;
  static const double tapAreaOffsetFactor = 0.3;
  static const double defaultTolerance = 1.3;

  // ====================
  // WIN CELEBRATION VIDEO
  // ====================
  static const int videoFadeInDuration = 1000;
  static const int videoDisplayDuration = 3000;
  static const int videoFadeOutDuration = 1000;
  static const int winVideosCount = 26;
  static const int videoPreparationDelay = 50;
  static const int congratulationsFontSize = 32;
}
