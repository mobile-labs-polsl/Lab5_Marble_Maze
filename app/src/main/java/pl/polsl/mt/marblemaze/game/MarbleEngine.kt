package pl.polsl.mt.marblemaze.game

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Tunable physics constants. All distances are in cells, time in seconds. */
data class PhysicsConfig(
    /** Ball radius as a fraction of one cell. */
    val radius: Float = 0.32f,
    /** Cells/s^2 of acceleration per 1 m/s^2 of device tilt (9.81 = full tilt). */
    val sensitivity: Float = 1.6f,
    /** Rolling friction: fraction of velocity lost per second. */
    val friction: Float = 0.9f,
    /** Restitution of walls: 0 = ball stops at the wall, 1 = perfect bounce. */
    val restitution: Float = 0.5f,
    /** Speed limit in cells/s. */
    val maxSpeed: Float = 14f,
    /** Wall impacts slower than this (cells/s) are silent - avoids buzzing when resting on a wall. */
    val hitThreshold: Float = 1.0f,
)

enum class GameStatus { READY, PLAYING, FELL, WON }

/** One-off things that happened during a step; the UI turns them into sound and vibration. */
sealed interface GameEvent {
    /** [strength] in 0..1, proportional to the impact speed. */
    data class WallHit(val strength: Float) : GameEvent
    data object FellInHole : GameEvent
    data class ReachedGoal(val timeMs: Long) : GameEvent
}

/**
 * Pure-Kotlin game logic: no Android classes, so it can be unit-tested on the JVM.
 *
 * READY-MADE: movement, friction, sub-steps and collisions with walls.
 * YOUR PART: checkEvents() - the rules of the game (Part 5).
 */
class MarbleEngine(level: Level, private val cfg: PhysicsConfig = PhysicsConfig()) {

    var level: Level = level
        private set

    var x = 0f; private set
    var y = 0f; private set
    var vx = 0f; private set
    var vy = 0f; private set
    private var ax = 0f
    private var ay = 0f

    var status = GameStatus.READY; private set
    var elapsedMs = 0L; private set
    var wallHits = 0; private set

    val radius: Float get() = cfg.radius

    init { reset() }

    fun load(newLevel: Level) {
        level = newLevel
        reset()
    }

    /** Puts the ball on the start cell. The clock starts on the first tilt. */
    fun reset() {
        val (c, r) = level.startCell
        x = c + 0.5f; y = r + 0.5f
        vx = 0f; vy = 0f
        elapsedMs = 0L; wallHits = 0
        status = GameStatus.READY
    }

    /**
     * Tilt already converted to screen axes, in m/s^2:
     * [tx] > 0 means "roll right", [ty] > 0 means "roll down".
     */
    fun setTilt(tx: Float, ty: Float) {
        ax = tx * cfg.sensitivity
        ay = ty * cfg.sensitivity
    }

    /** Advances the simulation by [dtSeconds] and returns what happened. */
    fun step(dtSeconds: Float): List<GameEvent> {
        if (status == GameStatus.FELL || status == GameStatus.WON) return emptyList()
        // A long pause (app in background, debugger) must not make the ball jump.
        val dt = dtSeconds.coerceIn(0f, 0.05f)
        if (status == GameStatus.READY && hypot(ax, ay) > 1.5f) status = GameStatus.PLAYING
        if (status != GameStatus.PLAYING) return emptyList()

        elapsedMs += (dt * 1000).toLong()

        // Sub-steps: the ball never moves more than ~1/4 of its radius at once (no tunnelling).
        val travel = (hypot(vx, vy) + hypot(ax, ay) * dt) * dt
        val steps = max(1, (travel / (cfg.radius * 0.25f)).toInt() + 1)
        val h = dt / steps
        var strongestHit = 0f

        repeat(steps) {
            vx += ax * h
            vy += ay * h
            val damping = max(0f, 1f - cfg.friction * h)
            vx *= damping; vy *= damping
            val speed = hypot(vx, vy)
            if (speed > cfg.maxSpeed) { vx *= cfg.maxSpeed / speed; vy *= cfg.maxSpeed / speed }

            x += vx * h
            y += vy * h
            strongestHit = max(strongestHit, resolveWalls())
        }

        val events = mutableListOf<GameEvent>()
        checkEvents(strongestHit, events)
        return events
    }

    /**
     * The rules of the game, called once at the end of every frame.
     * @param strongestHit the largest wall impact speed in this frame (cells/s), 0 when no wall was touched
     * @param events add the events of this frame here
     */
    @Suppress("UNUSED_PARAMETER")
    private fun checkEvents(strongestHit: Float, events: MutableList<GameEvent>) {
        // TODO Part 5: wall hits, holes and the goal
    }

    /**
     * Circle vs. square collision with every wall cell the ball overlaps.
     * Pushes the ball out along the contact normal and reflects the normal
     * velocity component scaled by restitution. Returns the largest impact speed.
     */
    private fun resolveWalls(): Float {
        var impact = 0f
        val r = cfg.radius
        for (row in floor(y - r).toInt()..floor(y + r).toInt()) {
            for (col in floor(x - r).toInt()..floor(x + r).toInt()) {
                if (level[col, row] != Tile.WALL) continue
                // Closest point of the wall square to the ball centre.
                val cx = x.coerceIn(col.toFloat(), col + 1f)
                val cy = y.coerceIn(row.toFloat(), row + 1f)
                var nx = x - cx
                var ny = y - cy
                val dist = sqrt(nx * nx + ny * ny)
                if (dist >= r) continue
                val penetration: Float
                if (dist > 1e-5f) {
                    nx /= dist; ny /= dist
                    penetration = r - dist
                } else {
                    // Centre inside the wall (should not happen thanks to sub-steps): push out along the shortest axis.
                    val left = x - col; val right = col + 1 - x
                    val top = y - row; val bottom = row + 1 - y
                    val m = min(min(left, right), min(top, bottom))
                    nx = 0f; ny = 0f
                    when (m) {
                        left -> nx = -1f
                        right -> nx = 1f
                        top -> ny = -1f
                        else -> ny = 1f
                    }
                    penetration = m + r
                }
                x += nx * penetration
                y += ny * penetration
                val vn = vx * nx + vy * ny
                if (vn < 0f) {
                    vx -= (1f + cfg.restitution) * vn * nx
                    vy -= (1f + cfg.restitution) * vn * ny
                    impact = max(impact, abs(vn))
                }
            }
        }
        return impact
    }

    /** Helpers for checkEvents(). */
    @Suppress("unused")
    private fun tileUnderBall(): Tile = level[floor(x).toInt(), floor(y).toInt()]

    @Suppress("unused")
    private fun distanceToCellCenter(): Float =
        hypot(x - (floor(x) + 0.5f), y - (floor(y) + 0.5f))
}
