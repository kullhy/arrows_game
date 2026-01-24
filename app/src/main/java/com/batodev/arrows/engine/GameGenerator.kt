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
    val headDirection: Direction,
)

data class GameLevel(
    val width: Int,
    val height: Int,
    val snakes: List<Snake>,
)

interface Criterion {
    fun isSatisfied(
        body: List<Point>,
        point: Point,
        snakes: List<Snake>,
        width: Int,
        height: Int,
        forbiddenPoints: Set<Point>,
        occupied: Array<BooleanArray>,
    ): Boolean
}

class NextToExistingSnakeCriterion : Criterion {
    // 8-connected directions: orthogonal + diagonal
    private val allDirections = listOf(
        Pair(-1, -1), Pair(0, -1),
        Pair(1, -1), Pair(-1, 0),
        Pair(1, 0), Pair(-1, 1),
        Pair(0, 1), Pair(1, 1)
    )

    override fun isSatisfied(
        body: List<Point>,
        point: Point,
        snakes: List<Snake>,
        width: Int,
        height: Int,
        forbiddenPoints: Set<Point>,
        occupied: Array<BooleanArray>,
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
        occupied: Array<BooleanArray>,
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
        fillTheBoard: Boolean = false,
        onProgress: (Float) -> Unit = {}
    ): GameLevel {
        require(width > 0 && height > 0) { "Board must be non-empty" }
        require(maxSnakeLength >= 1) { "maxSnakeLength must be at least 1" }

        val config = GameGeneratorConfig(width, height, maxSnakeLength, fillTheBoard)
        val totalCells = width * height
        val snakes = ArrayList<Snake>(totalCells)
        val occupied = Array(width) { BooleanArray(height) }
        val frontierCandidates = mutableSetOf<Pair<Point, Direction>>()

        val firstSnake = buildFirstSnake(config, occupied)
        snakes.add(firstSnake)
        markOccupied(occupied, firstSnake)
        updateFrontier(frontierCandidates, occupied, firstSnake, config)

        onProgress(calculateProgress(snakes, totalCells))

        var nextSnake: Snake? =
            buildNextSnake(config, snakes, occupied, frontierCandidates)

        while (nextSnake != null) {
            snakes.add(nextSnake)
            markOccupied(occupied, nextSnake)
            updateFrontier(frontierCandidates, occupied, nextSnake, config)

            onProgress(calculateProgress(snakes, totalCells))

            nextSnake =
                buildNextSnake(config, snakes, occupied, frontierCandidates)
        }

        if (fillTheBoard) {
            fillRemainingBoard(config, snakes, occupied)
        }

        return GameLevel(width, height, snakes)
    }

    private data class GameGeneratorConfig(
        val width: Int,
        val height: Int,
        val maxSnakeLength: Int,
        val fillTheBoard: Boolean
    )

    private fun fillRemainingBoard(
        config: GameGeneratorConfig,
        snakes: ArrayList<Snake>,
        occupied: Array<BooleanArray>
    ) {
        var lastSnake: Snake? = buildLastSnake(config, snakes, occupied)

        while (lastSnake != null) {
            snakes.add(lastSnake)
            markOccupied(occupied, lastSnake)
            lastSnake = buildLastSnake(config, snakes, occupied)
        }
    }

    /**
     * Pick a random free spot to start with, next to existing snake.
     * Generate a random snake with only one rule: it cannot obstruct it own line of sight (forbiddenPoints).
     * Use depth-first with short-circuit: if any branch reaches maxSnakeLength, return it immediately.
     */
    private fun buildLastSnake(
        config: GameGeneratorConfig,
        snakes: ArrayList<Snake>,
        occupied: Array<BooleanArray>,
    ): Snake? {
        val candidates = occupied.flatMapIndexed { x, row ->
            row.withIndex()
                .filter { !it.value }
                .map { Point(x, it.index) }
        }.filter { point ->
            NextToExistingSnakeCriterion().isSatisfied(
                listOf(), point, snakes, config.width, config.height, emptySet(), occupied
            )
        }.flatMap { point ->
            Direction.entries.map { direction ->
                Pair(point, direction)
            }
        }
        var bestSnake: Snake? = null

        for ((head, direction) in candidates) {
            // Lazy check: Is the candidate still valid?
            if (occupied[head.x][head.y]) continue

            val forbiddenPoints = forbiddenPoints(head, direction, config.width, config.height)
            val body = buildSnakeRecursive(
                snakes,
                listOf(head),
                config.maxSnakeLength,
                forbiddenPoints,
                config.width,
                config.height,
                NextToExistingSnakeCriterion(),
                previousMoveDir = null,
                occupied = occupied
            )

            val snake = Snake(ids.incrementAndGet(), body, direction)

            if (snake.body.size >= config.maxSnakeLength && isResolvable(GameLevel(config.width, config.height, snakes + snake))) {
                return snake // Early exit optimization
            }

            if (bestSnake == null || snake.body.size > bestSnake.body.size) {
                if (isResolvable(GameLevel(config.width, config.height, snakes + snake))) {
                    bestSnake = snake
                }
            }
        }

        return bestSnake
    }

