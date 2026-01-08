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
    val body: List<Point>, // Ordered list: Head -> ... -> Tail
    val headDirection: Direction
)

data class GameLevel(
    val width: Int,
    val height: Int,
    val snakes: List<Snake>
)

interface Criterion {
    fun isSatisfied(body: List<Point>,
                    point: Point,
                    snakes: List<Snake>,
                    width: Int,
                    height: Int,
                    forbiddenPoints: Set<Point>): Boolean
}

class NextToExistingSnakeCriterion : Criterion {
    // 8-connected directions: orthogonal + diagonal
    private val allDirections = listOf(
        Pair(-1, -1), Pair(0, -1),
        Pair(1, -1), Pair(-1, 0),
        Pair(1, 0), Pair(-1, 1),
        Pair(0, 1),  Pair(1, 1)
    )

    override fun isSatisfied(
        body: List<Point>,
        point: Point,
        snakes: List<Snake>,
        width: Int,
        height: Int,
        forbiddenPoints: Set<Point>
    ): Boolean {
        // Check adjacency to existing snakes (8-connected)
        val adjacentToExistingSnake = snakes.any { snake ->
            snake.body.any { segment -> isAdjacent(point, segment) }
        }
        if (adjacentToExistingSnake) return true

        // Check adjacency to current snake's body, excluding the last segment (8-connected)
        val bodyWithoutLast = if (body.size > 1) body.dropLast(1) else emptyList()
        return bodyWithoutLast.any { segment -> isAdjacent(point, segment) }
    }

    private fun isAdjacent(p1: Point, p2: Point): Boolean {
        return allDirections.any { (dx, dy) -> p1.x + dx == p2.x && p1.y + dy == p2.y }
    }
}

class AlwaysTrueCriterion : Criterion {
    override fun isSatisfied(
        body: List<Point>,
        point: Point,
        snakes: List<Snake>,
        width: Int,
        height: Int,
        forbiddenPoints: Set<Point>
    ): Boolean {
        return true
    }
}

class GameGenerator {
    private val ids = java.util.concurrent.atomic.AtomicInteger(0)
    private val rnd = java.util.Random()

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
     * Algorithm:
     * 1. Put in a random snake, it can turn it's body.
     * 2. The snake cannot obstruct it's head, it's head should point to the edge of the board with clear line of sight.
     * 3. Put in another snakes head next to any segment (head, body) of an existing snake. The head has to have a clear line of sight to the edge.
     * 4. Generate the snakes body, it can and likely will turn, abiding two rules:
     *    - every new segment has to be next to an existing snake segment (body or head, it can be self),
     *    apart from being next to the previous segment of the same snake
     *    - the snakes arrowhead always has clear line of sight to the boards edge
     * 5. Repeat 3 until board is full.
     * @param width Width of the grid (e.g., 7)
     * @param height Height of the grid (e.g., 10)
     * @param maxSnakeLength Maximum length of the snake (e.g., 5)
     * @return A GameLevel object representing the puzzle
     */
    fun generateSolvableLevel(width: Int, height: Int, maxSnakeLength: Int): GameLevel {
        require(width > 0 && height > 0) { "Board must be non-empty" }
        require(maxSnakeLength >= 1) { "maxSnakeLength must be at least 1" }

        val snakes = ArrayList<Snake>(width * height)

        val firstSnake = buildFirstSnake(width, height, maxSnakeLength)
        snakes.add(firstSnake)

        var nextSnake: Snake? = buildNextSnake(width, height, maxSnakeLength, snakes)
//        nextSnake?.let {
//            snakes.add(nextSnake)
//        }

        while (nextSnake != null) {
            snakes.add(nextSnake)
            nextSnake = buildNextSnake(width, height, maxSnakeLength, snakes)
        }

        return GameLevel(width, height, snakes)
    }

    private fun buildNextSnake(width: Int, height: Int, maxSnakeLength: Int, snakes: List<Snake>): Snake? {
        val possibleHeads = possibleNextHeads(width, height, snakes)
        if (possibleHeads.isEmpty()) return null

        val longest = possibleHeads
            .map { (head, direction) ->
                val forbiddenPoints = forbiddenPoints(head, direction, width, height)
                val body = buildSnakeRecursive(
                    snakes,
                    listOf(head),
                    maxSnakeLength,
                    forbiddenPoints,
                    width,
                    height,
                    NextToExistingSnakeCriterion()
                )
                Snake(ids.incrementAndGet(), body, direction)
            }
            .maxByOrNull { it.body.size }

        return longest
    }

