package pl.polsl.mt.marblemaze.ui

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import pl.polsl.mt.marblemaze.R
import pl.polsl.mt.marblemaze.feedback.FeedbackPlayer
import pl.polsl.mt.marblemaze.sensor.TiltSensor
import pl.polsl.mt.marblemaze.ui.theme.MarbleTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private lateinit var tiltSensor: TiltSensor
    private lateinit var feedback: FeedbackPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        feedback = FeedbackPlayer(this)
        tiltSensor = TiltSensor(this) { right, down -> viewModel.onTilt(right, down) }
        lifecycle.addObserver(tiltSensor) // registers in onResume, unregisters in onPause
        if (!tiltSensor.isAvailable) {
            Toast.makeText(this, R.string.no_sensor, Toast.LENGTH_LONG).show()
        }

        setContent {
            MarbleTheme {
                GameScreen(
                    viewModel = viewModel,
                    feedback = feedback,
                    onCalibrate = {
                        tiltSensor.calibrate()
                        Toast.makeText(this, R.string.calibrated, Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        feedback.release()
        super.onDestroy()
    }
}
