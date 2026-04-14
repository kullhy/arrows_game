import 'package:audioplayers/audioplayers.dart';
import 'game_preferences.dart';

class SoundService {
  final AudioPlayer _player = AudioPlayer();
  final GamePreferences _prefs;

  SoundService(this._prefs);

  Future<void> _playSound(String path) async {
    if (!_prefs.soundEnabled) return;
    try {
      await _player.play(AssetSource(path), mode: PlayerMode.lowLatency);
    } catch (e) {
      // Asset might be missing, ignore to prevent crash
      print('SoundService: Could not play $path. Ensure asset exists.');
    }
  }

  Future<void> playCorrect() async => _playSound('sounds/correct.mp3');
  Future<void> playWrong() async => _playSound('sounds/wrong.mp3');
  Future<void> playWin() async => _playSound('sounds/win.mp3');
  Future<void> playClick() async => _playSound('sounds/click.mp3');

  void dispose() {
    _player.dispose();
  }
}