    private fun possibleNextHeads(
        width: Int,
        height: Int,
        snakes: List<Snake>
    ) : Set<Pair<Point, Direction>> {
        val possibleHeads = mutableSetOf<Pair<Point, Direction>>()
        snakes.forEach { existingSnake ->
            existingSnake.body.forEach { existingSnakeSegment ->
                Direction.entries.forEach { direction ->
                    val possibleHead = existingSnakeSegment + direction
                    if (isNotOutOfBounds(possibleHead, width, height) &&
                        hasClearLineOfSightToEdgeOfBoard(possibleHead, direction, snakes, width, height) &&
                        isNotPartOfAnySnake(snakes, possibleHead)
                    ) {
                        possibleHeads.add(Pair(possibleHead, direction))
                    }
                }
            }
        }
        return possibleHeads
    }

    private fun hasClearLineOfSightToEdgeOfBoard(
        possibleHead: Point,
        direction: Direction,
        snakes: List<Snake>,
        width: Int,
        height: Int
    ) : Boolean {
        var current = possibleHead
        while (true) {
            current = current.copy(x = current.x + direction.dx, y = current.y + direction.dy)
            if (current.x !in 0..<width || current.y !in 0..<height) {
                break
            }
            if (isPartOfAnySnake(snakes, current)) {
                return false
            }
        }
        return true
    }

    private fun isPartOfAnySnake(
        snakes: List<Snake>,
        current: Point
    ): Boolean {
        return !isNotPartOfAnySnake(snakes, current)
    }

    private fun isNotPartOfAnySnake(
        snakes: List<Snake>,
        point: Point
    ): Boolean = !snakes.any { snake -> snake.body.contains(point) }

    private fun buildFirstSnake(width: Int, height: Int, maxSnakeLength: Int): Snake {
        val headX = rnd.nextInt(width)
        val headY = rnd.nextInt(height)
        val head = Point(headX, headY)
        val direction = Direction.entries.toTypedArray().random()
        val forbiddenPoints = forbiddenPoints(head, direction, width, height)
        val body = buildSnakeRecursive(
            listOf(),
            listOf(head),
            maxSnakeLength,
            forbiddenPoints,
            width,
            height
        )
        return Snake(ids.incrementAndGet(), body, direction)
    }

    private fun buildSnakeRecursive(
        snakes: List<Snake>,
        body: List<Point>,
        maxSnakeLength: Int,
        forbiddenPoints: Set<Point>,
        width: Int,
        height: Int,
        criterion: Criterion = AlwaysTrueCriterion()
    ) : List<Point> {
        if (body.size >= maxSnakeLength) {
            return body
        }
        val tail = body.last()
        val shuffledDirections = directionsShuffled()
        val possibleNextSegments = mutableSetOf<Point>()
        shuffledDirections.forEach { direction ->
            val next = tail + direction
            // not in line of sight, not in body, in board bounds
            if (next !in forbiddenPoints &&
                next !in body &&
                isNotOutOfBounds(next, width, height) &&
                isNotPartOfAnySnake(snakes, next) &&
                criterion.isSatisfied(body, next, snakes, width, height, forbiddenPoints)) {
                possibleNextSegments.add(next)
            }
        }
        if (possibleNextSegments.isEmpty()) { // no more ways to go
            return body
        } else {
            return possibleNextSegments
                .map { nextSegment ->
                    // append after current tail so head stays at index 0
                    buildSnakeRecursive(
                        snakes,
                        body + nextSegment,
                        maxSnakeLength,
                        forbiddenPoints,
                        width,
                        height,
                        criterion
                    )
                }
                .maxByOrNull { it.size } ?: body
         }
     }

    private fun isNotOutOfBounds(point: Point, width: Int, height: Int): Boolean {
        return point.x in 0..<width && point.y in 0..<height
    }

    private fun directionsShuffled(): Array<Direction> {
        val shuffledDirections = Direction.entries.toTypedArray()
        shuffledDirections.shuffle()
        return shuffledDirections
    }

    private fun forbiddenPoints(head: Point, direction: Direction, width: Int, height: Int): Set<Point> {
        val points = mutableSetOf<Point>()
        var current = head
        while (true) {
            current = current.copy(x = current.x + direction.dx, y = current.y + direction.dy)
            if (current.x !in 0..<width || current.y !in 0..<height) {
                break
            }
            points.add(current)
        }
        return points
    }
}
