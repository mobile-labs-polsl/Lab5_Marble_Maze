package pl.polsl.mt.marblemaze.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.floor

/**
 * Plain JVM tests - run them with the green arrow next to the class name, no device needed.
 * In the starter project most of them FAIL. Your goal is to make all of them pass.
 */
class MarbleEngineTest {

    private val corridor = Level.parse(
        """
        ; Test corridor
        #######
        #S...G#
        #######
        """.trimIndent()
    )

    private val withHole = Level.parse(
        """
        #####
        #SOG#
        #####
        """.trimIndent()
    )

    private fun run(engine: MarbleEngine, seconds: Float) {
        repeat((seconds * 60).toInt()) { engine.step(1f / 60f) }
    }

    @Test
    fun parsesTitleAndSize() {
        assertEquals("Test corridor", corridor.title)
        assertEquals(7, corridor.cols)
        assertEquals(3, corridor.rows)
        assertEquals(1 to 1, corridor.startCell)
    }

    @Test
    fun outsideOfGridIsWall() {
        assertEquals(Tile.WALL, corridor[-1, 0])
        assertEquals(Tile.WALL, corridor[100, 100])
    }

    @Test
    fun ballWaitsForTiltBeforeClockStarts() {
        val engine = MarbleEngine(corridor)
        run(engine, 1f)
        assertEquals(GameStatus.READY, engine.status)
        assertEquals(0L, engine.elapsedMs)
    }

    @Test
    fun ballMovesWhenTilted() {
        val engine = MarbleEngine(corridor)
        engine.setTilt(6f, 0f)
        run(engine, 0.5f)
        assertTrue("x should grow, was ${engine.x}", engine.x > 1.6f)
        assertEquals(GameStatus.PLAYING, engine.status)
    }

    @Test
    fun ballNeverEntersWallUnderStrongTilt() {
        val engine = MarbleEngine(corridor)
        engine.setTilt(0f, 9.81f) // push hard into the bottom wall
        repeat(600) {
            engine.step(1f / 60f)
            assertNotEquals(Tile.WALL, corridor[floor(engine.x).toInt(), floor(engine.y).toInt()])
            assertTrue(engine.y + engine.radius <= 2.001f)
        }
        assertTrue("first impact should be counted", engine.wallHits >= 1)
    }

    @Test
    fun rollingRightReachesGoal() {
        val engine = MarbleEngine(corridor)
        engine.setTilt(6f, 0f)
        run(engine, 5f)
        assertEquals(GameStatus.WON, engine.status)
    }

    @Test
    fun holeEndsTheGame() {
        val engine = MarbleEngine(withHole)
        engine.setTilt(6f, 0f)
        run(engine, 3f)
        assertEquals(GameStatus.FELL, engine.status)
    }

    @Test
    fun resetReturnsToStart() {
        val engine = MarbleEngine(corridor)
        engine.setTilt(6f, 0f)
        run(engine, 0.5f)
        engine.reset()
        assertEquals(1.5f, engine.x, 1e-4f)
        assertEquals(GameStatus.READY, engine.status)
    }
}
