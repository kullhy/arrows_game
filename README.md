# arrows_game

**Arrows Game** is an Android puzzle game inspired by "Arrows – Puzzle Escape".

## Project Rationale
This project is an experiment in board generation: the main goal is to create a robust board generator for puzzle games, focusing on solvability and variety. The generator logic attempts to fill the board with "snakes" (arrow paths) while ensuring each level is solvable and interesting.

## Features
- Procedural board generation with customizable shapes and sizes
- Solvability checker to ensure every generated board can be completed
- Multiple board shapes via `BoardShapeProvider`
- Dynamic level progression and difficulty scaling
- Touch-based gameplay with animated feedback
- User preferences for vibration, sound, and animation speed
- Debug and custom game modes

## Board Generator
The board generator (`GameGenerator`) is the heart of the project. It:
- Builds levels by placing snakes (arrow paths) on the board
- Fills the board while respecting shape boundaries and walls
- Ensures each board is solvable
- Supports custom shapes and fill-the-board modes

## Screenshots
![Screenshot 1](Screenshot_20260206_114049.png) ![Screenshot 2](Screenshot_20260206_114104.png) ![Screenshot 3](Screenshot_20260206_114121.png) ![Screenshot 4](Screenshot_20260206_114249.png)

## File Structure
- `app/src/main/java/com/batodev/arrows/engine/GameGenerator.kt` – Board generator logic
- `app/src/main/java/com/batodev/arrows/engine/BoardShapeProvider.kt` – Shape management
- `app/src/main/java/com/batodev/arrows/engine/GameEngine.kt` – Game state and mechanics

---
This project is a playground for board generation algorithms. Feedback and contributions are welcome!
