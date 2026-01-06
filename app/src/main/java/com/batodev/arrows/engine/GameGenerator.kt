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

// --- The Engine ---

class GameEngine {

    /**
     * Generates a guaranteed solvable Arrows puzzle.
     * * @param width Width of the grid (e.g., 7)
     * @param height Height of the grid (e.g., 10)
     * @param fillDensity Target percentage of grid to fill (0.0 to 1.0)
     */
    fun generateSolvableLevel(width: Int, height: Int, fillDensity: Double = 0.95): GameLevel {
        while (true) {
            val grid = Array(width) { IntArray(height) } // 0 = empty, >0 = snake ID
            val snakes = mutableListOf<Snake>()
            var nextId = 1

            // Try to fill the board until we hit density or run out of attempts
            var failures = 0
            val maxFailures = 200 // Increased maxFailures to allow more attempts
            val targetFilledCells = (width * height * fillDensity).toInt()
            var filledCells = 0

            while (filledCells < targetFilledCells && failures < maxFailures) {
                // 1. Pick a random empty start point
                val startPoint = findRandomEmptyCell(width, height, grid) ?: break
                // Board is full

                // 2. Grow a random snake shape
                val candidateBody = growRandomSnake(startPoint, width, height, grid)

                // 3. Determine direction (align with last segment)
                val head = candidateBody.last()
                val direction = if (candidateBody.size > 1) {
                    val preHead = candidateBody[candidateBody.size - 2]
                    getDirection(preHead, head)
                } else {
                    Direction.entries.toTypedArray().random()
                }

                val candidateSnake = Snake(nextId, candidateBody, direction)

                // 4. Critical: Check if adding this snake creates a Deadlock (Cycle)
                if (isSafeToAdd(candidateSnake, snakes, grid, width, height)) {
                    // Success: Commit to board
                    snakes.add(candidateSnake)
                    for (p in candidateBody) {
                        grid[p.x][p.y] = nextId
                    }
                    filledCells += candidateBody.size
                    nextId++
                    failures = 0 // Reset failures on success
                } else {
                    failures++
                }
            }

            // If we are aiming for a full board but failed, retry the entire generation.
            if (fillDensity == 1.0 && filledCells < targetFilledCells) {
                println("Generation failed to fill the board completely, retrying...")
                continue
            }

            // If we've reached this point, generation is successful.
            return GameLevel(width, height, snakes)
        }
    }

    /**
     * Checks if adding [newSnake] creates a dependency cycle in the graph.
     * Returns true if the board remains solvable (DAG).
     * Uses full line-of-sight checking: traces the entire path from head to board edge.
     */
    private fun isSafeToAdd(
        newSnake: Snake,
        existingSnakes: List<Snake>,
        grid: Array<IntArray>,
        w: Int, h: Int
    ): Boolean {
        // Create a set of points occupied by the new snake for quick lookup
        val newSnakeBody = newSnake.body.toSet()

        // First, check if the new snake blocks itself (body in its own line of sight)
        if (isSnakeSelfBlocked(newSnake, newSnakeBody, w, h)) {
            return false
        }

        // Check if any existing snake blocks itself (shouldn't happen, but defensive)
        for (snake in existingSnakes) {
            if (isSnakeSelfBlocked(snake, snake.body.toSet(), w, h)) {
                return false
            }
        }

        // We build a temporary dependency graph including the new snake
        // Adjacency list: Node ID -> List of IDs it depends on (blocks it)
        val graph = mutableMapOf<Int, MutableList<Int>>()

        // Initialize graph nodes
        val allSnakes = existingSnakes + newSnake
        allSnakes.forEach { graph[it.id] = mutableListOf() }

        // Build Edges using full line-of-sight - track ALL blockers, not just the first
        for (snake in allSnakes) {
            var pos = snake.body.last()
            val dir = snake.headDirection

            // Trace the entire line of sight from head to board edge
            while (true) {
                pos = Point(pos.x + dir.dx, pos.y + dir.dy)

                // If we reach outside the board, done checking this snake
                if (pos.x !in 0 until w || pos.y !in 0 until h) {
                    break
                }

                // Check if blocked by existing snake on grid (not the new snake)
                val blockerId = grid[pos.x][pos.y]
                if (blockerId != 0 && blockerId != snake.id) {
                    // "snake" depends on "blockerId" - add ALL blockers
                    graph[snake.id]?.add(blockerId)
                    // Don't break - continue checking for more blockers
                }

                // Check if blocked by the new snake (not yet on grid)
                if (snake != newSnake && newSnakeBody.contains(pos)) {
                    graph[snake.id]?.add(newSnake.id)
                    // Don't break - continue checking for more blockers
                }
            }
        }

        // Run Cycle Detection (DFS)
        return !hasCycle(graph)
    }

