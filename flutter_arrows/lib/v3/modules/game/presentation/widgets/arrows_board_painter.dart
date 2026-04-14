import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_arrows/v3/modules/game/application/models/game_level.dart';

import 'package:flutter_arrows/v3/modules/game/application/models/board_point.dart';
import 'package:flutter_arrows/v3/modules/game/application/models/direction.dart' as game_dir;
import 'package:flutter_arrows/v3/modules/game/application/models/game_constants.dart';
import 'package:flutter_arrows/v3/modules/game/application/models/snake.dart';
import 'package:flutter_arrows/v3/modules/game/presentation/theme_colors.dart';

class ArrowsBoardPainter extends CustomPainter {
  final GameLevel level;
  final int? flashingSnakeId;
  final double flashPulseAlpha;
  final Map<int, double> removalProgress;
  final Map<int, double> entryProgress;
  final double guidanceAlpha;
  final ThemeColors themeColors;

  ArrowsBoardPainter({
    required this.level,
    this.flashingSnakeId,
    this.flashPulseAlpha = 1.0,
    this.removalProgress = const {},
    this.entryProgress = const {},
    this.guidanceAlpha = 0.0,
    this.themeColors = ThemeColors.defaultTheme,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final cellSize = min(size.width / level.width, size.height / level.height);
    final boardWidth = cellSize * level.width;
    final boardHeight = cellSize * level.height;

    final metrics = _BoardMetrics(
      cellWidth: cellSize,
      cellHeight: cellSize,
      strokeWidth: cellSize * GameConstants.boardStrokeWidthFactor,
      cornerRadius: cellSize * GameConstants.boardCornerRadiusFactor,
      arrowHeadSize: cellSize * GameConstants.arrowHeadSizeFactor,
      moveDist: max(size.width, size.height) * GameConstants.snakeMoveDistFactor,
      boardWidth: boardWidth,
      boardHeight: boardHeight,
    );

    final leftOffset = (size.width - metrics.boardWidth) / 2;
    final topOffset = (size.height - metrics.boardHeight) / 2;

    _drawBoardSurface(canvas, leftOffset, topOffset, metrics);

    canvas.save();
    canvas.translate(leftOffset, topOffset);

    _drawGridSlots(canvas, metrics);

    if (guidanceAlpha > 0) {
      _drawGuidanceLines(canvas, metrics, size, leftOffset, topOffset);
    }

    _drawSnakes(canvas, metrics);

    canvas.restore();
  }

  void _drawGridSlots(Canvas canvas, _BoardMetrics metrics) {
    final slotPaint = Paint()
      ..color = Colors.black.withOpacity(0.15)
      ..style = PaintingStyle.fill;
    
    final borderPaint = Paint()
      ..color = Colors.white.withOpacity(0.05)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.0;

    for (int x = 0; x < level.width; x++) {
      for (int y = 0; y < level.height; y++) {
        final rect = Rect.fromLTWH(
          x * metrics.cellWidth + 4,
          y * metrics.cellHeight + 4,
          metrics.cellWidth - 8,
          metrics.cellHeight - 8,
        );
        final rRect = RRect.fromRectAndRadius(rect, Radius.circular(metrics.cornerRadius));
        canvas.drawRRect(rRect, slotPaint);
        canvas.drawRRect(rRect, borderPaint);
      }
    }
  }

  void _drawGuidanceLines(Canvas canvas, _BoardMetrics metrics, Size size, double leftOffset, double topOffset) {
    for (final snake in level.snakes) {
      if (removalProgress.containsKey(snake.id) || entryProgress.containsKey(snake.id)) {
        continue;
      }

      final head = snake.body.first;
      final headCx = head.x * metrics.cellWidth + metrics.cellWidth / 2;
      final headCy = head.y * metrics.cellHeight + metrics.cellHeight / 2;

      Offset fullEndPoint;
      switch (snake.headDirection) {
        case game_dir.Direction.up:
          fullEndPoint = Offset(headCx, -topOffset);
          break;
        case game_dir.Direction.down:
          fullEndPoint = Offset(headCx, metrics.boardHeight + (size.height - metrics.boardHeight - topOffset));
          break;
        case game_dir.Direction.left:
          fullEndPoint = Offset(-leftOffset, headCy);
          break;
        case game_dir.Direction.right:
          fullEndPoint = Offset(metrics.boardWidth + (size.width - metrics.boardWidth - leftOffset), headCy);
          break;
      }

      final endPoint = Offset(
        headCx + (fullEndPoint.dx - headCx) * guidanceAlpha,
        headCy + (fullEndPoint.dy - headCy) * guidanceAlpha,
      );

      final alphaFactor = GameConstants.guidanceLineAlphaFactor * guidanceAlpha;
      final paint = Paint()
        ..color = themeColors.accent.withOpacity(alphaFactor)
        ..strokeWidth = 2.0
        ..strokeCap = StrokeCap.round;

      // Note: Full dash path logic requires a custom path, but to keep performance high and dependencies low
      // we'll draw a solid line since Flutter core canvas doesn't supply dashed path easily
      // A dashed approximation can be done manually if essential, but solid line is typically fine for now
      canvas.drawLine(Offset(headCx, headCy), endPoint, paint);
    }
  }

  void _drawBoardSurface(Canvas canvas, double leftOffset, double topOffset, _BoardMetrics metrics) {
    const padding = 20.0;
    const radius = 24.0;
    final boardRect = Rect.fromLTWH(
      leftOffset - padding,
      topOffset - padding,
      metrics.boardWidth + padding * 2,
      metrics.boardHeight + padding * 2,
    );

    // 1. Bottom shadow/3D edge
    final edgePaint = Paint()..color = const Color(0xFF0A120A);
    canvas.drawRRect(
      RRect.fromRectAndRadius(boardRect.shift(const Offset(0, 12)), const Radius.circular(radius)),
      edgePaint,
    );

    // 2. Main board body (Emerald felt)
    final boardPaint = Paint()
      ..shader = LinearGradient(
        begin: Alignment.topLeft,
        end: Alignment.bottomRight,
        colors: [
          themeColors.background.withBlue(40).withGreen(60),
          themeColors.background,
        ],
      ).createShader(boardRect);
    
    canvas.drawRRect(RRect.fromRectAndRadius(boardRect, const Radius.circular(radius)), boardPaint);

    // 3. Subtle inner glow
    final glowPaint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.0
      ..color = Colors.white.withOpacity(0.1);
    canvas.drawRRect(RRect.fromRectAndRadius(boardRect.deflate(2), const Radius.circular(radius - 2)), glowPaint);
  }

  void _drawSnakes(Canvas canvas, _BoardMetrics metrics) {
    for (final snake in level.snakes) {
      final removalP = (removalProgress[snake.id] ?? 0.0).clamp(0.0, 1.0);
      final entryP = entryProgress[snake.id];

      double p;
      double shift;
      double alpha;

      if (entryP != null) {
        p = 1.0 - entryP.clamp(0.0, 1.0);
        shift = 0.0;
        alpha = min(entryP * 2.5, 1.0);
      } else {
        p = removalP;
        shift = metrics.moveDist * p;
        alpha = 1.0 - p;
      }

      final isFlashing = snake.id == flashingSnakeId;
      final baseColor = isFlashing ? themeColors.flashingRed : themeColors.snake;
      final animatedAlpha = isFlashing ? alpha * flashPulseAlpha : alpha;
      final snakeColor = baseColor.withOpacity(animatedAlpha);



      final head = snake.body.first;
      final headCx0 = head.x * metrics.cellWidth + metrics.cellWidth / 2;
      final headCy0 = head.y * metrics.cellHeight + metrics.cellHeight / 2;

      final headCx = headCx0 + snake.headDirection.dx * shift;
      final headCy = headCy0 + snake.headDirection.dy * shift;

      final lineEndX = headCx + snake.headDirection.dx * metrics.cornerRadius;
      final lineEndY = headCy + snake.headDirection.dy * metrics.cornerRadius;

      final baseLineEndX0 = headCx0 + snake.headDirection.dx * metrics.cornerRadius;
      final baseLineEndY0 = headCy0 + snake.headDirection.dy * metrics.cornerRadius;

      // Draw Shadow First
      final shadowPaint = Paint()
        ..color = Colors.black.withOpacity(0.4 * alpha)
        ..style = PaintingStyle.stroke
        ..strokeWidth = metrics.strokeWidth
        ..strokeCap = StrokeCap.round
        ..strokeJoin = StrokeJoin.round
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 4.0);
      
      canvas.save();
      canvas.translate(4, 4); // Shadow offset
      if (snake.body.length > 1) {
        _drawSnakeBodyInternal(canvas, snake, p, metrics, headCx0, headCy0, baseLineEndX0, baseLineEndY0, lineEndX, lineEndY, shadowPaint);
      } else {
        _drawSingleBlockSnakeTail(canvas, snake, metrics, lineEndX, lineEndY, shadowPaint);
      }
      canvas.restore();

      // Main Snake Body with Gradient
      final snakePaint = Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = metrics.strokeWidth
        ..strokeCap = StrokeCap.round
        ..strokeJoin = StrokeJoin.round;

      // Create a gradient for the snake
      final snakeRect = Rect.fromLTWH(
        headCx - metrics.cellWidth * 2, 
        headCy - metrics.cellHeight * 2, 
        metrics.cellWidth * 4, 
        metrics.cellHeight * 4
      );
      
      snakePaint.shader = LinearGradient(
        colors: [
          snakeColor,
          snakeColor.withBlue(max(0, snakeColor.blue - 40)).withGreen(max(0, snakeColor.green - 30)),
        ],
      ).createShader(snakeRect);

      if (snake.body.length > 1) {
        _drawSnakeBodyInternal(
          canvas,
          snake,
          p,
          metrics,
          headCx0,
          headCy0,
          baseLineEndX0,
          baseLineEndY0,
          lineEndX,
          lineEndY,
          snakePaint,
        );
        
        // Gloss Highlight
        final glossPaint = Paint()
          ..color = Colors.white.withOpacity(0.15 * alpha)
          ..style = PaintingStyle.stroke
          ..strokeWidth = metrics.strokeWidth * 0.3
          ..strokeCap = StrokeCap.round;
        _drawSnakeBodyInternal(canvas, snake, p, metrics, headCx0, headCy0, baseLineEndX0, baseLineEndY0, lineEndX, lineEndY, glossPaint);
      } else {
        _drawSingleBlockSnakeTail(
          canvas,
          snake,
          metrics,
          lineEndX,
          lineEndY,
          snakePaint,
        );
      }

      if (entryP == null) {
        final triangleCenterX =
            lineEndX + snake.headDirection.dx * (metrics.arrowHeadSize * GameConstants.arrowHeadCenterFactor);
        final triangleCenterY =
            lineEndY + snake.headDirection.dy * (metrics.arrowHeadSize * GameConstants.arrowHeadCenterFactor);

        _drawArrowHead(
          canvas,
          triangleCenterX,
          triangleCenterY,
          snake.headDirection,
          metrics.arrowHeadSize,
          snakeColor,
        );

        // Draw special indicators
        if (snake.type != SnakeType.normal) {
          _drawSpecialIndicator(
            canvas,
            metrics,
            snake,
            headCx,
            headCy,
            snakeColor,
            alpha,
          );
        }
      }
    }
  }

