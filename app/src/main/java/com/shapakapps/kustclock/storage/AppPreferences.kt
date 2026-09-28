package com.shapakapps.kustclock.storage

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shapakapps.kustclock.util.LocaleHelper

class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val themeIndexState = mutableStateOf(prefs.getInt(KEY_THEME_INDEX, 0))
    private val soundState = mutableStateOf(prefs.getBoolean(KEY_SOUND, true))
    private val langState =
        mutableStateOf(prefs.getString(KEY_LANG, LocaleHelper.LANG_SYSTEM) ?: LocaleHelper.LANG_SYSTEM)

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

    var language: String
        get() = langState.value
        set(value) {
            langState.value = value
            prefs.edit().putString(KEY_LANG, value).apply()
        }

    fun readLanguage(): String =
        prefs.getString(KEY_LANG, LocaleHelper.LANG_SYSTEM) ?: LocaleHelper.LANG_SYSTEM

    companion object {
        private const val PREFS_NAME = "kust_clock_preferences"
        private const val KEY_THEME_INDEX = "theme_color_index"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_LANG = "app_language"
    }
}