    private fun updateFrontier(
        frontier: MutableSet<Pair<Point, Direction>>,
        occupied: Array<BooleanArray>,
        newSnake: Snake,
        config: GameGeneratorConfig,
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
                if (isNotOutOfBounds(
                        neighbor,
                        config.width,
                        config.height
                    ) && !occupied[neighbor.x][neighbor.y]
                ) {
                    // Check all possible head directions for this neighbor
                    Direction.entries.forEach { headDir ->
                        if (hasClearLineOfSightToEdgeOfBoard(
                                neighbor,
                                headDir,
                                occupied,
                                config.width,
                                config.height
                            )
                        ) {
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
        config: GameGeneratorConfig,
        snakes: List<Snake>,
        occupied: Array<BooleanArray>,
        frontier: MutableSet<Pair<Point, Direction>>,
    ): Snake? {
        if (frontier.isEmpty()) return null

        val candidates = frontier.toList().shuffled(rnd)
        var bestSnake: Snake? = null

        for ((head, direction) in candidates) {
            // Lazy check: Is the candidate still valid?
            if (occupied[head.x][head.y]) continue
            if (!hasClearLineOfSightToEdgeOfBoard(
                    head,
                    direction,
                    occupied,
                    config.width,
                    config.height
                )
            ) continue

            val forbiddenPoints = forbiddenPoints(head, direction, config.width, config.height)
            val body = buildSnakeRecursive(
                snakes,
                listOf(head),
                config.maxSnakeLength,
                forbiddenPoints,
                config.width,
                config.height,
                NextToExistingSnakeCriterion(),
                previousMoveDir = null,
                occupied = occupied
            )

            val snake = Snake(ids.incrementAndGet(), body, direction)

            if (snake.body.size >= config.maxSnakeLength) {
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
        height: Int,
    ): Boolean {
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
        config: GameGeneratorConfig,
        occupied: Array<BooleanArray>,
    ): Snake {
        val headX = rnd.nextInt(config.width)
        val headY = rnd.nextInt(config.height)
        val head = Point(headX, headY)
        val direction = Direction.entries.toTypedArray().random()
        val forbiddenPoints = forbiddenPoints(head, direction, config.width, config.height)
        val body = buildSnakeRecursive(
            listOf(),
            listOf(head),
            config.maxSnakeLength,
            forbiddenPoints,
            config.width,
            config.height,
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
        occupied: Array<BooleanArray>,
    ): List<Point> {
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
        previousMoveDir: Direction?,
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

    private fun forbiddenPoints(
        head: Point,
        direction: Direction,
        width: Int,
        height: Int,
    ): Set<Point> {
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

    /**
     * Checks if a generated level is resolvable by simulating the puzzle-solving process.
     * A level is resolvable if all snakes can be removed by repeatedly finding snakes
     * with clear line of sight to the board edge and removing them.
     *
     * @param level The game level to check
     * @return true if the level can be solved, false otherwise
     */
    private fun isResolvable(level: GameLevel): Boolean {
        val width = level.width
        val height = level.height

        // Create a grid to track which cells are occupied
        val grid = Array(width) { IntArray(height) }
        val snakeMap = mutableMapOf<Int, Snake>()

        // Fill the grid with snake IDs
        for (snake in level.snakes) {
            snakeMap[snake.id] = snake
            for (p in snake.body) {
                grid[p.x][p.y] = snake.id
            }
        }

        val remainingSnakes = snakeMap.keys.toMutableSet()
        val maxIterations = level.snakes.size + 10 // Safety limit

        var iterationCount = 0
        while (remainingSnakes.isNotEmpty()) {
            iterationCount++

            if (iterationCount > maxIterations) {
                // Infinite loop detected
                return false
            }

            // Find a snake that can be removed (has clear line of sight to edge)
            val removableSnake =
                findRemovableSnakeId(remainingSnakes, snakeMap, grid, width, height)
                    ?: // No removable snake found, puzzle is unsolvable
                    return false

            // Remove the snake from the grid
            val snake = snakeMap[removableSnake]!!
            for (p in snake.body) {
                grid[p.x][p.y] = 0
            }
            remainingSnakes.remove(removableSnake)
        }

        // All snakes were successfully removed
        return true
    }

    /**
     * Finds a snake that has an unobstructed line of sight to the edge of the board.
     *
     * @return The snake ID or null if no such snake exists
     */
    private fun findRemovableSnakeId(
        remainingSnakes: Set<Int>,
        snakeMap: Map<Int, Snake>,
        grid: Array<IntArray>,
        width: Int,
        height: Int
    ): Int? {
        for (snakeId in remainingSnakes) {
            val snake = snakeMap[snakeId]!!
            val head = snake.body.first()
            val direction = snake.headDirection

            // Check if the line of sight to the edge is clear
            if (hasCleanLineOfSightToEdge(head, direction, snakeId, grid, width, height)) {
                return snakeId
            }
        }
        return null
    }

    /**
     * Checks if a snake head has a clear line of sight to the edge of the board.
     * The path must not contain any other snake parts (excluding its own body segments).
     *
     * @param head The snake head position
     * @param direction The direction the snake is facing
     * @param snakeId The ID of the current snake (to allow its own body in the path)
     * @param grid The occupancy grid
     * @param width Board width
     * @param height Board height
     * @return true if line of sight is clear, false otherwise
     */
    private fun hasCleanLineOfSightToEdge(
        head: Point,
        direction: Direction,
        snakeId: Int,
        grid: Array<IntArray>,
        width: Int,
        height: Int
    ): Boolean {
        var current = head + direction

        while (current.x in 0 until width && current.y in 0 until height) {
            val cellSnakeId = grid[current.x][current.y]

            // If there's another snake (not this one) blocking the path
            if (cellSnakeId != 0 && cellSnakeId != snakeId) {
                return false
            }

            current += direction
        }

        // We've reached the edge without obstruction
        return true
    }
}
