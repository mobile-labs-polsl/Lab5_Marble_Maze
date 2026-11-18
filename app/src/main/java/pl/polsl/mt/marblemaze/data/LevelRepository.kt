package pl.polsl.mt.marblemaze.data

import android.content.res.AssetManager
import pl.polsl.mt.marblemaze.game.Level

/** Provides the list of levels shown by the game. */
class LevelRepository(@Suppress("unused") private val assets: AssetManager) {

    fun loadAll(): List<Level> {
        // TODO Part 7: levels from assets/levels (keep the built-in level as a fallback)
        return listOf(Level.parse(Level.BUILT_IN))
    }
}
