package com.shapakapps.kustclock.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    const val LANG_SYSTEM = "system"
    const val LANG_EN = "en"
    const val LANG_RU = "ru"

    fun resolveLocale(langMode: String, deviceLocale: Locale = Locale.getDefault()): Locale? {
        return when (langMode) {
            LANG_EN -> Locale("en")
            LANG_RU -> Locale("ru")
            else -> {
                // Auto: Russian device -> Russian, everything else -> English
                if (deviceLocale.language == "ru") Locale("ru") else Locale("en")
            }
        }
    }

    fun wrap(base: Context, langMode: String): Context {
        // IMPORTANT: capture device locale BEFORE setDefault, otherwise
        // "system/auto" mode would read back the previously forced locale.
        val deviceLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            base.resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION") base.resources.configuration.locale
        }
        val locale = resolveLocale(langMode, deviceLocale)
        // Persist as default so String.format etc. behave consistently
        locale?.let { Locale.setDefault(it) }
        val config = Configuration(base.resources.configuration)
        if (locale != null) {
            config.setLocale(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocales(LocaleList(locale))
            }
        }
        return base.createConfigurationContext(config)
    }

    fun currentAppLang(prefsLang: String): String {
        return resolveLocale(prefsLang)?.language ?: "en"
    }
}
