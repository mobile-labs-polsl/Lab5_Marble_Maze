package pl.polsl.mt.marblemaze.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.polsl.mt.marblemaze.data.BestTimeStore
import pl.polsl.mt.marblemaze.data.LevelRepository
import pl.polsl.mt.marblemaze.game.GameEvent
import pl.polsl.mt.marblemaze.game.GameStatus
import pl.polsl.mt.marblemaze.game.MarbleEngine

/** Everything the HUD needs. Time is rounded to 0.1 s so the StateFlow does not emit every frame. */
data class HudState(
    val levelIndex: Int = 0,
    val levelCount: Int = 0,
    val levelTitle: String = "",
    val elapsedTenths: Long = 0,
    val wallHits: Int = 0,
    val bestMs: Long? = null,
    val status: GameStatus = GameStatus.READY,
    val newRecord: Boolean = false,
)

/**
 * Owns the engine so the game survives configuration changes.
 * The UI calls [onTilt] from the sensor and [onFrame] once per display frame,
 * draws [engine] and observes [hud] (state) and [events] (one-off effects).
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val levels = LevelRepository(app.assets).loadAll()
    private val bestTimes = BestTimeStore(app)

    val engine = MarbleEngine(levels.first())

    private val _hud = MutableStateFlow(HudState())
    val hud: StateFlow<HudState> = _hud.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var levelIndex = 0
    private var bestJob: Job? = null

    init {
        startLevel(0)
    }

    fun onTilt(rollRight: Float, rollDown: Float) = engine.setTilt(rollRight, rollDown)

    fun onFrame(dtSeconds: Float) {
        for (event in engine.step(dtSeconds)) {
            _events.tryEmit(event)
            if (event is GameEvent.ReachedGoal) saveTime(event.timeMs)
        }
        publishHud()
    }

    fun restart() {
        engine.reset()
        _hud.update { it.copy(newRecord = false) }
        publishHud()
    }

    fun nextLevel() = startLevel((levelIndex + 1) % levels.size)

    private fun startLevel(index: Int) {
        levelIndex = index
        engine.load(levels[index])
        _hud.value = HudState(
            levelIndex = index,
            levelCount = levels.size,
            levelTitle = levels[index].title,
        )
        bestJob?.cancel()
        bestJob = viewModelScope.launch {
            bestTimes.bestTime(index).collect { best -> _hud.update { it.copy(bestMs = best) } }
        }
    }

    private fun saveTime(timeMs: Long) {
        val index = levelIndex
        viewModelScope.launch {
            if (bestTimes.submit(index, timeMs)) _hud.update { it.copy(newRecord = true) }
        }
    }

    private fun publishHud() {
        _hud.update {
            it.copy(
                elapsedTenths = engine.elapsedMs / 100,
                wallHits = engine.wallHits,
                status = engine.status,
            )
        }
    }
}
