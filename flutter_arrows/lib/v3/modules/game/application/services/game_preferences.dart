import 'package:shared_preferences/shared_preferences.dart';

/// Centralized persistence for game settings and progress
class GamePreferences {
  static const String _keyLevel = 'game_level';
  static const String _keyScore = 'game_score';
  static const String _keyTheme = 'game_theme';
  static const String _keyVibration = 'setting_vibration';
  static const String _keySound = 'setting_sound';
  static const String _keyCoins = 'game_coins';
  static const String _keyHighScore = 'game_high_score';
  static const String _keyLevelStars = 'level_stars_'; // + level number
  static const String _keyDailyDate = 'daily_challenge_date';
  static const String _keyDailyCompleted = 'daily_challenge_completed';

  late SharedPreferences _prefs;

  Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
  }

  // === LEVEL & SCORE ===
  int get currentLevel => _prefs.getInt(_keyLevel) ?? 1;
  set currentLevel(int v) => _prefs.setInt(_keyLevel, v);

  int get score => _prefs.getInt(_keyScore) ?? 0;
  set score(int v) => _prefs.setInt(_keyScore, v);

  int get highScore => _prefs.getInt(_keyHighScore) ?? 0;
  set highScore(int v) => _prefs.setInt(_keyHighScore, v);

  // === COINS ===
  int get coins => _prefs.getInt(_keyCoins) ?? 0;
  set coins(int v) => _prefs.setInt(_keyCoins, v);

  void addCoins(int amount) {
    coins = coins + amount;
  }

  bool spendCoins(int amount) {
    if (coins >= amount) {
      coins = coins - amount;
      return true;
    }
    return false;
  }

  // === SETTINGS ===
  bool get vibrationEnabled => _prefs.getBool(_keyVibration) ?? true;
  set vibrationEnabled(bool v) => _prefs.setBool(_keyVibration, v);

  bool get soundEnabled => _prefs.getBool(_keySound) ?? true;
  set soundEnabled(bool v) => _prefs.setBool(_keySound, v);

  // === THEME ===
  int get themeIndex => _prefs.getInt(_keyTheme) ?? 0;
  set themeIndex(int v) => _prefs.setInt(_keyTheme, v);

  // === STAR RATINGS ===
  int getStarsForLevel(int level) {
    return _prefs.getInt('$_keyLevelStars$level') ?? 0;
  }

  void setStarsForLevel(int level, int stars) {
    final current = getStarsForLevel(level);
    if (stars > current) {
      _prefs.setInt('$_keyLevelStars$level', stars);
    }
  }

  int get maxUnlockedLevel {
    int maxLevel = 1;
    for (int i = 1; i <= 200; i++) {
      if (getStarsForLevel(i) > 0) {
        maxLevel = i + 1;
      } else {
        break;
      }
    }
    return maxLevel;
  }

  // === DAILY CHALLENGE ===
  String get dailyChallengeDate => _prefs.getString(_keyDailyDate) ?? '';
  set dailyChallengeDate(String v) => _prefs.setString(_keyDailyDate, v);

  bool get dailyChallengeCompleted => _prefs.getBool(_keyDailyCompleted) ?? false;
  set dailyChallengeCompleted(bool v) => _prefs.setBool(_keyDailyCompleted, v);

  // === TUTORIAL STATES ===
  bool get hasSeenBombIntro => _prefs.getBool('seen_bomb_intro') ?? false;
  set hasSeenBombIntro(bool v) => _prefs.setBool('seen_bomb_intro', v);

  bool get hasSeenLockIntro => _prefs.getBool('seen_lock_intro') ?? false;
  set hasSeenLockIntro(bool v) => _prefs.setBool('seen_lock_intro', v);
}
