package com.batodev.arrows.engine

import org.junit.Assert.*
import org.junit.Test
import com.batodev.arrows.engine.GameEngine
import com.batodev.arrows.engine.Point
import com.batodev.arrows.engine.Direction
import com.batodev.arrows.engine.Snake
import com.batodev.arrows.engine.GameLevel

class GameEngineSolvableTest {
    /**
     * Test that at every step, there is at least one snake whose head points outside the board
     * and whose line of sight is unobstructed. Remove it, repeat until no snakes left.
     */
    @Test
    fun testEveryStepHasUnobstructedExitArrow() {
        val engine = GameEngine()
        val width = 70
        val height = 100
        val level = engine.generateSolvableLevel(width, height, 0.95)
        val snakes = level.snakes.toMutableList()
        val grid = Array(width) { Array<Snake?>(height) { null } }
        for (snake in snakes) {
            for (p in snake.body) {
                grid[p.x][p.y] = snake
            }
        }
        while (snakes.isNotEmpty()) {
            println("Current snakes: ${snakes.map { it.id }}")
            printBoard(grid, width, height)
            val removable = snakes.firstOrNull { snake ->
                isUnobstructedExit(snake, grid, width, height)
            }
            assertNotNull("No removable snake found, but snakes remain: $snakes", removable)
            val snake = removable!!
            // Remove snake from grid
            for (p in snake.body) {
                grid[p.x][p.y] = null
            }
            snakes.remove(snake)
        }
    }

    private fun isUnobstructedExit(snake: Snake, grid: Array<Array<Snake?>>, width: Int, height: Int): Boolean {
        var p = snake.body.last()
        val dir = snake.headDirection
        // Step in direction until out of bounds
        while (true) {
            p = Point(p.x + dir.dx, p.y + dir.dy)
            if (p.x !in 0 until width || p.y !in 0 until height) {
                return true // Reaches outside
            }
            // If any snake occupies this cell, it's blocked
            if (grid[p.x][p.y] != null) return false
        }
    }

    private fun printBoard(grid: Array<Array<Snake?>>, width: Int, height: Int) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val s = grid[x][y]
                if (s == null) print(".")
                else print(s.id % 10)
            }
            println()
        }
        println()
    }

    @Test
    fun testEveryStepHasRemovableSnakeByDependencyGraph() {
        val engine = GameEngine()
        val width = 7
        val height = 10
        val level = engine.generateSolvableLevel(width, height, 0.95)
        val snakes = level.snakes.toMutableList()
        val grid = Array(width) { IntArray(height) { 0 } }
        for (snake in snakes) {
            for (p in snake.body) {
                grid[p.x][p.y] = snake.id
            }
        }
        while (snakes.isNotEmpty()) {
            val dependencyGraph = buildDependencyGraph(snakes, grid, width, height)
            val removable = snakes.firstOrNull { snake ->
                dependencyGraph[snake.id]?.isEmpty() == true
            }
            println("Current snakes: ${snakes.map { it.id }}")
            printBoardInt(grid, width, height)
            assertNotNull("No removable snake found by dependency graph, but snakes remain: $snakes", removable)
            val snake = removable!!
            for (p in snake.body) {
                grid[p.x][p.y] = 0
            }
            snakes.remove(snake)
        }
    }

    private fun buildDependencyGraph(snakes: List<Snake>, grid: Array<IntArray>, w: Int, h: Int): Map<Int, List<Int>> {
        val graph = mutableMapOf<Int, MutableList<Int>>()
        for (snake in snakes) graph[snake.id] = mutableListOf()
        for (snake in snakes) {
            val target = snake.body.last() + snake.headDirection
            if (target.x in 0 until w && target.y in 0 until h) {
                val blockerId = grid[target.x][target.y]
                if (blockerId != 0 && blockerId != snake.id) {
                    graph[snake.id]?.add(blockerId)
                }
            }
        }
        return graph
    }

    private fun printBoardInt(grid: Array<IntArray>, width: Int, height: Int) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val s = grid[x][y]
                if (s == 0) print(".")
                else print(s % 10)
            }
            println()
        }
        println()
    }
}
