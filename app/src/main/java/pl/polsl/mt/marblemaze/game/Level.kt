package pl.polsl.mt.marblemaze.game

/** Type of a single cell of the maze grid. */
enum class Tile { EMPTY, WALL, HOLE, GOAL, START }

/**
 * Immutable maze definition. Coordinates are expressed in "cells":
 * column c covers x in [c, c+1), row r covers y in [r, r+1).
 *
 * Text format (one character per cell, every row has the same length):
 *   #  wall      .  floor      O  hole      S  start      G  goal
 * Lines starting with ';' are comments; the first comment becomes the level title.
 */
class Level(
    val title: String,
    val cols: Int,
    val rows: Int,
    private val tiles: Array<Tile>,
) {
    /** Cells outside the grid behave like walls, so the ball can never leave the board. */
    operator fun get(col: Int, row: Int): Tile =
        if (col !in 0 until cols || row !in 0 until rows) Tile.WALL else tiles[row * cols + col]

    val startCell: Pair<Int, Int> = findFirst(Tile.START) ?: (1 to 1)

    private fun findFirst(tile: Tile): Pair<Int, Int>? {
        for (r in 0 until rows) for (c in 0 until cols) if (this[c, r] == tile) return c to r
        return null
    }

    companion object {
        fun parse(text: String, fallbackTitle: String = "Level"): Level {
            val lines = text.lines().map { it.trimEnd() }
            val title = lines.firstOrNull { it.startsWith(";") }
                ?.removePrefix(";")?.trim()
                ?.ifEmpty { null } ?: fallbackTitle
            val grid = lines.filter { it.isNotBlank() && !it.startsWith(";") }
            require(grid.isNotEmpty()) { "Level '$title' is empty" }
            val cols = grid.maxOf { it.length }
            val tiles = Array(cols * grid.size) { i ->
                when (grid[i / cols].getOrElse(i % cols) { '#' }) {
                    '#' -> Tile.WALL
                    'O', 'o' -> Tile.HOLE
                    'G', 'g' -> Tile.GOAL
                    'S', 's' -> Tile.START
                    else -> Tile.EMPTY
                }
            }
            val level = Level(title, cols, grid.size, tiles)
            require(level.findFirst(Tile.GOAL) != null) { "Level '$title' has no goal (G)" }
            return level
        }

        /** Used until levels are loaded from assets (Part 7) and as a fallback. */
        val BUILT_IN = """
            ; Built-in corridor
            #########
            #S......#
            #######.#
            #.......#
            #.#######
            #.......#
            #######.#
            #......G#
            #########
        """.trimIndent()
    }
}
