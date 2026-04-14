class RemovalAnimator {
  final Map<int, double> removalProgress = {};
  
  // In a real Flutter implementation, this will be driven by a Ticker 
  // or mapped from Cubit to AnimationControllers recursively.
  // For standard V3 architecture, we can manage animation ticks manually or 
  // notify Cubit state changes.
  
  void animate({
    required int snakeId,
    required String speed,
    required Function(int, double) onUpdate,
    required Function(int) onComplete,
  }) {
    // Scaffold for animation
    removalProgress[snakeId] = 0.0;
    _simulateTick(snakeId, onUpdate, onComplete);
  }

  Future<void> _simulateTick(int snakeId, Function(int, double) onUpdate, Function(int) onComplete) async {
    for (var i = 1; i <= 20; i++) {
      await Future.delayed(const Duration(milliseconds: 16)); // ~60fps
      final val = i / 20.0;
      removalProgress[snakeId] = val;
      onUpdate(snakeId, val);
    }
    removalProgress.remove(snakeId);
    onComplete(snakeId);
  }

  void clear() {
    removalProgress.clear();
  }
}

class EntryAnimator {
  final Map<int, double> entryProgress = {};
  bool isEntryAnimating = false;

  void clear() {
    entryProgress.clear();
    isEntryAnimating = false;
  }

  // Similar to Removal, ideally handled via AnimationController in UI
}
