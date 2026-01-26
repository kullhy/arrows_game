package com.batodev.arrows.engine

object SolvabilityChecker {
    private const val SOLVABILITY_ITERATION_MARGIN = 10

    fun isResolvable(level: GameLevel): Boolean {
        val grid = Array(level.width) { IntArray(level.height) }
        val snakeMap = level.snakes.associateBy { it.id }
        level.snakes.forEach { s -> s.body.forEach { p -> grid[p.x][p.y] = s.id } }

        val remaining = snakeMap.keys.toMutableSet()
        val maxIter = level.snakes.size + SOLVABILITY_ITERATION_MARGIN
        var iter = 0

        while (remaining.isNotEmpty() && iter < maxIter) {
            iter++
            val removable = remaining.firstOrNull { sId ->
                val s = snakeMap[sId]!!
                hasCleanLoS(LoSParams(s.body.first(), s.headDirection, sId, grid, level.width, level.height))
            } ?: return false

            snakeMap[removable]!!.body.forEach { grid[it.x][it.y] = 0 }
            remaining.remove(removable)
        }
        return remaining.isEmpty()
    }

    private fun hasCleanLoS(params: LoSParams): Boolean {
        var curr = params.head + params.dir
        while (isInside(curr, params.w, params.h)) {
            if (params.grid[curr.x][curr.y] != 0 && params.grid[curr.x][curr.y] != params.sId) return false
            curr += params.dir
        }
        return true
    }

    private fun isInside(p: Point, w: Int, h: Int) = p.x in 0 until w && p.y in 0 until h
}
