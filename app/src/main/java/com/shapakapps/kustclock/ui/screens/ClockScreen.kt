package com.shapakapps.kustclock.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.shapakapps.kustclock.R
import com.shapakapps.kustclock.audio.SoundManager
import com.shapakapps.kustclock.engine.ClockEngine
import com.shapakapps.kustclock.engine.ClockPhase
import com.shapakapps.kustclock.engine.PlayerClock
import com.shapakapps.kustclock.model.TimeControlRepository
import com.shapakapps.kustclock.storage.AppPreferences
import com.shapakapps.kustclock.ui.theme.ClockThemeColors
import com.shapakapps.kustclock.util.formatClockTime

@Composable
fun ClockScreen(
    controlId: Long,
    preferences: AppPreferences,
    soundManager: SoundManager
) {
    val setup = remember(controlId) { TimeControlRepository.pairById(controlId) }
    if (setup == null) return
    val engine = remember(controlId) {
        ClockEngine(setup.first, setup.second) { sound ->
            if (preferences.soundEnabled) soundManager.play(sound)
        }
    }
    var showReset by remember { mutableStateOf(false) }
    var showAdjust by remember { mutableStateOf(false) }

    DisposableEffect(engine) {
        engine.start()
        onDispose { engine.dispose() }
    }

    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    val lifecycleOwner = LocalContext.current as LifecycleOwner
    DisposableEffect(lifecycleOwner, engine) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) engine.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val themeColor = ClockThemeColors[preferences.themeColorIndex.coerceIn(0, ClockThemeColors.size - 1)]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ClockPanel(
            clock = engine.playerTwo,
            isActive = engine.activePlayer == 1 && engine.phase == ClockPhase.RUNNING,
            phase = engine.phase,
            isWinner = engine.winner == 1,
            themeColor = themeColor,
            onTap = { engine.press(1) },
            rotated = true,
            modifier = Modifier.weight(1f)
        )
        CenterBar(
            running = engine.phase == ClockPhase.RUNNING,
            onReset = { showReset = true },
            onTogglePause = {
                if (engine.phase == ClockPhase.RUNNING) engine.pause() else engine.resume()
            },
            onAdjust = { showAdjust = true },
            soundOn = preferences.soundEnabled,
            onToggleSound = { preferences.soundEnabled = !preferences.soundEnabled }
        )
        ClockPanel(
            clock = engine.playerOne,
            isActive = engine.activePlayer == 0 && engine.phase == ClockPhase.RUNNING,
            phase = engine.phase,
            isWinner = engine.winner == 0,
            themeColor = themeColor,
            onTap = { engine.press(0) },
            rotated = false,
            modifier = Modifier.weight(1f)
        )
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text(stringResource(R.string.reset_clock)) },
            confirmButton = {
                TextButton(onClick = {
                    engine.reset()
                    showReset = false
                }) { Text(stringResource(R.string.action_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text(stringResource(R.string.action_no)) }
            }
        )
    }

    if (showAdjust) {
        AdjustTimeDialog(engine = engine, onDismiss = { showAdjust = false })
    }
}

@Composable
private fun ClockPanel(
    clock: PlayerClock,
    isActive: Boolean,
    phase: ClockPhase,
    isWinner: Boolean,
    themeColor: androidx.compose.ui.graphics.Color,
    onTap: () -> Unit,
    rotated: Boolean,
    modifier: Modifier = Modifier
) {
    val errorColor = MaterialTheme.colorScheme.error
    val finished = phase == ClockPhase.FINISHED
    val lowTime = isActive && clock.remainingMillis < 20_000
    val backgroundColor = when {
        finished && isWinner -> themeColor
        finished -> errorColor
        isActive && lowTime -> errorColor
        isActive -> themeColor
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        backgroundColor == themeColor -> MaterialTheme.colorScheme.onPrimary
        backgroundColor == errorColor -> MaterialTheme.colorScheme.onError
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .graphicsLayer { rotationZ = if (rotated) 180f else 0f }
            .clickable(onClick = onTap)
    ) {
        if (clock.control.stages.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                clock.control.stages.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == clock.stageIndex) contentColor
                                else contentColor.copy(alpha = 0.25f)
                            )
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.moves_count, clock.movesMade),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val timeText = formatClockTime(clock.remainingMillis)
            Text(
                text = timeText,
                fontSize = if (timeText.length >= 7) 60.sp else 80.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            if (finished) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(if (isWinner) R.string.winner else R.string.time_up),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = contentColor
                )
            }
        }
        Text(
            text = clock.control.name,
            fontSize = 14.sp,
            color = contentColor.copy(alpha = 0.85f),
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}


@Composable
private fun CenterBar(
    running: Boolean,
    onReset: () -> Unit,
    onTogglePause: () -> Unit,
    onAdjust: () -> Unit,
    soundOn: Boolean,
    onToggleSound: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onReset) {
            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.reset_clock))
        }
        IconButton(onClick = onTogglePause) {
            if (running) {
                PauseIcon()
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.resume))
            }
        }
        IconButton(onClick = onAdjust) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.adjust_time))
        }
        IconButton(onClick = onToggleSound) {
            SoundIcon(on = soundOn)
        }
    }
}

@Composable
private fun PauseIcon() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 10.dp, height = 28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface)
        )
        Box(
            modifier = Modifier
                .size(width = 10.dp, height = 28.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
private fun SoundIcon(on: Boolean) {
    val color = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val body = Path().apply {
            moveTo(0.16f * w, 0.4f * h)
            lineTo(0.36f * w, 0.4f * h)
            lineTo(0.58f * w, 0.2f * h)
            lineTo(0.58f * w, 0.8f * h)
            lineTo(0.36f * w, 0.6f * h)
            lineTo(0.16f * w, 0.6f * h)
            close()
        }
        drawPath(body, color)
        if (on) {
            drawArc(
                color = color,
                startAngle = -35f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(0.62f * w, 0.28f * h),
                size = Size(0.26f * w, 0.44f * h),
                style = Stroke(width = 2.dp.toPx())
            )
            drawArc(
                color = color,
                startAngle = -35f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(0.52f * w, 0.12f * h),
                size = Size(0.46f * w, 0.76f * h),
                style = Stroke(width = 2.dp.toPx())
            )
        } else {
            drawLine(
                color = color,
                start = Offset(0.64f * w, 0.38f * h),
                end = Offset(0.86f * w, 0.62f * h),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = color,
                start = Offset(0.64f * w, 0.62f * h),
                end = Offset(0.86f * w, 0.38f * h),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
private fun AdjustTimeDialog(engine: ClockEngine, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.adjust_time)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AdjustRow(
                    label = stringResource(R.string.player_one),
                    current = formatClockTime(engine.playerOne.remainingMillis),
                    onAdjust = { engine.adjustTime(0, it) }
                )
                AdjustRow(
                    label = stringResource(R.string.player_two),
                    current = formatClockTime(engine.playerTwo.remainingMillis),
                    onAdjust = { engine.adjustTime(1, it) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_ok)) }
        }
    )
}

@Composable
private fun AdjustRow(label: String, current: String, onAdjust: (Long) -> Unit) {
    Column {
        Text(
            text = "$label · $current",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(
                "-1 min" to -60_000L,
                "-10 s" to -10_000L,
                "+10 s" to 10_000L,
                "+1 min" to 60_000L
            ).forEach { (text, delta) ->
                TextButton(onClick = { onAdjust(delta) }) {
                    Text(text, fontSize = 12.sp)
                }
            }
        }
    }
}

