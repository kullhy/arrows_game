# Arrows game

**Arrows Game** is an Android puzzle game inspired by "Arrows – Puzzle Escape".

Google play link to the game: https://play.google.com/store/apps/details?id=com.batodev.arrows

## Project Rationale
This project is an experiment in board generation: the main goal is to create a robust board generator for puzzle games, focusing on solvability and variety. The generator logic attempts to fill the board with "snakes" (arrow paths) while ensuring each level is solvable and interesting.

## Features

### Gameplay
- Procedural board generation with customizable shapes and sizes
- Solvability checker to ensure every generated board can be completed
- Multiple board shapes via `BoardShapeProvider`
- Dynamic level progression and difficulty scaling
- Touch-based gameplay with animated feedback
- Lives system with game-over recovery via rewarded ads
- Hint system (free when ad-free, otherwise rewarded-ad-gated)
- Pinch-to-zoom and pan on the game board
- Toggleable guidance lines overlay to help aim

### Win Celebration
- Full-screen celebration with fade-in/out video playback (26 videos)
- 10 randomized congratulatory messages
- Confetti particle animation

### Custom Level Generator
- Unlocked at level 20
- Adjustable board width and height
- Shape selection (rectangular or custom shapes)
- Fill-the-board mode for denser puzzles

### Settings & Customization
- 6 color themes: Dark, Green, Red, Yellow, Orange, Black and White
- 3 animation speed levels: High, Medium, Low
- Vibration and sound toggles
- Fill board toggle

### Monetization
- Banner, rewarded, and interstitial ads
- Ad-free unlock by watching 30 rewarded ads (with progress bar)

### Localization
- 15 languages: English, Arabic, Bengali, German, Spanish, French, Hindi, Indonesian, Italian, Japanese, Polish, Portuguese, Russian, Urdu, Chinese

### Feedback & Legal
- In-app review (Rate Us), email support (Write Us), developer store link (More Games)
- Privacy policy link and third-party licenses dialog

### Debug
- Debug menu to force board dimensions, lives, and shapes

## Board Generator
The board generator (`GameGenerator`) is the heart of the project. It:
- Builds levels by placing snakes (arrow paths) on the board
- Fills the board while respecting shape boundaries and walls
- Ensures each board is solvable
- Supports custom shapes and fill-the-board modes

## Screenshots
![Screenshot 1](Screenshot_20260206_114049.png) ![Screenshot 2](Screenshot_20260206_114104.png) ![Screenshot 3](Screenshot_20260206_114121.png) ![Screenshot 4](Screenshot_20260206_114249.png)

## Navigation with Appyx

The app uses [Appyx](https://bumble-tech.github.io/appyx/) (v1.7.1) for navigation instead of the standard Compose Navigation library.

### Why Appyx

Appyx is a **model-driven navigation** library for Jetpack Compose. Rather than routing between destinations with string routes or `NavController`, navigation state is a first-class Kotlin object — a typed, observable model that can be tested like any other class.

Key reasons for choosing it here:

- **Type-safe destinations** — `NavTarget` is a sealed class; the compiler catches invalid routes.
- **Testable navigation logic** — Back-stack operations (`push`, `pop`, `replace`, `newRoot`) are pure state changes that can be asserted in unit tests without a UI (see `BackStackNavigationTest`).
- **Custom transitions in one place** — The `transitionHandler` parameter of `Children` makes it trivial to plug in arbitrary Compose `Modifier` animations; the random transition system was built without touching any screen composable.
- **Structured concurrency-friendly** — Each screen lives in a `Node` that has its own lifecycle and coroutine scope, so background work (ads, game engine) is automatically cancelled when the screen leaves the stack.

### How it is structured

```
RootNode                    ← ParentNode, owns the BackStack<NavTarget>
 ├── HomeNode               ← leaf Node, renders HomeScreen
 ├── GameNode               ← leaf Node, renders GameScreen
 ├── GenerateNode           ← leaf Node, renders GenerateScreen
 └── SettingsNode           ← leaf Node, renders SettingsScreen
```

`RootNode` resolves each `NavTarget` value into the appropriate `Node` and renders all active children through the Appyx `Children` composable. The `BackStack` nav model handles the history so hardware back automatically pops the stack.

### Navigation destinations (`NavTarget`)

| Target                | Description                                     |
|-----------------------|-------------------------------------------------|
| `NavTarget.Home`      | Landing screen                                  |
| `NavTarget.Game(...)` | Gameplay — carries optional custom board params |
| `NavTarget.Generate`  | Custom level builder (unlocked at level 20)     |
| `NavTarget.Settings`  | Theme, sound, ad preferences                    |

### Random view transitions

Navigation changes play one of five `Modifier`-based transitions chosen at random:

| Type               | Effect                      |
|--------------------|-----------------------------|
| `FADE`             | Alpha 0 → 1                 |
| `SLIDE_HORIZONTAL` | Slides in from the right    |
| `SLIDE_VERTICAL`   | Slides in from the bottom   |
| `SCALE_FADE`       | Scales 0.85 → 1 with fade   |
| `ROTATE_FADE`      | Slight Z-rotation with fade |

`rememberRandomTransitionHandler()` (in `navigation/transitions/`) wires this into `RootNode`. Each screen entering the composition picks its own type via `remember { picker.pick() }`, keeping the Compose runtime in full control of animation state.

## File Structure
- `app/src/main/java/com/batodev/arrows/engine/GameGenerator.kt` – Board generator logic
- `app/src/main/java/com/batodev/arrows/engine/BoardShapeProvider.kt` – Shape management
- `app/src/main/java/com/batodev/arrows/engine/GameEngine.kt` – Game state and mechanics

---
This project is a playground for board generation algorithms. Feedback and contributions are welcome!
