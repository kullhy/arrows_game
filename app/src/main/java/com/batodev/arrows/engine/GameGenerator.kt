package com.batodev.arrows.engine

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

class GameGenerator {
    private var ids = 1

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
     * 5. Repeat point 1 until possible.
     * 6. Fill in the rest of the board with random snakes.
     * @param width Width of the grid (e.g., 7)
     * @param height Height of the grid (e.g., 10)
     */
    fun generateSolvableLevel(width: Int, height: Int, maxSnakeLength: Int): GameLevel {
        val snakes = mutableListOf<Snake>()
        var freePoints = allFreePoints(width, height)
        var result = copyAndPickRandomUnobstructed(freePoints, width, height)
        while (result != null) {
            val snake = buildSnake(result, freePoints, maxSnakeLength)
            freePoints = freePoints.minus(snake.body)
            snakes.add(snake)
            result = copyAndPickRandomUnobstructed(freePoints, width, height)
        }
        return GameLevel(width, height, snakes)
    }

    /**
     * Builds a snake starting from the given head position and direction.
     * The snake body can turn and grow in any direction.
     * Ensures the snake body doesn't obstruct its own line of sight to the edge.
     *
     * @param start Pair of (head position, direction pointing to edge)
     * @param freePoints Set of free points available on the board
     * @param maxSnakeLength Maximum length of the snake
     * @return A Snake with body starting from tail to head
     */
    fun buildSnake(
        start: Pair<Point, Direction>,
        freePoints: Set<Point>,
        maxSnakeLength: Int
    ): Snake {
        val (head, headDirection) = start

        val body = mutableListOf<Point>()
        val snakePoints = mutableSetOf<Point>()
        var current = head

        body.add(current)
        snakePoints.add(current)

        // Start building the snake body from head, growing in available directions
        val targetLength = kotlin.random.Random.nextInt(2, maxSnakeLength + 1).coerceAtMost(maxSnakeLength)

        // First segment MUST be in the opposite direction from the head
        if (targetLength > 1) {
            val oppositeDirection = getOppositeDirection(headDirection)
            val firstSegment = current + oppositeDirection

            // Verify the first segment is valid
            if (firstSegment in freePoints &&
                !wouldObstructLineOfSight(firstSegment, head, headDirection)) {
                current = firstSegment
                body.add(current)
                snakePoints.add(current)
            } else {
                // Can't place first segment, return just the head
                return Snake(ids++, body, headDirection)
            }
        }

        // Now continue building with random turns
        @Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER")
        for (_step in 2 until targetLength) {
            // Find all valid neighbors for the next segment
            val validNeighbors = mutableListOf<Point>()

            for (direction in Direction.entries) {
                val next = current + direction

                // Check if next position is valid:
                // 1. Must be in freePoints
                // 2. Must not already be part of this snake
                // 3. Must not obstruct the head's line of sight to the edge
                if (next in freePoints &&
                    next !in snakePoints &&
                    !wouldObstructLineOfSight(next, head, headDirection)) {
                    validNeighbors.add(next)
                }
            }

            // If no valid neighbors, stop growing
            if (validNeighbors.isEmpty()) {
                break
            }

            // Pick a random valid neighbor
            current = validNeighbors.random()
            body.add(current)
            snakePoints.add(current)
        }

        // Body is built from head to tail, reverse it to get tail -> ... -> head order
        body.reverse()

        return Snake(ids++, body, headDirection)
    }

    /**
     * Checks if placing a body segment at 'bodyPoint' would obstruct the line of sight
     * from 'head' in 'headDirection' to the edge.
     * This checks if the bodyPoint is directly in the path from head towards the edge.
     */
    private fun wouldObstructLineOfSight(
        bodyPoint: Point,
        head: Point,
        headDirection: Direction
    ): Boolean {
        // Check if bodyPoint is in the line of sight from head to edge
        var current = head + headDirection

        // Walk in the direction the head is pointing
        // If we encounter the bodyPoint, it would obstruct the line of sight
        @Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER")
        for (_step in 0..100) { // Reasonable max distance
            if (current == bodyPoint) {
                return true // This body point would obstruct the line of sight
            }
            current += headDirection
        }

        return false
    }

    /**
     * All free points on the board.
     */
    private fun allFreePoints(width: Int, height: Int): Set<Point> {
        val freePoints =  mutableSetOf<Point>()
        for (x in 0 until width) {
            for (y in 0 until height) {
                freePoints.add(Point(x, y))
            }
        }
        return freePoints
    }

    /**
     * Picks a random point from freePoints that:
     * 1. Has a clear line of sight to the edge of the board in at least one direction
     * 2. Has at least one free point in the opposite direction (for snake body)
     *
     * @return A pair of (Point, Direction) where Direction points to the edge, or null if no valid point exists
     */
    private fun copyAndPickRandomUnobstructed(freePoints: Set<Point>, width: Int, height: Int): Pair<Point, Direction>? {
        val copy = freePoints.toMutableSet()

        while (copy.isNotEmpty()) {
            val randomPoint = copy.random()

            // Check all four directions
            for (direction in Direction.entries) {
                // Check if this direction has clear line of sight to edge
                if (hasCleanLineOfSightToEdge(randomPoint, direction, freePoints, width, height)) {
                    // Check if opposite direction has at least one free point
                    val oppositeDirection = getOppositeDirection(direction)
                    if (hasFreePointInDirection(randomPoint, oppositeDirection, freePoints, width, height)) {
                        return Pair(randomPoint, direction)
                    }
                }
            }

            // This point doesn't meet criteria, remove it and try another
            copy.remove(randomPoint)
        }

        return null
    }

    /**
     * Checks if there's a clear line of sight from the point to the edge in the given direction.
     * All points between the starting point and the edge must be in freePoints.
     */
    private fun hasCleanLineOfSightToEdge(
        point: Point,
        direction: Direction,
        freePoints: Set<Point>,
        width: Int,
        height: Int
    ): Boolean {
        var current = point + direction

        while (current.x in 0 until width && current.y in 0 until height) {
            if (current !in freePoints) {
                return false
            }
            current += direction
        }

        return true
    }

    /**
     * Checks if there's at least one free point in the given direction from the starting point.
     */
    private fun hasFreePointInDirection(
        point: Point,
        direction: Direction,
        freePoints: Set<Point>,
        width: Int,
        height: Int
    ): Boolean {
        val next = point + direction
        return next.x in 0 until width &&
               next.y in 0 until height &&
               next in freePoints
    }

    /**
     * Returns the opposite direction.
     */
    private fun getOppositeDirection(direction: Direction): Direction {
        return when (direction) {
            Direction.UP -> Direction.DOWN
            Direction.DOWN -> Direction.UP
            Direction.LEFT -> Direction.RIGHT
            Direction.RIGHT -> Direction.LEFT
        }
    }
}

