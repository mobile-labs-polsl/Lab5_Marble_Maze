package pl.polsl.mt.marblemaze.feedback

import android.content.Context
import pl.polsl.mt.marblemaze.game.GameEvent

/** Turns game events into sound and vibration. Sounds are in res/raw (hit, fall, win). */
class FeedbackPlayer(@Suppress("unused") private val context: Context) {

    var soundEnabled = true
    var vibrationEnabled = true

    fun play(event: GameEvent) {
        // TODO Part 6
    }

    fun release() {
        // TODO Part 6
    }
}
