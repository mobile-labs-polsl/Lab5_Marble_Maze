package pl.polsl.mt.marblemaze.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import pl.polsl.mt.marblemaze.game.MarbleEngine
import kotlin.math.min

private val Board = Color(0xFFE9D8B4)
@Suppress("unused") private val GridDot = Color(0xFFDCC79E)
@Suppress("unused") private val Wall = Color(0xFF6D4C33)
@Suppress("unused") private val WallTop = Color(0xFF8A6446)
@Suppress("unused") private val Hole = Color(0xFF1E1712)
@Suppress("unused") private val HoleRim = Color(0xFF4A3828)
@Suppress("unused") private val Goal = Color(0xFF2E9E5B)

/** Draws the board and the marble. Game coordinates are in cells; here they become pixels. */
fun DrawScope.drawMaze(engine: MarbleEngine) {
    val level = engine.level
    val cell = min(size.width / level.cols, size.height / level.rows)
    val origin = Offset((size.width - cell * level.cols) / 2f, (size.height - cell * level.rows) / 2f)

    drawRect(Board, topLeft = origin, size = Size(cell * level.cols, cell * level.rows))

    // TODO Part 2: walls, holes, goal and the marble
}
