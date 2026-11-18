package pl.polsl.mt.marblemaze.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.polsl.mt.marblemaze.R
import pl.polsl.mt.marblemaze.feedback.FeedbackPlayer
import pl.polsl.mt.marblemaze.game.GameStatus

private val OnTable = Color(0xFFF3E7D3)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    feedback: FeedbackPlayer,
    onCalibrate: () -> Unit,
) {
    val hud by viewModel.hud.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current // needed in Part 3

    // Changing this value every frame invalidates only the Canvas draw phase (no recomposition).
    var frame by remember { mutableLongStateOf(0L) }

    // TODO Part 3: game loop (one viewModel.onFrame(dt) per display frame, only while RESUMED)

    // TODO Part 6: play one-off events (viewModel.events) with `feedback`

    var soundOn by remember { mutableStateOf(true) }
    var vibrationOn by remember { mutableStateOf(true) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Hud(hud)

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    frame // read state -> redraw on every new frame
                    drawMaze(viewModel.engine)
                }
                StatusBanner(hud)
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(onClick = viewModel::restart, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.restart))
                }
                FilledTonalButton(onClick = onCalibrate, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.calibrate))
                }
                Button(
                    onClick = viewModel::nextLevel,
                    enabled = hud.status == GameStatus.WON,
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.next_level)) }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.sound), color = OnTable)
                Spacer(Modifier.width(8.dp))
                Switch(checked = soundOn, onCheckedChange = { soundOn = it; feedback.soundEnabled = it })
                Spacer(Modifier.width(24.dp))
                Text(stringResource(R.string.vibration), color = OnTable)
                Spacer(Modifier.width(8.dp))
                Switch(checked = vibrationOn, onCheckedChange = { vibrationOn = it; feedback.vibrationEnabled = it })
            }
        }
    }
}

@Composable
private fun Hud(hud: HudState) {
    val tabular = MaterialTheme.typography.bodyLarge.copy(color = OnTable, fontFeatureSettings = "tnum")
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            stringResource(R.string.level_title, hud.levelIndex + 1, hud.levelCount, hud.levelTitle),
            style = MaterialTheme.typography.titleMedium,
            color = OnTable,
        )
        Row(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.time_value, formatTenths(hud.elapsedTenths)), style = tabular, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.hits_value, hud.wallHits), style = tabular, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.best_value, formatMs(hud.bestMs)), style = tabular, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatusBanner(hud: HudState) {
    val message = when (hud.status) {
        GameStatus.READY -> stringResource(R.string.status_ready)
        GameStatus.PLAYING -> null
        GameStatus.FELL -> stringResource(R.string.status_fell)
        GameStatus.WON -> stringResource(if (hud.newRecord) R.string.status_record else R.string.status_won)
    } ?: return
    Text(
        message,
        color = Color.White,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .background(Color(0xCC1B1410), RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
    )
}
