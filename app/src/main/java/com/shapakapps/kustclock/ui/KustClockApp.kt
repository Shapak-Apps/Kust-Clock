package com.shapakapps.kustclock.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.shapakapps.kustclock.audio.SoundManager
import com.shapakapps.kustclock.storage.AppPreferences
import com.shapakapps.kustclock.ui.screens.AppSettingsScreen
import com.shapakapps.kustclock.ui.screens.ClockScreen
import com.shapakapps.kustclock.ui.screens.TimeControlsScreen
import com.shapakapps.kustclock.ui.screens.TimerSettingsScreen
import com.shapakapps.kustclock.ui.theme.ClockThemeColors
import com.shapakapps.kustclock.ui.theme.KustClockTheme

sealed interface AppScreen {
    data object TimeControls : AppScreen
    data object AppSettings : AppScreen
    data class TimerSettings(val customId: Long?) : AppScreen
    data class Clock(val controlId: Long) : AppScreen
}

@Composable
fun KustClockApp() {
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
    val soundManager = remember { SoundManager(context) }
    val backStack = remember { mutableStateListOf<AppScreen>(AppScreen.TimeControls) }
    val current = backStack.last()

    DisposableEffect(soundManager) {
        onDispose { soundManager.release() }
    }

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeAt(backStack.size - 1)
    }

    val accent = ClockThemeColors[preferences.themeColorIndex.coerceIn(0, ClockThemeColors.size - 1)]

    KustClockTheme(themeColor = accent) {
        Crossfade(targetState = current, label = "screen") { screen ->
            when (screen) {
                AppScreen.TimeControls -> TimeControlsScreen(
                    onOpenClock = { id -> backStack.add(AppScreen.Clock(id)) },
                    onEditCustom = { id -> backStack.add(AppScreen.TimerSettings(id)) },
                    onNewCustom = { backStack.add(AppScreen.TimerSettings(null)) },
                    onOpenSettings = { backStack.add(AppScreen.AppSettings) }
                )
                AppScreen.AppSettings -> AppSettingsScreen(
                    preferences = preferences,
                    onBack = { backStack.removeAt(backStack.size - 1) }
                )
                is AppScreen.TimerSettings -> TimerSettingsScreen(
                    customId = screen.customId,
                    onDone = { backStack.removeAt(backStack.size - 1) }
                )
                is AppScreen.Clock -> ClockScreen(
                    controlId = screen.controlId,
                    preferences = preferences,
                    soundManager = soundManager
                )
            }
        }
    }
}
