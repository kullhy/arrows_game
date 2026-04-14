import 'dart:math';

import 'game_generator.dart';

class HeartShape extends BoardShape {
  @override
  List<List<bool>> getWalls(int targetWidth, int targetHeight) {
    final walls = List.generate(targetWidth, (_) => List.filled(targetHeight, true));
    
    for (int x = 0; x < targetWidth; x++) {
      for (int y = 0; y < targetHeight; y++) {
        double nx = (x / (targetWidth - 1)) * 2.4 - 1.2;
        double ny = -((y / (targetHeight - 1)) * 2.8 - 1.6); 
        
        double term1 = nx * nx + ny * ny - 1;
        double term2 = nx * nx * ny * ny * ny;
        if (term1 * term1 * term1 - term2 <= 0) {
          walls[x][y] = false;
        }
      }
    }
    _ensureFreeCells(walls, targetWidth, targetHeight);
    return walls;
  }
}

class CrossShape extends BoardShape {
  @override
  List<List<bool>> getWalls(int targetWidth, int targetHeight) {
    final walls = List.generate(targetWidth, (_) => List.filled(targetHeight, true));
    double widthThird = targetWidth / 3;
    double heightThird = targetHeight / 3;

    for (int x = 0; x < targetWidth; x++) {
      for (int y = 0; y < targetHeight; y++) {
        bool inVertical = x >= widthThird && x < widthThird * 2;
        bool inHorizontal = y >= heightThird && y < heightThird * 2;
        if (inVertical || inHorizontal) {
          walls[x][y] = false;
        }
      }
    }
    return walls;
  }
}

class CircleShape extends BoardShape {
  @override
  List<List<bool>> getWalls(int targetWidth, int targetHeight) {
    final walls = List.generate(targetWidth, (_) => List.filled(targetHeight, true));
    double cx = targetWidth / 2 - 0.5;
    double cy = targetHeight / 2 - 0.5;
    double radius = min(targetWidth, targetHeight) / 2;

    for (int x = 0; x < targetWidth; x++) {
      for (int y = 0; y < targetHeight; y++) {
        double dx = x - cx;
        double dy = y - cy;
        if (dx * dx + dy * dy <= radius * radius) {
          walls[x][y] = false;
        }
      }
    }
    return walls;
  }
}

class StarShape extends BoardShape {
  @override
  List<List<bool>> getWalls(int targetWidth, int targetHeight) {
    final walls = List.generate(targetWidth, (_) => List.filled(targetHeight, true));
    final cx = targetWidth / 2;
    final cy = targetHeight / 2;
    final outerR = min(targetWidth, targetHeight) / 2 * 0.95;
    final innerR = outerR * 0.38;
    const numPoints = 5;

    for (int x = 0; x < targetWidth; x++) {
      for (int y = 0; y < targetHeight; y++) {
        if (_isInsideStar(x.toDouble(), y.toDouble(), cx, cy, outerR, innerR, numPoints)) {
          walls[x][y] = false;
        }
      }
    }
    _ensureFreeCells(walls, targetWidth, targetHeight);
    return walls;
  }

  bool _isInsideStar(double px, double py, double cx, double cy, double outerR, double innerR, int numPoints) {
    final angle = atan2(py - cy, px - cx);
    final dist = sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy));
    
    // Normalized angle to [0, 2*pi]
    final normAngle = (angle + pi * 2.5) % (2 * pi);
    final sectionAngle = 2 * pi / numPoints;
    final halfSection = sectionAngle / 2;
    final sectionProgress = normAngle % sectionAngle;
    
    // Interpolate boundary radius
    double boundaryRadius;
    if (sectionProgress < halfSection) {
      final t = sectionProgress / halfSection;
      boundaryRadius = outerR + (innerR - outerR) * t;
    } else {
      final t = (sectionProgress - halfSection) / halfSection;
      boundaryRadius = innerR + (outerR - innerR) * t;
    }
    
    return dist <= boundaryRadius;
  }
}

class DiamondShape extends BoardShape {
  @override
  List<List<bool>> getWalls(int targetWidth, int targetHeight) {
    final walls = List.generate(targetWidth, (_) => List.filled(targetHeight, true));
    final cx = targetWidth / 2;
    final cy = targetHeight / 2;

    for (int x = 0; x < targetWidth; x++) {
      for (int y = 0; y < targetHeight; y++) {
        double dx = (x - cx).abs() / cx;
        double dy = (y - cy).abs() / cy;
        if (dx + dy <= 1.0) {
          walls[x][y] = false;
        }
      }
    }
    return walls;
  }
}

class HouseShape extends BoardShape {
  @override
  List<List<bool>> getWalls(int targetWidth, int targetHeight) {
    final walls = List.generate(targetWidth, (_) => List.filled(targetHeight, true));
    final roofHeight = targetHeight ~/ 3;
    final cx = targetWidth / 2;
    final bodyLeft = targetWidth ~/ 5;
    final bodyRight = targetWidth - bodyLeft;

    for (int x = 0; x < targetWidth; x++) {
      for (int y = 0; y < targetHeight; y++) {
        // Roof (triangle)
        if (y < roofHeight) {
          final roofWidth = (roofHeight - y) / roofHeight * (targetWidth / 2);
          if ((x - cx).abs() <= roofWidth) {
            walls[x][y] = false;
          }
        }
        // Body (rectangle)
        if (y >= roofHeight && x >= bodyLeft && x < bodyRight) {
          walls[x][y] = false;
        }
      }
    }
    _ensureFreeCells(walls, targetWidth, targetHeight);
    return walls;
  }
}

class LightningShape extends BoardShape {
  @override
  List<List<bool>> getWalls(int targetWidth, int targetHeight) {
    final walls = List.generate(targetWidth, (_) => List.filled(targetHeight, true));
    final boltWidth = targetWidth ~/ 3;

    // Lightning bolt: zigzag pattern
    for (int y = 0; y < targetHeight; y++) {
      final section = (y * 3) ~/ targetHeight; // 0, 1, or 2
      int leftX, rightX;

      switch (section) {
        case 0: // Top: left to center
          final progress = y / (targetHeight / 3);
          leftX = (targetWidth ~/ 6);
          rightX = leftX + boltWidth + (progress * boltWidth * 0.3).toInt();
          break;
        case 1: // Middle: shift right
          leftX = (targetWidth ~/ 3);
          rightX = leftX + boltWidth;
          break;
        default: // Bottom: center to right
          final progress = (y - 2 * targetHeight / 3) / (targetHeight / 3);
          leftX = (targetWidth ~/ 4) + (progress * targetWidth * 0.2).toInt();
          rightX = leftX + boltWidth;
      }

      for (int x = leftX; x < rightX && x < targetWidth; x++) {
        if (x >= 0) walls[x][y] = false;
      }
    }
    _ensureFreeCells(walls, targetWidth, targetHeight);
    return walls;
  }
}

/// Helper to ensure at least some cells are free
void _ensureFreeCells(List<List<bool>> walls, int width, int height) {
  bool hasFree = false;
  for (var col in walls) {
    if (col.contains(false)) {
      hasFree = true;
      break;
    }
  }
  if (!hasFree) walls[width ~/ 2][height ~/ 2] = false;
}
