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
    int? bombTimer,
  }) {
    return Snake(
      id: id,
      body: body,
      headDirection: headDirection,
      type: type,
      bombTimer: bombTimer ?? this.bombTimer,
      lockParentId: lockParentId,
    );
  }
}
