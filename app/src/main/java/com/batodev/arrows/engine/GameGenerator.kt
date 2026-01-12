package com.batodev.arrows.engine

import java.util.concurrent.atomic.AtomicInteger

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
                    forbiddenPoints: Set<Point>,
                    occupied: Array<BooleanArray>): Boolean
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
        forbiddenPoints: Set<Point>,
        occupied: Array<BooleanArray>
    ): Boolean {
        // Check adjacency to existing snakes (8-connected) using occupied grid
        for ((dx, dy) in allDirections) {
            val nx = point.x + dx
            val ny = point.y + dy
            if (nx in 0 until width && ny in 0 until height) {
                if (occupied[nx][ny]) return true
            }
        }

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
        forbiddenPoints: Set<Point>,
        occupied: Array<BooleanArray>
    ): Boolean {
        return true
    }
}

class GameGenerator {
    /**
     * Bias for continuing in the same direction as the previous segment when growing a snake.
     *
     * 0.0 = no bias (fully random among valid moves)
     * 1.0 = always choose straight if it is possible
     */
    var straightPreference: Float = 0.90f
        set(value) {
            require(value in 0f..1f) { "straightPreference must be in [0, 1]" }
            field = value
        }

    private val ids = AtomicInteger(0)
    private val rnd = java.util.Random()

    /**
     * Generates a guaranteed solvable Arrows puzzle.
     */
    fun generateSolvableLevel(
        width: Int,
        height: Int,
        maxSnakeLength: Int,
        onProgress: (Float) -> Unit = {}
    ): GameLevel {
        require(width > 0 && height > 0) { "Board must be non-empty" }
        require(maxSnakeLength >= 1) { "maxSnakeLength must be at least 1" }

        val totalCells = width * height
        val snakes = ArrayList<Snake>(totalCells)
        val occupied = Array(width) { BooleanArray(height) }
        val frontierCandidates = mutableSetOf<Pair<Point, Direction>>()

        val firstSnake = buildFirstSnake(width, height, maxSnakeLength, occupied)
        snakes.add(firstSnake)
        markOccupied(occupied, firstSnake)
        updateFrontier(frontierCandidates, occupied, firstSnake, width, height)

        // Report progress
        onProgress(calculateProgress(snakes, totalCells))

        var nextSnake: Snake? = buildNextSnake(width, height, maxSnakeLength, snakes, occupied, frontierCandidates)

        while (nextSnake != null) {
            snakes.add(nextSnake)
            markOccupied(occupied, nextSnake)
            updateFrontier(frontierCandidates, occupied, nextSnake, width, height)

            onProgress(calculateProgress(snakes, totalCells))

            nextSnake = buildNextSnake(width, height, maxSnakeLength, snakes, occupied, frontierCandidates)
        }

        var lastSnake: Snake? = buildLastSnake(width, height, maxSnakeLength, snakes, occupied)

        while (lastSnake != null) {
            snakes.add(lastSnake)
            markOccupied(occupied, lastSnake)

            lastSnake = buildLastSnake(width, height, maxSnakeLength, snakes, occupied)
        }

        return GameLevel(width, height, snakes)
    }

    /**
     * Pick a random free spot to start with, next to existing snake.
     * Generate a random snake with only one rule: it cannot obstruct it own line of sight (forbiddenPoints).
     * Use depth-first with short-circuit: if any branch reaches maxSnakeLength, return it immediately.
     */
    private fun buildLastSnake(
        width: Int,
        height: Int,
        maxSnakeLength: Int,
        snakes: ArrayList<Snake>,
        occupied: Array<BooleanArray>
    ): Snake? {
        // TODO implement
        return null
    }

    private fun updateFrontier(
        frontier: MutableSet<Pair<Point, Direction>>,
        occupied: Array<BooleanArray>,
        newSnake: Snake,
        width: Int,
        height: Int
    ) {
        // Remove candidates that are now occupied
        newSnake.body.forEach { p ->
            Direction.entries.forEach { d ->
                frontier.remove(Pair(p, d))
            }
        }

        // Add new valid candidates around the new snake
        newSnake.body.forEach { segment ->
            Direction.entries.forEach { dir ->
                val neighbor = segment + dir
                if (isNotOutOfBounds(neighbor, width, height) && !occupied[neighbor.x][neighbor.y]) {
                    // Check all possible head directions for this neighbor
                    Direction.entries.forEach { headDir ->
                        if (hasClearLineOfSightToEdgeOfBoard(neighbor, headDir, occupied, width, height)) {
                            frontier.add(Pair(neighbor, headDir))
                        }
                    }
                }
            }
        }
    }

