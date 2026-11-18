package pl.polsl.mt.marblemaze.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Best completion time per level, persisted with Jetpack DataStore. */
class BestTimeStore(@Suppress("unused") private val context: Context) {

    /** Emits null while the level has not been completed yet. */
    fun bestTime(@Suppress("UNUSED_PARAMETER") levelIndex: Int): Flow<Long?> {
        // TODO Part 8
        return flowOf(null)
    }

    /** Saves [timeMs] only if it beats the stored record. Returns true for a new record. */
    suspend fun submit(@Suppress("UNUSED_PARAMETER") levelIndex: Int, @Suppress("UNUSED_PARAMETER") timeMs: Long): Boolean {
        // TODO Part 8
        return false
    }
}