  void _drawSpecialIndicator(
    Canvas canvas,
    _BoardMetrics metrics,
    Snake snake,
    double headCx,
    double headCy,
    Color snakeColor,
    double alpha,
  ) {
    final center = Offset(headCx, headCy);
    final unit = metrics.cellWidth;

    switch (snake.type) {
      case SnakeType.bomb:
        // 1. Outer Glow
        final glowPaint = Paint()
          ..color = Colors.red.withOpacity(0.3 * alpha)
          ..maskFilter = MaskFilter.blur(BlurStyle.normal, unit * 0.15);
        canvas.drawCircle(center, unit * 0.3, glowPaint);

        // 2. Bomb Body
        final bombPaint = Paint()
          ..shader = RadialGradient(
            colors: [const Color(0xFFFF5252), const Color(0xFFB71C1C)],
          ).createShader(Rect.fromCircle(center: center, radius: unit * 0.25));
        canvas.drawCircle(center, unit * 0.22, bombPaint);

        // 3. Countdown Text
        final textPainter = TextPainter(
          text: TextSpan(
            text: '${snake.bombTimer}',
            style: TextStyle(
              color: Colors.white.withOpacity(alpha),
              fontSize: unit * 0.28,
              fontWeight: FontWeight.w900,
              shadows: [Shadow(color: Colors.black54, offset: Offset(0, 2), blurRadius: 4)],
            ),
          ),
          textDirection: TextDirection.ltr,
        );
        textPainter.layout();
        textPainter.paint(canvas, center - Offset(textPainter.width / 2, textPainter.height / 2));
        break;

      case SnakeType.locked:
        // 1. Lock Body
        final bodyRect = Rect.fromCenter(center: center.translate(0, unit * 0.05), width: unit * 0.35, height: unit * 0.28);
        final bodyPaint = Paint()
          ..shader = const LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFFE0E0E0), Color(0xFF757575), Color(0xFF424242)],
          ).createShader(bodyRect);
        canvas.drawRRect(RRect.fromRectAndRadius(bodyRect, Radius.circular(unit * 0.05)), bodyPaint);

        // 2. Shackle
        final shacklePaint = Paint()
          ..color = const Color(0xFFBDBDBD).withOpacity(alpha)
          ..style = PaintingStyle.stroke
          ..strokeWidth = unit * 0.06
          ..strokeCap = StrokeCap.round;
        final shackleRect = Rect.fromCenter(center: center.translate(0, -unit * 0.05), width: unit * 0.22, height: unit * 0.25);
        canvas.drawArc(shackleRect, pi, pi, false, shacklePaint);

        // 3. Keyhole
        final keyholePaint = Paint()..color = Colors.black.withOpacity(0.7 * alpha);
        canvas.drawCircle(center.translate(0, unit * 0.05), unit * 0.04, keyholePaint);
        break;

      case SnakeType.key:
        // Golden Metallic Gradient
        final keyPaint = Paint()
          ..shader = const LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFFFFD54F), Color(0xFFF9A825), Color(0xFFFFD54F)],
          ).createShader(Rect.fromCircle(center: center, radius: unit * 0.3));

        // 1. Key Handle (Ring)
        canvas.drawCircle(center.translate(-unit * 0.15, 0), unit * 0.12, keyPaint);
        canvas.drawCircle(center.translate(-unit * 0.15, 0), unit * 0.06, Paint()..blendMode = BlendMode.clear);

        // 2. Key Stem
        canvas.drawRRect(
          RRect.fromRectAndRadius(
            Rect.fromLTWH(center.dx - unit * 0.05, center.dy - unit * 0.03, unit * 0.35, unit * 0.06),
            Radius.circular(unit * 0.03),
          ),
          keyPaint,
        );

        // 3. Key Bits
        canvas.drawRect(Rect.fromLTWH(center.dx + unit * 0.15, center.dy, unit * 0.05, unit * 0.1), keyPaint);
        canvas.drawRect(Rect.fromLTWH(center.dx + unit * 0.25, center.dy, unit * 0.05, unit * 0.07), keyPaint);
        break;
      default:
        break;
    }
  }

  void _drawSnakeBodyInternal(
    Canvas canvas,
    Snake snake,
    double p,
    _BoardMetrics metrics,
    double headCx0,
    double headCy0,
    double baseLineEndX0,
    double baseLineEndY0,
    double lineEndX,
    double lineEndY,
    Paint paint,
  ) {
    final path = Path();
    final body = snake.body;

    final tailPosition = (body.length - 1).toDouble() * (1.0 - p);
    final tailFullIndex = tailPosition.toInt().clamp(0, body.length - 1);
    final tailFraction = tailPosition - tailFullIndex;

    final tailTip = _calculateTailTipOffset(body, tailFullIndex, tailFraction, metrics);
    path.moveTo(tailTip.dx, tailTip.dy);

    final loopStart = (tailFraction > 0.001 && tailFullIndex > 0 && tailFullIndex < body.length - 1)
        ? tailFullIndex
        : tailFullIndex - 1;

    for (var i = loopStart; i >= 1; i--) {
      final prev = body[i + 1];
      final current = body[i];
      final next = body[i - 1];

      final currX = current.x * metrics.cellWidth + metrics.cellWidth / 2;
      final currY = current.y * metrics.cellHeight + metrics.cellHeight / 2;

      final entryX = currX + (prev.x - current.x).clamp(-1, 1) * metrics.cornerRadius;
      final entryY = currY + (prev.y - current.y).clamp(-1, 1) * metrics.cornerRadius;

      final exitX = currX + (next.x - current.x).clamp(-1, 1) * metrics.cornerRadius;
      final exitY = currY + (next.y - current.y).clamp(-1, 1) * metrics.cornerRadius;

      path.lineTo(entryX, entryY);
      path.quadraticBezierTo(currX, currY, exitX, exitY);
    }

    final head = body[0];
    final prev = body[1];
    final headEntryX = headCx0 + (prev.x - head.x).clamp(-1, 1) * metrics.cornerRadius;
    final headEntryY = headCy0 + (prev.y - head.y).clamp(-1, 1) * metrics.cornerRadius;

    path.lineTo(headEntryX, headEntryY);
    path.quadraticBezierTo(headCx0, headCy0, baseLineEndX0, baseLineEndY0);

    if (lineEndX != baseLineEndX0 || lineEndY != baseLineEndY0) {
      path.lineTo(lineEndX, lineEndY);
    }

    canvas.drawPath(path, paint);
  }

  Offset _calculateTailTipOffset(
    List<BoardPoint> body,
    int tailFullIndex,
    double tailFraction,
    _BoardMetrics metrics,
  ) {
    if (tailFraction < 0.001 || tailFullIndex >= body.length - 1) {
      final cell = body[tailFullIndex];
      return Offset(
        cell.x * metrics.cellWidth + metrics.cellWidth / 2,
        cell.y * metrics.cellHeight + metrics.cellHeight / 2,
      );
    } else {
      final fromCell = body[tailFullIndex];
      final toCell = body[tailFullIndex + 1];
      final fromCx = fromCell.x * metrics.cellWidth + metrics.cellWidth / 2;
      final fromCy = fromCell.y * metrics.cellHeight + metrics.cellHeight / 2;
      final toCx = toCell.x * metrics.cellWidth + metrics.cellWidth / 2;
      final toCy = toCell.y * metrics.cellHeight + metrics.cellHeight / 2;
      return Offset(
        fromCx + (toCx - fromCx) * tailFraction,
        fromCy + (toCy - fromCy) * tailFraction,
      );
    }
  }

  void _drawSingleBlockSnakeTail(
    Canvas canvas,
    Snake snake,
    _BoardMetrics metrics,
    double lineEndX,
    double lineEndY,
    Paint paint,
  ) {
    final tailLength = metrics.cellWidth * GameConstants.singleBlockTailFactor;
    final tailStartX = lineEndX - snake.headDirection.dx * (tailLength + metrics.cornerRadius);
    final tailStartY = lineEndY - snake.headDirection.dy * (tailLength + metrics.cornerRadius);

    canvas.drawLine(
      Offset(tailStartX, tailStartY),
      Offset(lineEndX, lineEndY),
      paint,
    );
  }

  void _drawArrowHead(
    Canvas canvas,
    double centerX,
    double centerY,
    game_dir.Direction direction,
    double arrowHeadSize,
    Color color,
  ) {
    final angleDeg = switch (direction) {
      game_dir.Direction.up => GameConstants.angleUp,
      game_dir.Direction.down => GameConstants.angleDown,
      game_dir.Direction.left => GameConstants.angleLeft,
      game_dir.Direction.right => GameConstants.angleRight,
    };
    final angle = angleDeg * GameConstants.degToRad;

    final path = Path();
    path.moveTo(
      centerX + arrowHeadSize * cos(angle),
      centerY + arrowHeadSize * sin(angle),
    );
    path.lineTo(
      centerX + arrowHeadSize * cos(angle + GameConstants.angleTriangleOffset),
      centerY + arrowHeadSize * sin(angle + GameConstants.angleTriangleOffset),
    );
    path.lineTo(
      centerX + arrowHeadSize * cos(angle - GameConstants.angleTriangleOffset),
      centerY + arrowHeadSize * sin(angle - GameConstants.angleTriangleOffset),
    );
    path.close();

    final fillPaint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;

    final strokePaint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = arrowHeadSize * GameConstants.arrowHeadStrokeWidthFactor
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;

    canvas.drawPath(path, fillPaint);
    canvas.drawPath(path, strokePaint);
  }

  @override
  bool shouldRepaint(covariant ArrowsBoardPainter oldDelegate) {
    return oldDelegate.level != level ||
        oldDelegate.flashingSnakeId != flashingSnakeId ||
        oldDelegate.flashPulseAlpha != flashPulseAlpha ||
        oldDelegate.removalProgress != removalProgress ||
        oldDelegate.entryProgress != entryProgress ||
        oldDelegate.guidanceAlpha != guidanceAlpha ||
        oldDelegate.themeColors != themeColors;
  }
}

class _BoardMetrics {
  final double cellWidth;
  final double cellHeight;
  final double strokeWidth;
  final double cornerRadius;
  final double arrowHeadSize;
  final double moveDist;
  final double boardWidth;
  final double boardHeight;

  _BoardMetrics({
    required this.cellWidth,
    required this.cellHeight,
    required this.strokeWidth,
    required this.cornerRadius,
    required this.arrowHeadSize,
    required this.moveDist,
    required this.boardWidth,
    required this.boardHeight,
  });
}
