import 'package:equatable/equatable.dart';

import 'board_point.dart';
import 'direction.dart';

enum SnakeType {
  normal,
  bomb,
  locked,
  key,
}

class Snake extends Equatable {
  final int id;
  final List<BoardPoint> body; // Ordered list: Head -> ... -> Tail
  final Direction headDirection;
  final SnakeType type;
  final int bombTimer; // Rounds or seconds? Let's say moves.
  final int? lockParentId; // If locked, which key unlocks it?

  const Snake({
    required this.id,
    required this.body,
    required this.headDirection,
    this.type = SnakeType.normal,
    this.bombTimer = 0,
    this.lockParentId,
  });

  @override
  List<Object?> get props => [id, body, headDirection, type, bombTimer, lockParentId];

  Snake copyWith({
    int? id,
    List<BoardPoint>? body,
    Direction? headDirection,
    SnakeType? type,
    int? bombTimer,
    int? lockParentId,
    bool clearLockParent = false,
  }) {
    return Snake(
      id: id ?? this.id,
      body: body ?? this.body,
      headDirection: headDirection ?? this.headDirection,
      type: type ?? this.type,
      bombTimer: bombTimer ?? this.bombTimer,
      lockParentId: clearLockParent ? null : (lockParentId ?? this.lockParentId),
    );
  }
}
