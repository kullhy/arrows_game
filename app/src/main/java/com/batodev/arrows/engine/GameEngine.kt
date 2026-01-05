package com.batodev.arrows.engine

import java.util.Stack
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
        val grid = Array(width) { IntArray(height) { 0 } } // 0 = empty, >0 = snake ID
        val snakes = mutableListOf<Snake>()
        var nextId = 1

        // Try to fill the board until we hit density or run out of attempts
        var failures = 0
        val maxFailures = 200
        val targetFilledCells = (width * height * fillDensity).toInt()
        var filledCells = 0

        while (filledCells < targetFilledCells && failures < maxFailures) {
            // 1. Pick a random empty start point
            val startPoint = findRandomEmptyCell(width, height, grid)
            if (startPoint == null) break // Board is full

            // 2. Grow a random snake shape
            val candidateBody = growRandomSnake(startPoint, width, height, grid)

            // 3. Determine direction (align with last segment)
            // If snake is length 1, pick random. Otherwise, flow from 2nd-to-last to last.
            val head = candidateBody.last()
            val direction = if (candidateBody.size > 1) {
                val preHead = candidateBody[candidateBody.size - 2]
                getDirection(preHead, head)
            } else {
                Direction.values().random()
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
                failures = 0
            } else {
                failures++
            }
        }

        return GameLevel(width, height, snakes)
    }

    /**
     * Checks if adding [newSnake] creates a dependency cycle in the graph.
     * Returns true if the board remains solvable (DAG).
     */
    private fun isSafeToAdd(
        newSnake: Snake,
        existingSnakes: List<Snake>,
        grid: Array<IntArray>,
        w: Int, h: Int
    ): Boolean {
        // We build a temporary dependency graph including the new snake
        // Adjacency list: Node ID -> List of IDs it depends on (blocks it)
        val graph = mutableMapOf<Int, MutableList<Int>>()

        // Initialize graph nodes
        val allSnakes = existingSnakes + newSnake
        allSnakes.forEach { graph[it.id] = mutableListOf() }

        // Build Edges
        for (snake in allSnakes) {
            val target = snake.body.last() + snake.headDirection

            // Check bounds
            if (target.x in 0 until w && target.y in 0 until h) {
                val blockerId = grid[target.x][target.y]

                // If the target cell is occupied by ANOTHER snake, add dependency.
                // Note: If blockerId is 0 (empty), it might be filled later,
                // but for this snapshot, it's not a block.
                // Crucial: check if the newSnake ITSELF is the blocker (self-collision is impossible in this logic
                // because we only check grid, and newSnake isn't on grid yet,
                // BUT we must check if newSnake blocks an EXISTING snake).

                if (blockerId != 0 && blockerId != snake.id) {
                    // "snake" depends on "blockerId"
                    graph[snake.id]?.add(blockerId)
                }

                // Special Check: The new snake isn't on the 'grid' array yet.
                // We must check if any EXISTING snake is blocked by the NEW snake's body.
                if (snake != newSnake) {
                    // Does this existing snake point to a cell now occupied by the new snake?
                    val isBlockedByNewSnake = newSnake.body.contains(target)
                    if (isBlockedByNewSnake) {
                        graph[snake.id]?.add(newSnake.id)
                    }
                }
            }
        }

        // Run Cycle Detection (DFS)
        return !hasCycle(graph)
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
        val maxLen = Random.nextInt(2, 6) // Random length between 2 and 5 segments

        for (i in 0 until maxLen) {
            // Find valid neighbors (inside bounds, empty in grid, not in current snake)
            val neighbors = Direction.values()
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