import 'direction.dart';
import 'package:equatable/equatable.dart';

class BoardPoint extends Equatable {
  final int x;
  final int y;

  const BoardPoint(this.x, this.y);

  BoardPoint operator +(Direction dir) {
    return BoardPoint(x + dir.dx, y + dir.dy);
  }

  @override
  List<Object?> get props => [x, y];
}
