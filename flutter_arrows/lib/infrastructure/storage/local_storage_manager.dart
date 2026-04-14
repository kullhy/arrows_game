// Mocking the required LocalStorageManager pattern for V3 migration

class LocalStorageManager {
  // In reality, this wraps LocalStorageProvider + SessionManager
  // We mock it for the standalone game until attached to the real V3 core.
  final Map<String, dynamic> _memoryCache = {};

  Future<void> saveInt(String key, int value) async {
    _memoryCache[key] = value;
  }

  int getInt(String key, {int defaultValue = 0}) {
    return _memoryCache[key] as int? ?? defaultValue;
  }
  
  Future<void> saveBool(String key, bool value) async {
    _memoryCache[key] = value;
  }

  bool getBool(String key, {bool defaultValue = false}) {
    return _memoryCache[key] as bool? ?? defaultValue;
  }
}
