import 'package:get_it/get_it.dart';

import '../v3/modules/game/application/services/game_generator.dart';
import '../v3/modules/game/application/services/game_preferences.dart';
import '../v3/modules/game/application/services/sound_service.dart';
import '../v3/modules/game/presentation/bloc/game_cubit.dart';
import 'infrastructure/storage/local_storage_manager.dart';

final sl = GetIt.instance;

Future<void> initDependencies() async {
  // Core / Infrastructure
  final prefs = GamePreferences();
  await prefs.init();
  sl.registerSingleton<GamePreferences>(prefs);
  
  sl.registerLazySingleton<LocalStorageManager>(() => LocalStorageManager());
  sl.registerLazySingleton<SoundService>(() => SoundService(sl<GamePreferences>()));

  // Game Module
  sl.registerLazySingleton<GameGenerator>(() => GameGenerator());
  
  // Cubits/Blocs
  sl.registerLazySingleton<GameCubit>(() => GameCubit(
    gameGenerator: sl<GameGenerator>(),
    prefs: sl<GamePreferences>(),
    soundService: sl<SoundService>(),
  ));
}
