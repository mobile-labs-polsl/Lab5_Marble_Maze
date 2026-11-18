package pl.polsl.mt.marblemaze.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Lifecycle-aware tilt source. MainActivity already calls lifecycle.addObserver(tiltSensor),
 * so onResume/onPause below are called automatically.
 *
 * [onTilt] expects SCREEN axes in m/s^2: rollRight > 0 = roll right, rollDown > 0 = roll down.
 */
class TiltSensor(
    context: Context,
    @Suppress("unused") private val onTilt: (rollRight: Float, rollDown: Float) -> Unit,
) : SensorEventListener, DefaultLifecycleObserver {

    @Suppress("unused") private val sensorManager = context.getSystemService(SensorManager::class.java)

    // TODO Part 4: choose the sensor
    private val sensor: Sensor? = null

    val isAvailable: Boolean get() = sensor != null

    override fun onResume(owner: LifecycleOwner) {
        // TODO Part 4
    }

    override fun onPause(owner: LifecycleOwner) {
        // TODO Part 4
    }

    override fun onSensorChanged(event: SensorEvent) {
        // TODO Part 4 (and Part 9: calibration offset)
    }

    /** Treat the current device pose as "flat". */
    fun calibrate() {
        // TODO Part 9
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
