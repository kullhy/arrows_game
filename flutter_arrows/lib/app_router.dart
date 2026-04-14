import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import 'v3/modules/game/presentation/screens/game_screen.dart';
import 'v3/modules/game/presentation/screens/generate_screen.dart';
import 'v3/modules/home/presentation/screens/home_screen.dart';
import 'v3/modules/home/presentation/screens/level_select_screen.dart';
import 'v3/modules/home/presentation/screens/main_screen.dart';
import 'v3/modules/settings/presentation/screens/settings_screen.dart';

final appRouter = GoRouter(
  initialLocation: '/home',
  routes: [
    StatefulShellRoute.indexedStack(
      builder: (context, state, navigationShell) {
        return MainScreen(navigationShell: navigationShell);
      },
      branches: [
        StatefulShellBranch(
          routes: [
            GoRoute(
              path: '/generate',
              builder: (context, state) => const GenerateScreen(),
            ),
          ],
        ),
        StatefulShellBranch(
          routes: [
            GoRoute(
              path: '/home',
              builder: (context, state) => const HomeScreen(),
              routes: [
                GoRoute(
                  path: 'levels',
                  builder: (context, state) => const LevelSelectScreen(),
                ),
              ],
            ),
          ],
        ),
        StatefulShellBranch(
          routes: [
            GoRoute(
              path: '/settings',
              builder: (context, state) => const SettingsScreen(),
            ),
          ],
        ),
      ],
    ),
    GoRoute(
      path: '/game',
      builder: (context, state) => const GameScreen(),
    ),
  ],
);
