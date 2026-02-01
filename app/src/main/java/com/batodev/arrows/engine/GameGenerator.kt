package com.batodev.arrows.engine

import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

private const val DEFAULT_STRAIGHT_PREFERENCE = 0.90f
private const val PROGRESS_FACTOR = 1f

class GameGenerator {
    var straightPreference: Float = DEFAULT_STRAIGHT_PREFERENCE
        set(value) {
            require(value in 0f..1f) { "straightPreference must be in [0, 1]" }
            field = value
            snakeBuilder = SnakeBuilder(ids, rnd, value)
        }

    private val ids = AtomicInteger(0)
    private val rnd = Random(System.currentTimeMillis())
    private var snakeBuilder = SnakeBuilder(ids, rnd, straightPreference)

    fun generateSolvableLevel(params: GenerationParams): GameLevel {
        val walls = params.boardShape?.getWalls(params.width, params.height)
            ?: Array(params.width) { BooleanArray(params.height) }

        val config = GameGeneratorConfig(
            params.width, params.height, params.maxSnakeLength, walls
        )
        val context = GenerationContext(
            config, Array(params.width) { BooleanArray(params.height) },
            mutableListOf(), mutableSetOf()
        )

        val totalCells = GenerationUtils.countValidCells(params.width, params.height, walls)
        generateInitialSnakes(context, totalCells, params.onProgress)

        return GameLevel(params.width, params.height, context.snakes)
    }

    private fun generateInitialSnakes(
        context: GenerationContext, totalCells: Int, onProgress: (Float) -> Unit
    ) {
        var snake = snakeBuilder.buildFirstSnake(context.config, context.occupied)
        while (snake != null) {
            addSnakeToContext(context, snake)
            onProgress(calculateProgress(context.snakes, totalCells))
            snake = snakeBuilder.buildNextSnake(context)
        }
    }

    private fun addSnakeToContext(context: GenerationContext, snake: Snake) {
        context.snakes.add(snake)
        snake.body.forEach { context.occupied[it.x][it.y] = true }
        snake.body.forEach { p -> 
            Direction.entries.forEach { context.frontierCandidates.remove(Pair(p, it)) } 
        }
        updateFrontierWithSnake(context, snake)
    }

    private fun updateFrontierWithSnake(context: GenerationContext, snake: Snake) {
        snake.body.forEach { segment ->
            Direction.entries.forEach { dir ->
                val neighbor = segment + dir
                if (isFreeAt(neighbor, context.occupied, context.config)) {
                    addFrontierCandidatesForPoint(context, neighbor)
                }
            }
        }
    }

    private fun addFrontierCandidatesForPoint(context: GenerationContext, p: Point) {
        Direction.entries.forEach { headDir ->
            val hasLoS = GenerationUtils.hasClearLoS(
                p, headDir, context.occupied, context.config.width, context.config.height
            )
            if (hasLoS) {
                context.frontierCandidates.add(Pair(p, headDir))
            }
        }
    }

    private fun isFreeAt(p: Point, occupied: Array<BooleanArray>, config: GameGeneratorConfig): Boolean {
        return GenerationUtils.isInside(p, config.width, config.height) && 
                !occupied[p.x][p.y] && !config.walls[p.x][p.y]
    }

    private fun calculateProgress(snakes: List<Snake>, totalCells: Int): Float =
        (snakes.sumOf { it.body.size }.toFloat() / totalCells).coerceIn(0f, PROGRESS_FACTOR)
}
