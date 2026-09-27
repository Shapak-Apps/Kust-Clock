package com.shapakapps.kustclock.storage

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val themeIndexState = mutableStateOf(prefs.getInt(KEY_THEME_INDEX, 0))
    private val soundState = mutableStateOf(prefs.getBoolean(KEY_SOUND, true))

    var themeColorIndex: Int
        get() = themeIndexState.value
        set(value) {
            themeIndexState.value = value
            prefs.edit().putInt(KEY_THEME_INDEX, value).apply()
        }

    var soundEnabled: Boolean
        get() = soundState.value
        set(value) {
            soundState.value = value
            prefs.edit().putBoolean(KEY_SOUND, value).apply()
        }

    companion object {
        private const val PREFS_NAME = "kust_clock_preferences"
        private const val KEY_THEME_INDEX = "theme_color_index"
        private const val KEY_SOUND = "sound_enabled"
    }
}
