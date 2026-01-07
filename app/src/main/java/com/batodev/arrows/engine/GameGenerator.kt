package com.batodev.arrows.engine

import kotlin.random.Random

// --- Data Models ---

data class Point(val x: Int, val y: Int) {
    operator fun plus(dir: Direction) = Point(x + dir.dx, y + dir.dy)
}

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0)
}

data class Snake(
    val id: Int,
    val body: List<Point>, // Ordered list: Tail -> ... -> Head
    val headDirection: Direction
)

data class GameLevel(
    val width: Int,
    val height: Int,
    val snakes: List<Snake>
)

class GameEngine {

    /**
     * Generates a guaranteed solvable Arrows puzzle.
     *
     * Algorithm, assume 10x10 board:
     *
     * Definitions:
     * Reachable field - a field that can be reached in a straight line from the edge of the board
     * without hitting a snake.
     * Arrowhead - the head of the snake, pointing to a direction: up, down, left, right.
     *
     *
     * 1. Gather all rows and columns that have at least one field free and reachable from the edge.
     * 2. Pick one row or column at random. Pick one reachable field in that row or column at random.
     * 3. Put an arrowhead pointing to the edge. The line of sight must be clear of snakes.
     * 4. Put in a random snake body, but mind that it cannot obstruct it's own heads line of sight.
     * 5. Repeat until the board is full.
     *
     * @param width Width of the grid (e.g., 7)
     * @param height Height of the grid (e.g., 10)
     */
    fun generateSolvableLevel(width: Int, height: Int, maxSnakeLength: Int): GameLevel {

        val snakes = mutableListOf<Snake>()
        return GameLevel(width, height, snakes)
    }
}
