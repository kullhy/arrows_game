package com.batodev.arrows.engine

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class GameGeneratorTest {

    @Test
    fun testEverySpotOccupiedAfterGenerationParallel() {
        val engine = GameEngine()
        val width = 10
        val height = 10
        val simulations = 100
        val failures = AtomicInteger(0)

        val threads = mutableListOf<Thread>()

        for (sim in 1..simulations) {
            val t = Thread {
                val level = engine.generateSolvableLevel(width, height, 1.0) // Fill 100%
                var occupiedCount = 0
                val grid = Array(width) { IntArray(height) { 0 } }
                for (snake in level.snakes) {
                    for (p in snake.body) {
                        grid[p.x][p.y] = snake.id
                        occupiedCount++
                    }
                }

                if (occupiedCount != width * height) {
                    failures.incrementAndGet()
                    println("Simulation $sim/$simulations: FAILED - Not all spots occupied. Occupied: $occupiedCount, Total: ${width * height}")
                }
            }
            threads.add(t)
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        println("Completed $simulations parallel simulations. Total failures: ${failures.get()}")
        assertEquals("Some simulations failed: Not all spots occupied", 0, failures.get())
    }
}