    private fun markOccupied(occupied: Array<BooleanArray>, snake: Snake) {
        snake.body.forEach { point ->
            occupied[point.x][point.y] = true
        }
    }

    private fun calculateProgress(snakes: List<Snake>, totalCells: Int): Float {
        val occupied = snakes.sumOf { it.body.size }
        return (occupied.toFloat() / totalCells).coerceIn(0f, 1f)
    }

    private fun buildNextSnake(
        width: Int,
        height: Int,
        maxSnakeLength: Int,
        snakes: List<Snake>,
        occupied: Array<BooleanArray>,
        frontier: MutableSet<Pair<Point, Direction>>
    ): Snake? {
        if (frontier.isEmpty()) return null

        val candidates = frontier.toList().shuffled(rnd)
        var bestSnake: Snake? = null

        for ((head, direction) in candidates) {
            // Lazy check: Is the candidate still valid?
            if (occupied[head.x][head.y]) continue
            if (!hasClearLineOfSightToEdgeOfBoard(head, direction, occupied, width, height)) continue

            val forbiddenPoints = forbiddenPoints(head, direction, width, height)
            val body = buildSnakeRecursive(
                snakes,
                listOf(head),
                maxSnakeLength,
                forbiddenPoints,
                width,
                height,
                NextToExistingSnakeCriterion(),
                previousMoveDir = null,
                occupied = occupied
            )

            val snake = Snake(ids.incrementAndGet(), body, direction)

            if (snake.body.size >= maxSnakeLength) {
                return snake // Early exit optimization
            }

            if (bestSnake == null || snake.body.size > bestSnake.body.size) {
                bestSnake = snake
            }
        }

        return bestSnake
    }

    private fun hasClearLineOfSightToEdgeOfBoard(
        possibleHead: Point,
        direction: Direction,
        occupied: Array<BooleanArray>,
        width: Int,
        height: Int
    ) : Boolean {
        var current = possibleHead
        while (true) {
            current = current.copy(x = current.x + direction.dx, y = current.y + direction.dy)
            if (current.x !in 0..<width || current.y !in 0..<height) {
                break
            }
            if (occupied[current.x][current.y]) {
                return false
            }
        }
        return true
    }

    private fun buildFirstSnake(
        width: Int,
        height: Int,
        maxSnakeLength: Int,
        occupied: Array<BooleanArray>
    ): Snake {
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
            height,
            previousMoveDir = null,
            occupied = occupied
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
        criterion: Criterion = AlwaysTrueCriterion(),
        previousMoveDir: Direction?,
        occupied: Array<BooleanArray>
    ) : List<Point> {
        if (body.size >= maxSnakeLength) {
            return body
        }
        val tail = body.last()

        // Collect possible directions (keeps validity rules unchanged)
        val shuffledDirections = directionsShuffled().toList()
        val possibleDirections = ArrayList<Direction>(4)
        shuffledDirections.forEach { direction ->
            val next = tail + direction
            if (next !in forbiddenPoints &&
                next !in body &&
                isNotOutOfBounds(next, width, height) &&
                !occupied[next.x][next.y] &&
                criterion.isSatisfied(body, next, snakes, width, height, forbiddenPoints, occupied)
            ) {
                possibleDirections.add(direction)
            }
        }

        if (possibleDirections.isEmpty()) { // no more ways to go
            return body
        }

        val ordered = orderDirectionsWithStraightBias(possibleDirections, previousMoveDir)

        // Depth-first with short-circuit: if any branch reaches maxSnakeLength, return it immediately.
        var best: List<Point> = body
        for (direction in ordered) {
            val nextSegment = tail + direction
            val candidate = buildSnakeRecursive(
                snakes = snakes,
                body = body + nextSegment,
                maxSnakeLength = maxSnakeLength,
                forbiddenPoints = forbiddenPoints,
                width = width,
                height = height,
                criterion = criterion,
                previousMoveDir = direction,
                occupied = occupied
            )

            if (candidate.size >= maxSnakeLength) {
                return candidate
            }
            if (candidate.size > best.size) {
                best = candidate
            }
        }

        return best
    }

    private fun orderDirectionsWithStraightBias(
        directions: List<Direction>,
        previousMoveDir: Direction?
    ): List<Direction> {
        if (previousMoveDir == null) return directions
        if (straightPreference <= 0f) return directions
        if (!directions.contains(previousMoveDir)) return directions

        // With probability straightPreference, try the straight direction first.
        if (rnd.nextFloat() >= straightPreference) return directions

        val reordered = ArrayList<Direction>(directions.size)
        reordered.add(previousMoveDir)
        directions.forEach { d -> if (d != previousMoveDir) reordered.add(d) }
        return reordered
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