    /**
     * Checks if a snake's own body is in its line of sight (self-blocking).
     */
    private fun isSnakeSelfBlocked(snake: Snake, snakeBody: Set<Point>, w: Int, h: Int): Boolean {
        var pos = snake.body.last()
        val dir = snake.headDirection

        while (true) {
            pos = Point(pos.x + dir.dx, pos.y + dir.dy)

            // If we reach outside the board, not self-blocked
            if (pos.x !in 0 until w || pos.y !in 0 until h) {
                return false
            }

            // If own body is in line of sight, self-blocked
            if (snakeBody.contains(pos)) {
                return true
            }
        }
    }

    /**
     * Standard DFS Cycle Detection.
     * Returns true if a circular dependency exists (A blocks B, B blocks A).
     */
    private fun hasCycle(graph: Map<Int, List<Int>>): Boolean {
        val visited = mutableSetOf<Int>()
        val recursionStack = mutableSetOf<Int>()

        for (node in graph.keys) {
            if (detectCycleDFS(node, graph, visited, recursionStack)) {
                return true
            }
        }
        return false
    }

    private fun detectCycleDFS(
        node: Int,
        graph: Map<Int, List<Int>>,
        visited: MutableSet<Int>,
        stack: MutableSet<Int>
    ): Boolean {
        if (stack.contains(node)) return true // Cycle detected
        if (visited.contains(node)) return false // Already processed safely

        visited.add(node)
        stack.add(node)

        val neighbors = graph[node] ?: emptyList()
        for (neighbor in neighbors) {
            if (detectCycleDFS(neighbor, graph, visited, stack)) {
                return true
            }
        }

        stack.remove(node)
        return false
    }

    // --- Helpers ---

    private fun growRandomSnake(start: Point, w: Int, h: Int, grid: Array<IntArray>): List<Point> {
        val body = mutableListOf(start)
        // Mark strictly for this generation attempt (don't write to main grid yet)
        val tempVisited = mutableSetOf(start)

        var current = start
        val maxLen = Random.nextInt(4, 12) // Random length between 2 and 5 segments

        @Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER")
        for (unused in 0 until maxLen) {
            // Find valid neighbors (inside bounds, empty in grid, not in current snake)
            val neighbors = Direction.entries
                .map { current + it }
                .filter { p ->
                    p.x in 0 until w &&
                            p.y in 0 until h &&
                            grid[p.x][p.y] == 0 &&
                            !tempVisited.contains(p)
                }

            if (neighbors.isEmpty()) break

            val next = neighbors.random()
            body.add(next)
            tempVisited.add(next)
            current = next
        }
        return body
    }

    private fun findRandomEmptyCell(w: Int, h: Int, grid: Array<IntArray>): Point? {
        val emptyCells = mutableListOf<Point>()
        for (x in 0 until w) {
            for (y in 0 until h) {
                if (grid[x][y] == 0) emptyCells.add(Point(x, y))
            }
        }
        if (emptyCells.isEmpty()) return null
        return emptyCells.random()
    }

    private fun getDirection(from: Point, to: Point): Direction {
        return when {
            to.x > from.x -> Direction.RIGHT
            to.x < from.x -> Direction.LEFT
            to.y > from.y -> Direction.DOWN
            else -> Direction.UP
        }
    }
}